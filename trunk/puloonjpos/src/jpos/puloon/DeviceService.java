package jpos.puloon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.events.DataEvent;
import jpos.events.DirectIOEvent;
import jpos.events.ErrorEvent;
import jpos.events.JposEvent;
import jpos.events.OutputCompleteEvent;
import jpos.events.StatusUpdateEvent;
import jpos.loader.JposServiceInstance;
import jpos.services.BaseService;
import jpos.services.EventCallbacks;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Puloon Device Service
 * @author Maxim
 */
public abstract class DeviceService implements BaseService, JposServiceInstance  {

    //--------------------------------------------------------------------------
    // Public Base Service methods
    //

    /**
     * Tests the state of a device.
     * A text description of the results of this method is placed in the
     * CheckHealthText property.
     * @throws JposException
     */
    public void checkHealth(int level) throws JposException {
        if (!isPolling())
            poll();
    }

    /**
     * Requests exclusive access to the device.
     * @throws JposException
     */
    public void claim(int timeout) throws JposException {
        Object monitor = port;
        synchronized(monitor) {
            // Check for another object to claimed the port
            for (DeviceService service: deviceServices.keySet()) {
                if ((service.getClaimed()) && (service != this)
                        && (deviceServices.get(service) == port)) {
                    try {
                        monitor.wait(timeout);
                    } catch (InterruptedException e) {
                        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
                    }
                    if (service.getClaimed())
                        throw new JposException(JposConst.JPOS_E_CLAIMED,
                                getErrorDescription(JposConst.JPOS_E_CLAIMED));
                }
            }
            claimed = true;
        }
    }

    /**
     * Releases the device and its resources.
     * @throws JposException
     */
    public void close() throws JposException {
        if ((state != JposConst.JPOS_S_CLOSED) && (port != null)) {
            // If the DeviceEnabled property is true, then the device is
            // disabled.
            try {
                if (deviceEnabled)
                    setDeviceEnabled(false);
            } catch (JposException e) {}
            // If the Claimed property is true, then exclusive access to the
            // device is released.
            try {
                if (claimed)
                    release();
            } catch (JposException e) {}
            // Delete peripheral from map
            deviceServices.remove(this);
            // Check connected peripheral to this serial port
            boolean exists = false;
            for (SerialPort value: deviceServices.values())
                if (value == port)
                    exists = true;
            // Close unused port
            if (!exists) {
                // Delete port from map and close
                port.close();
                port = null;
            }
            state = JposConst.JPOS_S_CLOSED;
        }
    }

    /**
     * Communicates directly with the UnifiedPOS Service.
     * @param command Command number whose specific values are assigned by the
     * UnifiedPOS Service.
     * @param data An array of one mutable integer whose specific values or
     * usage vary by commandand UnifiedPOS Service.
     * @param obj Additional data whose usage varies by command and
     * UnifiedPOS Service.
     * @throws JposException
     */
    public void directIO(int cmd, int[] data, Object obj) throws JposException {
        Class objClass = obj.getClass();
        if (objClass.isArray() && (objClass.getComponentType() == String.class)) {
            char[] result = execute((char)cmd, ((String[])obj)[0].toCharArray(), false, 0);
            ((String[])obj)[0] = new String(result);
        } else if (objClass == String.class) {
            execute((char)cmd, ((String)obj).toCharArray(), false, 0);
        } else
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Return the results of the most recent call to the checkHealth method.
     * @return Health status description
     * @throws JposException
     */
    public String getCheckHealthText() throws JposException {
        return getStatusDescription(status);
    }

    /**
     * Get claimed device status
     * If true, the device is claimed for exclusive access. If false, the device
     * is released for sharing with other applications.
     * @return
     * @throws JposException
     */
    public boolean getClaimed() throws JposException {
        return claimed;
    }

    /**
     * Get device operation state
     * If true, the device is in an operational state.
     * If false, the device has been disabled.
     * @return Device operation state
     * @throws JposException
     */
    public boolean getDeviceEnabled() throws JposException {
        return deviceEnabled;
    }

    /**
     * Get freeze deliver events
     * If true, the UnifiedPOS Control will not deliver events.
     * @return Freeze event state
     * @throws JposException
     */
    public boolean getFreezeEvents() throws JposException {
        return frozeEvents;
    }

    /**
     * Holds the current state of the Device.
     */
    public int getState() throws JposException {
        return state;
    }

    /**
     * Opens a device for subsequent I/O.
     * @param logicalDeviceName Parameter specifies the device name to open
     * @param ec Callback event handler
     * @throws JposException
     */
    public void open(String string, EventCallbacks ec) throws JposException {
        // Check port opened
        if ((state != JposConst.JPOS_S_CLOSED) && (port != null))
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));

        powerNotify = JposConst.JPOS_PN_DISABLED;
        powerState = JposConst.JPOS_PS_UNKNOWN;
        state = JposConst.JPOS_S_ERROR;

        // Read and check parameters
        portName = jposEntry.getProp("portName").getValueAsString();
        int baudRate = Integer.valueOf(jposEntry.getProp("baudRate").getValueAsString());

        // Try find existen serial port
        for (DeviceService value: deviceServices.keySet()) {
            if (value.getPortName().equalsIgnoreCase(portName)) {
                port = deviceServices.get(value);
                break;
            }
        }
        // Open port
        if (port == null ) {
            try {
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
                CommPort commPort = portIdentifier.open("PULOON", RESPONSE_TIMEOUT);
                if (commPort instanceof SerialPort) {
                    port = ((SerialPort)commPort);
                    port.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    port.setDTR(true);
                    port.setRTS(true);
                    port.setInputBufferSize(SIZE_BUFFER);
                    port.setOutputBufferSize(SIZE_BUFFER);
                } else
                    throw new JposException(JposConst.JPOS_E_NOSERVICE,
                            getErrorDescription(JposConst.JPOS_E_NOSERVICE));
            } catch (NoSuchPortException e) {
                throw new JposException(JposConst.JPOS_E_NOHARDWARE,
                        getErrorDescription(JposConst.JPOS_E_NOHARDWARE), e);
            } catch (PortInUseException e) {
                throw new JposException(JposConst.JPOS_E_EXISTS,
                        getErrorDescription(JposConst.JPOS_E_EXISTS));
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_NOHARDWARE,
                        getErrorDescription(JposConst.JPOS_E_NOHARDWARE));
            }
        }
        // Streams
        try {
            is = port.getInputStream();
            os = port.getOutputStream();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
        // Add reference to port
        deviceServices.put(this, port);
        // Set events callback handler
        eventCallbacks = ec;
        // Set state
        state = JposConst.JPOS_S_IDLE;
    }

    /**
     * Releases exclusive access to the device.
     * @throws JposException
     */
    public void release() throws JposException {
        if (!claimed)
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        // If the DeviceEnabled property is true, then the device is disabled.
        try {
            if (deviceEnabled)
                setDeviceEnabled(false);
        } catch (JposException e) {}
        // Release claimed state and notify waiting monitor
        Object monitor = port;
        synchronized(monitor){
            claimed = false;
            monitor.notify();
        }
    }

    /**
     * Get device operation state
     *
     * If true, the device is in an operational state. If changed to true, then
     * the device is brought to an operational state.
     * If false, the device has been disabled. If changed to false, then the
     * device is physically disabled when possible, any subsequent input will be
     * discarded, and output operations are disallowed.
     *
     * @param bln New device operation state
     * @throws JposException
     */
    public void setDeviceEnabled(boolean bln) throws JposException {
        if (bln) {
            reset();
            poll();
            eventThread = new Thread(new EventRunner());
            eventThread.start();
            startPolling();
            deviceEnabled = true;
        } else {
            deviceEnabled = false;
            stopPolling();
            dataEventEnabled = false;
            frozeEvents = false;
            eventStore.clear();
            synchronized (eventQueue) {
                eventQueue.clear();
                eventQueue.notify();
            }
            eventThread = null;
        }
    }

    /**
     * Set freeze deliver events
     *
     * If true, the UnifiedPOS Control will not deliver events. Events will be
     * enqueued until this property is set to false. If false, the application
     * allows events to be delivered. If some events have been held while events
     * were frozen and all other conditions are correct for delivering the
     * events, then changing this property to false will allow these events to
     * be delivered. An application may choose to freeze events for a specific
     * sequence of code where interruption by an event is not desirable.
     *
     * @param bln
     * @throws JposException
     */
    public void setFreezeEvents(boolean bln) throws JposException {
        synchronized (eventQueue) {
            frozeEvents = bln;
            if (!frozeEvents)
                eventQueue.notify();
        }
    }

    /**
     * Called when the JposServiceConnection is disconnected (i.e. service is closed)
	 * @since 1.2 (NY2K meeting)
     */
    public void deleteInstance() throws JposException {}


    /**
     * Error description
     * @param code Error code
     * @return Description for error code
     */
    protected String getErrorDescription(int code) {
        switch (code) {
            case JposConst.JPOS_E_TIMEOUT:
                return "Time out waiting for device response";
            case JposConst.JPOS_E_FAILURE:
                return "Invalid device response";
            case JposConst.JPOS_E_NOSERVICE:
                return "Device is not found";
            case JposConst.JPOS_E_NOHARDWARE:
                return "Port is not found";
            case JposConst.JPOS_E_ILLEGAL:
                return "Illegal commad";
            case JposConst.JPOS_E_NOTCLAIMED:
                return "Device is not claimed";
            case JposConst.JPOS_E_CLAIMED:
                return "Device is already claimed";
            case JposConst.JPOS_E_CLOSED:
                return "Device is not initialized";
            case JposConst.JPOS_E_EXISTS:
                return "Device port is already opened";
        }
        return "Unknown error";
    }

    //--------------------------------------------------------------------------
    // Package methods
    //

    /**
     * Allows the JposServiceInstanceFactory to set the JposEntry associated with
     * this DeviceService.  Subclasses can access the JposEntry with getter
     */
    void setJposEntry( JposEntry entry )
    {
        jposEntry = entry;
    }

    /**
     * @return the JposEntry object associated with this DeviceService
     */
    JposEntry getJposEntry() {
        return jposEntry;
    }

    /**
     * Get port name
     * @return
     */
    String getPortName() {
        return portName;
    }

    /**
     * Check polling mode
     * @return
     */
    protected boolean isPolling() {
        return pollingTimer != null;
    }


    /**
     * Start polling process
     */
    protected void startPolling() {
        if (!isPolling()) {
            pollingTimer = new Timer();
            pollingTimer.scheduleAtFixedRate(new PollingTask(), POLL_INTERVAL,
                    POLL_INTERVAL);
        }
    }

    /**
     * Stop polling process
     */
    protected void stopPolling() {
        if (isPolling()) {
            pollingTimer.cancel();
            pollingTimer.purge();
            pollingTimer = null;
        }
    }

    /**
     * Refresh current status
     * @return Peripheral state
     */
    protected abstract void poll() throws JposException;

    /**
     * Reset device
     * @return Peripheral state
     */
    protected abstract void reset() throws JposException;
    
    /**
     * Block Check Character
     * BCC can be gotten through Exclusive-OR (XOR) from the start of each 
     * message to ETX except BCC.
     * @param len
     * @return
     */
    private int calcBCC(int data[], int len) {
        if (len > 0) {
            int result = data[0];
            int i = 1;
            while (i < len) {
                result = result ^ data[i];
                i++;
            }
            return result;
        } else
            return 0;
    }

    /**
     * Receive byte from stream
     * @param timeout
     * @return
     */
    protected int receive(int timeout) throws JposException {
        Object monitor = port;
        try {
            synchronized(monitor) {
                long start = System.currentTimeMillis();
                while (true) {
                    int data = is.read();
                    if (data >= 0)
                        return data;
                    if ((System.currentTimeMillis() - start) > timeout)
                        break;
                    monitor.wait(INTER_BYTE_TIMEOUT);
                }
            }
            throw new JposException(JposConst.JPOS_E_TIMEOUT,
                    getErrorDescription(JposConst.JPOS_E_TIMEOUT));
        } catch (InterruptedException e) {
            throw new JposException(JposConst.JPOS_E_TIMEOUT,
                    getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
    }

    /**
     * Send command to device
     * @param cmd command code
     * @param para additional params
     */
    protected void command(char cmd, char[] para) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                int repeatCount = 0;
                int result = 0;
                while (repeatCount < MAX_RETRY) {
                    // Clear input stream
                    port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                    try {
                        while (is.read() > -1) {}
                    } finally {
                        port.disableReceiveTimeout();
                    }
                    // Start of Transmission
                    int index = 0;
                    buffer[index] = EOT_CHAR;
                    index++;
                    // Communication ID
                    buffer[index] = getIdChar();
                    index++;
                    // Start of Text
                    buffer[index] = STX_CHAR;
                    index++;
                    // Command Code
                    buffer[index] = cmd;
                    index++;
                    // Command Parameter (Variable Length)
                    if (para != null) {
                        for (int i = 0; i < para.length; i++)
                            buffer[index + i] = para[i];
                        index = index + para.length;
                    }
                    // End of Text
                    buffer[index] = ETX_CHAR;
                    index++;
                    // Block Check Character
                    buffer[index] = calcBCC(buffer, index);
                    index++;
                    // Transmit data to output stream
                    for (int i = 0; i < index; i++)
                        os.write(buffer[i]);
                    os.flush();
                    // System.out.println(dataToHex(buffer, 0, index));
                    // Receive ACK or NAK
                    port.enableReceiveTimeout(ACK_WAITING_TIMEOUT);
                    try {
                        result = is.read();
                    } finally {
                        port.disableReceiveTimeout();
                    }
                    // Подтверждение - выход из цикла повторений
                    if (result == ACK_CHAR) break;
                    // Следующий повтор
                    repeatCount++;
                }
                if (result != ACK_CHAR) 
                    throw new JposException(JposConst.JPOS_E_FAILURE,
                            getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        }
    }

    protected char[] response(char cmd) throws JposException {
        Object monitor = port;
        synchronized(monitor) {
            boolean done = false;
            int index = 0;
            long start = System.currentTimeMillis();
            try {
                port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                try {
                    int repeatCount = 0;
                    while ((!done) && (repeatCount < MAX_RETRY) &&
                            ((System.currentTimeMillis() - start) < RESPONSE_TIMEOUT)) {
                        int data = is.read();
                        if (data == SOH_CHAR) {
                            // Start of Header
                            buffer[index] = data;
                            index++;
                            // Communications ID
                            if ((buffer[index] = is.read()) == getIdChar()) {
                                index++;
                                // Start of Text
                                if ((buffer[index] = is.read()) == STX_CHAR) {
                                    index++;
                                    // Command Code
                                    if ((buffer[index] = is.read()) == cmd) {
                                        index++;
                                        // Response Parameter to End of Text
                                        while ((data = is.read()) != ETX_CHAR) {
                                            if (data < 0)
                                                break;
                                            buffer[index] = data;
                                            index++;
                                        }
                                        if (data == ETX_CHAR) {
                                            buffer[index] = data;
                                            index++;
                                            // Block Check Character
                                            int bcc = is.read();
                                            if (bcc == calcBCC(buffer, index)) {
                                                buffer[index] = bcc;
                                                index++;
                                                done = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (done) {
                                // Send ACK
                                os.write(ACK_CHAR);
                                os.flush();
                            } else {
                                // Clear input stream
                                while (is.read() > -1) {}
                                // Send NAK
                                os.write(NAK_CHAR);
                                os.flush();
                                // Try again
                                index = 0;
                                repeatCount++;
                                start = System.currentTimeMillis();
                            }
                        }
                        if (data < 0)
                            monitor.wait(INTER_BYTE_TIMEOUT);
                    }
                } finally {
                    port.disableReceiveTimeout();
                }
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE), e);
            }
            if (!done)
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT));
            // Create result buffer
            // System.out.println(dataToHex(buffer, 0, index));
            if (index > 6) {
                char[] result = new char[index-6];
                for (int i = 4; i < index-2; i++)
                    result[i - 4] = (char)buffer[i];
                return result;
            } else
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }


    /**
     * Execute command and wait receive
     * @param cmd Command
     * @param para Parameters
     * @param freeTime Pause after response
     * @return response
     */
    protected char[] execute(char cmd, char[] para, boolean waitEOT, int freeTime) throws JposException {
        return execute(cmd, para, true, waitEOT, freeTime);
    }

    /**
     * Execute command and wait receive
     * @param cmd
     * @param para
     * @param waitResponse
     * @param waitEOT
     * @param freeTime
     * @return
     * @throws JposException
     */
    protected char[] execute(char cmd, char[] para, boolean waitResponse, boolean waitEOT, int freeTime) throws JposException {
        char[] result = null;
        try {
            Object monitor = port;
            synchronized(monitor) {
                while (executed)
                    monitor.wait();
                executed = true;
                try {
                    // Send command
                    command(cmd, para);
                    if (waitResponse) {
                        // Read response
                        result = response(cmd);
                        // Wait for device ready
                        if (waitEOT) {
                            port.enableReceiveTimeout(EOT_WAITING_TIMEOUT);
                            try {
                                is.read();
                            } finally {
                                port.disableReceiveTimeout();
                            }
                        }
                    }
                    // Wait for free time
                    if (freeTime > 0)
                        monitor.wait(freeTime);
                } finally {
                    executed = false;
                    monitor.notify();
                }
            }
        } catch (InterruptedException e) {
            throw new JposException(JposConst.JPOS_E_TIMEOUT,
                    getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
        } catch (UnsupportedCommOperationException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                getErrorDescription(JposConst.JPOS_E_FAILURE));
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
        return result;
    }

    /**
     * Enqueue event.
     * @param event
     */
    protected void fireEvent(JposEvent event) {
        if (deviceEnabled) {
            if (dataEventEnabled ||
                    (!(event instanceof DataEvent)) ||
                    (!(event instanceof ErrorEvent)))
                synchronized (eventQueue) {
                    eventQueue.add(event);
                    eventQueue.notify();
                }
            else
                eventStore.add(event);
        }
    }

    /**
     * Get status description
     * @return State description
     */
    protected abstract String getStatusDescription(int status);

    /**
     * Get ID char for parent device
     * @return
     */
    protected abstract int getIdChar();

    /**
     * Convert hex string to binary data
     * @param string  String
     * @return  Data array
     */
    protected int[] hexToData(String string) {
        int data[] = new int[string.length() / 2];
        for (int i = 0; i < data.length; i++) {
          int index = i * 2;
          data[i] = Integer.parseInt(string.substring(index, index + 2), 16);
        }
        return data;
    }

    /**
     * Convert binary data to hex string
     * @param data  Data array
     * @param ofs   Offset
     * @param len   Length
     * @return  Hex string
     */
    protected String dataToHex(int data[], int ofs, int len) {
        StringBuilder s = new StringBuilder(2*len);
        for (int i = 0; i < len; i++) {
            String h = Integer.toHexString(data[i + ofs]).toUpperCase();
            if (h.length() < 2)
                s.append('0');
            s.append(h);
        }
        return s.toString();
    }

    //--------------------------------------------------------------------------
    // Private task and thread classes
    //

    /**
     * Thread for delivery events
     * The Service must enqueue these events on an internally created and
     * managed queue. All events are delivered in a first-in, first-out manner.
     * Events are delivered by an internally created and managed Service thread.
     * The Service causes event delivery by calling an event firing callback
     * method in the Control, which then delivers the event to the application.
     */
    private class EventRunner implements Runnable {

        public void run() {
            try {
                while (deviceEnabled) {
                    if (!frozeEvents) {
                        while (true) {
                            JposEvent event = null;
                            synchronized (eventQueue) {
                                 event = eventQueue.poll();
                            }
                            if (event == null)
                                break;
                            try {
                                if (eventCallbacks != null) {
                                    if (event instanceof DataEvent)
                                        eventCallbacks.fireDataEvent(
                                                (DataEvent)event);
                                    else if (event instanceof ErrorEvent)
                                        eventCallbacks.fireErrorEvent(
                                                (ErrorEvent)event);
                                    else if (event instanceof DirectIOEvent)
                                        eventCallbacks.fireDirectIOEvent(
                                                (DirectIOEvent)event);
                                    else if (event instanceof OutputCompleteEvent)
                                        eventCallbacks.fireOutputCompleteEvent(
                                                (OutputCompleteEvent)event);
                                    else if (event instanceof StatusUpdateEvent)
                                        eventCallbacks.fireStatusUpdateEvent(
                                                (StatusUpdateEvent)event);
                                }
                            } catch (Exception e) {}
                        }
                    }
                    synchronized (eventQueue) {
                        eventQueue.wait();
                    }
                }
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Event comparator class
     */
    private class EventComparator implements Comparator<JposEvent> {

        public int compare(JposEvent o1, JposEvent o2) {
                if (o1.getSequenceNumber() < o2.getSequenceNumber())
                    return -1;
                else
                    return 1;
        }

    }

    /**
     * Task that continue executing each poll interval
     */
    private class PollingTask extends TimerTask {

        /**
         * Implements run task
         */
        public void run() {
            try {
                Object monitor = port;
                synchronized(monitor) {
                    if (!executed)
                        poll();
                }
            } catch (JposException e) {}
        }

    }

    //--------------------------------------------------------------------------
    // Static collections
    //

    private static final HashMap<DeviceService, SerialPort> deviceServices =
            new HashMap<DeviceService, SerialPort>();

    //--------------------------------------------------------------------------
    // Private and protected local variables
    //

    protected JposEntry jposEntry;
    private int buffer[] = new int[SIZE_BUFFER];
    private SerialPort port = null;
    private InputStream is = null;
    private OutputStream os = null;
    private String portName = null;
    private boolean executed = false;
    protected boolean claimed = false;
    protected EventCallbacks eventCallbacks = null;
    protected int state = JposConst.JPOS_S_CLOSED;
    protected int status = 0;
    protected boolean deviceEnabled = false;
    protected int powerState = JposConst.JPOS_PS_UNKNOWN;
    protected int powerNotify = JposConst.JPOS_PN_DISABLED;
    protected boolean frozeEvents = false;
    protected boolean dataEventEnabled = false;
    private Thread eventThread = null;
    private Timer pollingTimer = null;
    protected final PriorityQueue<JposEvent> eventQueue =
            new PriorityQueue<JposEvent>(100, new EventComparator());
    protected final ArrayList<JposEvent> eventStore =
            new ArrayList<JposEvent>();

    //--------------------------------------------------------------------------
    // Constants
    //

    /**
     * Buffer max size
     */
    private static final int SIZE_BUFFER = 1024;

    /**
     * Retrive count for executing
     */
    private static final int MAX_RETRY = 3;

    /**
     * Response timeout
     */
    private static final int RESPONSE_TIMEOUT = 300000;
    
    /**
     * ACK or NAK waiting timeout
     */
    private static final int ACK_WAITING_TIMEOUT = 550;

    /**
     * EOT waiting timeout
     */
    private static final int EOT_WAITING_TIMEOUT = 2000;

    /**
     * Max interval between two bytes transmition
     */
    private static final int INTER_BYTE_TIMEOUT = 50;

    /**
     * Status polling interval
     */
    private static final int POLL_INTERVAL = 1000;

    /**
     * ACK to indicate that message has been accepted.
     */
    private static final int ACK_CHAR = 0x06;

    /**
     * NAK to indicate that the message has been rejected and that the message should be resent.
     */
    private static final int NAK_CHAR = 0x15;

    /**
     * Communication ID
     */
    // private static final int ID_CHAR = 0x30;

    /**
     * Start of Head
     */
    private static final int SOH_CHAR = 0x01;

    /**
     * Start of Text
     */
    private static final int STX_CHAR = 0x02;

    /**
     * End of Text
     */
    private static final int ETX_CHAR = 0x03;

    /**
     * Start of Transmission
     */
    private static final int EOT_CHAR = 0x04;

}

