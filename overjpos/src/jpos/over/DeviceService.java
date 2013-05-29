/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.over;

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
public abstract class DeviceService implements BaseService, JposServiceInstance {

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
        if (!isPolling()) {
            poll();
        }
    }

    /**
     * Requests exclusive access to the device.
     * @throws JposException
     */
    public void claim(int timeout) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            // Check for another object to claimed the port
            for (DeviceService service : deviceServices.keySet()) {
                if ((service.getClaimed()) && (service != this)
                        && (deviceServices.get(service) == port)) {
                    try {
                        monitor.wait(timeout);
                    } catch (InterruptedException e) {
                        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
                    }
                    if (service.getClaimed()) {
                        throw new JposException(JposConst.JPOS_E_CLAIMED,
                                getErrorDescription(JposConst.JPOS_E_CLAIMED));
                    }
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
                if (deviceEnabled) {
                    setDeviceEnabled(false);
                }
            } catch (JposException e) {
            }
            // If the Claimed property is true, then exclusive access to the
            // device is released.
            try {
                if (claimed) {
                    release();
                }
            } catch (JposException e) {
            }
            // Delete peripheral from map
            deviceServices.remove(this);
            // Check connected peripheral to this serial port
            boolean exists = false;
            for (SerialPort value : deviceServices.values()) {
                if (value == port) {
                    exists = true;
                }
            }
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
    public void directIO(int command, int[] data, Object obj) throws JposException {
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
        if ((state != JposConst.JPOS_S_CLOSED) && (port != null)) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }

        powerNotify = JposConst.JPOS_PN_DISABLED;
        powerState = JposConst.JPOS_PS_UNKNOWN;
        state = JposConst.JPOS_S_ERROR;

        // Read and check parameters
        portName = jposEntry.getProp("portName").getValueAsString();
        int baudRate = Integer.valueOf(jposEntry.getProp("baudRate").getValueAsString());

        // Try find existen serial port
        for (DeviceService value : deviceServices.keySet()) {
            if (value.getPortName().equalsIgnoreCase(portName)) {
                port = deviceServices.get(value);
                break;
            }
        }
        // Open port
        if (port == null) {
            try {
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
                CommPort commPort = portIdentifier.open("OVER", PORT_OPEN_TIMEOUT);
                if (commPort instanceof SerialPort) {
                    port = ((SerialPort) commPort);
                    port.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    port.setDTR(true);
                    port.setRTS(true);
                    port.setInputBufferSize(SIZE_BUFFER);
                    port.setOutputBufferSize(SIZE_BUFFER);
                } else {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE,
                            getErrorDescription(JposConst.JPOS_E_NOSERVICE));
                }
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
        if (!claimed) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        // If the DeviceEnabled property is true, then the device is disabled.
        try {
            if (deviceEnabled) {
                setDeviceEnabled(false);
            }
        } catch (JposException e) {
        }
        // Release claimed state and notify waiting monitor
        Object monitor = port;
        synchronized (monitor) {
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
     * counter of code where interruption by an event is not desirable.
     *
     * @param bln
     * @throws JposException
     */
    public void setFreezeEvents(boolean bln) throws JposException {
        synchronized (eventQueue) {
            frozeEvents = bln;
            if (!frozeEvents) {
                eventQueue.notify();
            }
        }
    }

    /**
     * Called when the JposServiceConnection is disconnected (i.e. service is closed)
     * @since 1.2 (NY2K meeting)
     */
    public void deleteInstance() throws JposException {
    }

    /**
     * Return an identifier for the UnifiedPOS Service and the company that
     * produced it.
     * @return identifier
     * @throws JposException
     */
    public String getDeviceServiceDescription() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_DESCRIPTION_PROP_NAME).getValueAsString();
    }

    /**
     * Return the UnifiedPOS Service version number.
     *
     * Three version levels are specified, as follows:
     * Major - The вЂњmillionsвЂќ place.
     * Minor - The вЂњthousandsвЂќ place.
     * Build - The вЂњunitsвЂќ place.пЂ 
     * @return
     * @throws JposException
     */
    public int getDeviceServiceVersion() throws JposException {
        return SERVICE_VERSION;
    }

    /**
     * Return an identifier for the physical device.
     * @return
     * @throws JposException
     */
    public String getPhysicalDeviceDescription() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_DESCRIPTION_PROP_NAME).getValueAsString();
    }

    /**
     * Return a short name identifying the physical device. This is a short
     * version of PhysicalDeviceDescription and should be limited to 30
     * characters.
     * @return
     * @throws JposException
     */
    public String getPhysicalDeviceName() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_NAME_PROP_NAME).getValueAsString();
    }

    //--------------------------------------------------------------------------
    // Package methods
    //
    /**
     * Allows the JposServiceInstanceFactory to set the JposEntry associated with
     * this DeviceService.  Subclasses can access the JposEntry with getter
     */
    void setJposEntry(JposEntry entry) {
        jposEntry = entry;
    }

    /**
     * @return the JposEntry object associated with this DeviceService
     */
    JposEntry getJposEntry() {
        return jposEntry;
    }

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
     * Enqueue event.
     * @param event
     */
    protected void fireEvent(JposEvent event) {
        if (deviceEnabled) {
            if (dataEventEnabled
                    || (!(event instanceof DataEvent))
                    || (!(event instanceof ErrorEvent))) {
                synchronized (eventQueue) {
                    eventQueue.add(event);
                    eventQueue.notify();
                }
            } else {
                eventStore.add(event);
            }
        }
    }

    /**
     * Get status description
     * @return State description
     */
    protected String getStatusDescription(int status) {
        switch (status) {
            case RC_OK:
                return "Command correctly executed";
            case RC_WRONGSEQ:
                return "Offsequence command";
            case RC_WRONGPARAM:
                return "Parameters error";
            case RC_ERRLKEY:
                return "Encryption key loading incorrect";
            case RC_ERRLDTBL:
                return "DES table loading incorrect";
            case RC_ERRWRAM:
                return "RAM writing incorrect";
            case RC_ERRPIN:
                return "PIN input incorrect";
            case RC_NOKEY:
                return "No encryption key";
            case RC_NODTBL:
                return "No DES table";
            case RC_NORAM:
                return "User RAM not initialised";
            case RC_NOMOREKEYS:
                return "PIN pad can perform no more DUKPT encryptions (KSN space finished)";
            case RC_WRONGPIN:
                return "PIN incorrect";
            case RC_WRONGMAC:
                return "MAC incorrect";
            case RC_WRONGKEY:
                return "The correctness of the DUKPT initial key is not verified, after the match with ‘CHKVALUE’";
            case RC_KBDTIMEOUT:
                return "Acquisition timeout, no character";
            case RC_KEYTOBEEND:
                return "Manual encryption key partially loaded, to be completed with the second half-key";
            case RC_EMVBADCRTLENGTH:
                return "The EMV certificate has a length different from the length of the CA Public Key Modulus.";
            case RC_EMVBADTRAILER:
                return "The EMV certificate has a bad Data Trailer.";
            case RC_EMVBADHEADER:
                return "The EMV certificate has a bad Data Header.";
            case RC_EMVBADFORMAT:
                return "The EMV certificate has a bad Certificate Format.";
            case RC_EMVBADHASHALG:
                return "The EMV certificate has a Hash Algorithm Indicator not supported.";
            case RC_EMVBADHASH:
                return "The EMV certificate Hash Result check failed.";
            case RC_EMVBADPKALG:
                return "The EMV certificate has a Public Key Algorithm Indicator not supported.";
            case RC_EMVBADKEYDATA:
                return "Bad data format.";
            case RC_WRONGPSWD:
                return "Wrong Password";
            case RC_BADPSWD:
                return "Bad password typed";
            case RC_USERABORT:
                return "If user types Abort key";
            case RC_LOCKED:
                return "If the command is temporary locked";
            case RC_WRONGSIGN:
                return "If the RSA key signature is wrong";
            case RC_INVALIDKEY:
                return "If the key is not valid";
            default:
                return "Unknwon status";
        }
    }

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
        StringBuilder s = new StringBuilder(2 * len);
        for (int i = 0; i < len; i++) {
            String h = Integer.toHexString(data[i + ofs]).toUpperCase();
            if (h.length() < 2) {
                s.append('0');
            }
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
                            if (event == null) {
                                break;
                            }
                            try {
                                if (eventCallbacks != null) {
                                    if (event instanceof DataEvent) {
                                        eventCallbacks.fireDataEvent(
                                                (DataEvent) event);
                                    } else if (event instanceof ErrorEvent) {
                                        eventCallbacks.fireErrorEvent(
                                                (ErrorEvent) event);
                                    } else if (event instanceof DirectIOEvent) {
                                        eventCallbacks.fireDirectIOEvent(
                                                (DirectIOEvent) event);
                                    } else if (event instanceof OutputCompleteEvent) {
                                        eventCallbacks.fireOutputCompleteEvent(
                                                (OutputCompleteEvent) event);
                                    } else if (event instanceof StatusUpdateEvent) {
                                        eventCallbacks.fireStatusUpdateEvent(
                                                (StatusUpdateEvent) event);
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    synchronized (eventQueue) {
                        eventQueue.wait();
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Event comparator class
     */
    private class EventComparator implements Comparator<JposEvent> {

        public int compare(JposEvent o1, JposEvent o2) {
            if (o1.getSequenceNumber() < o2.getSequenceNumber()) {
                return -1;
            } else {
                return 1;
            }
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
                poll();
            } catch (JposException e) {
            }
        }
    }

    /**
     * It gives back an integer number the value of which is the CRC. 
     * @param buf Data buffer
     * @param num Data number
     * @return
     */
    private int crc(int[] buf, int start, int end) {
        int a, b, c, d;
        b = 0xFF;
        c = 0xFF;
        for (int i = start; i < end; i++) {
            a = buf[i];
            a ^= b;
            a ^= a >> 4;
            d = a;
            a = (a << 4) & 0xFF;
            b = a ^ c;
            c = ((a << 1) ^ d) & 0xFF;
            b ^= (d >> 3) & 0x1F;
        }
        return ((b << 8) | c);
    }

    /**
     * Execute device command
     * @param data send data
     * @return response
     */
    protected int[] execute(String cmd, int[] data) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                // Create command
                int commandCount = 0;
                // Data beginning character
                command[commandCount] = STX_CHAR;
                commandCount++;
                // Message dimension
                int dim = 11;
                if (data != null) {
                    dim += data.length;
                }
                command[commandCount] = dim & 0xFF;
                commandCount++;
                command[commandCount] = dim >> 8;
                commandCount++;
                // Message counter
                counter++;
                if (counter > 127) {
                    counter = 0;
                }
                command[commandCount] = counter;
                commandCount++;
                // Command
                for (int i = 0; i < 4; i++) {
                    command[commandCount + i] = cmd.charAt(i);
                }
                commandCount = commandCount + 4;
                // Data fieild
                if (data != null) {
                    for (int i = 0; i < data.length; i++) {
                        command[commandCount + i] = data[i];
                    }
                    commandCount = commandCount + data.length;
                }
                // Control code
                int crc = crc(command, 0, commandCount);
                command[commandCount] = crc & 0xFF;
                commandCount++;
                command[commandCount] = crc >> 8;
                commandCount++;
                // Data end character
                command[commandCount] = ETX_CHAR;
                commandCount++;
                int[] result = null;
                boolean done = false;
                // Execute command
                while (executed) {
                    monitor.wait();
                }
                executed = true;
                try {
                    int repeatCount = 0;
                    while ((!done) && (repeatCount < MAX_RETRY)) {
                        repeatCount++;
                        // Clear input stream
                        port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                        try {
                            while (is.read() > -1) {
                            }
                        } finally {
                            port.disableReceiveTimeout();
                        }
                        // Preamble
                        for (int i = 0; i < 3; i++) {
                            os.write(SYNC_CHAR);
                        }
                        // Transmit data to output stream
                        for (int i = 0; i < commandCount; i++) {
                            os.write(command[i]);
                        }
                        os.flush();
                        System.out.println(dataToHex(command, 0, commandCount));
                        // Receive response
                        long start = System.currentTimeMillis();
                        port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                        try {
                            while ((!done)
                                    && ((System.currentTimeMillis() - start) < RESPONSE_TIMEOUT)) {
                                // monitor.wait(INTER_BYTE_TIMEOUT);
                                // Preamble
                                if (is.read() != SYNC_CHAR) {
                                    continue;
                                }
                                if (is.read() != SYNC_CHAR) {
                                    continue;
                                }
                                if (is.read() != SYNC_CHAR) {
                                    continue;
                                }
                                int responseCount = 0;
                                // Data beginning character
                                if ((response[responseCount] = is.read()) != STX_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // Data beginning character
                                if ((response[responseCount] = is.read()) != STX_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // Message dimension
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                int resultCount = response[responseCount - 2]
                                        + (response[responseCount - 1] << 8) - 11;
                                // Message counter
                                if ((response[responseCount] = is.read()) != (counter + 128)) {
                                    continue;
                                }
                                responseCount++;
                                // Command
                                if ((response[responseCount] = is.read()) != cmd.charAt(0)) {
                                    continue;
                                }
                                if ((response[responseCount] = is.read()) != cmd.charAt(1)) {
                                    continue;
                                }
                                if ((response[responseCount] = is.read()) != cmd.charAt(2)) {
                                    continue;
                                }
                                if ((response[responseCount] = is.read()) != cmd.charAt(3)) {
                                    continue;
                                }
                                responseCount = responseCount + 4;
                                if (resultCount > 0) {
                                    result = new int[resultCount];
                                    int ret = -1;
                                    for (int i = 0; i < resultCount; i++) {
                                        if ((ret = is.read()) < 0) {
                                            break;
                                        }
                                        response[responseCount + i] = ret;
                                        result[i] = ret;
                                    }
                                    if (ret < 0) {
                                        continue;
                                    }
                                    responseCount = responseCount + resultCount;
                                } else {
                                    result = null;
                                }
                                // CRC
                                crc = crc(response, 0, responseCount);
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if (crc == ((response[responseCount - 1] << 8)
                                        | response[responseCount - 2])) {
                                    done = true;
                                }
                            }
                        } finally {
                            port.disableReceiveTimeout();
                        }
                    }
                } finally {
                    executed = false;
                    monitor.notify();
                }
                if (!done) {
                    throw new JposException(JposConst.JPOS_E_TIMEOUT,
                            getErrorDescription(JposConst.JPOS_E_TIMEOUT));
                }
                // Check result status
                if (result != null) {
                    status = getWord(result, 0);
                    if (status != RC_OK) {
                        throw new JposException(JposConst.JPOS_E_FAILURE,
                                getStatusDescription(status));
                    }
                }
                // Return result
                return result;
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        }
    }

    /**
     * Get word from array
     * @param data
     * @param ofs
     * @return
     * @throws JposException
     */
    protected int getWord(int[] data, int ofs) throws JposException {
        if (data.length > ofs + 1) {
            return data[ofs] | (data[ofs + 1] << 8);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Set word to array
     * @param data
     * @param ofs
     * @param word
     * @throws JposException
     */
    protected void setWord(int[] data, int ofs, int word) throws JposException {
        if (data.length > ofs + 1) {
            data[ofs] = word & 0xFF;
            data[ofs + 1] = word >> 8;
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
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
    protected SerialPort port = null;
    protected InputStream is = null;
    protected OutputStream os = null;
    protected String portName = null;
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
    private int command[] = new int[SIZE_BUFFER];
    private int response[] = new int[SIZE_BUFFER];
    private int counter = -1;
    private boolean executed = false;
    //--------------------------------------------------------------------------
    // Constants
    //
    /**
     * command max size
     */
    protected static final int SIZE_BUFFER = 1024;
    /**
     * Port open timeout
     */
    private static final int PORT_OPEN_TIMEOUT = 10000;
    /**
     * Service version
     */
    private static final int SERVICE_VERSION = 1013000;
    /**
     * Preamble
     */
    private static final char SYNC_CHAR = 0x16;
    /**
     * Data beginning character
     */
    private static final char STX_CHAR = 0x02;
    /**
     * Data end character
     */
    private static final char ETX_CHAR = 0x03;
    /**
     * Retrive count for executing
     */
    private static final int MAX_RETRY = 3;
    /**
     * Status polling interval
     */
    protected static final int POLL_INTERVAL = 2000;
    /**
     * Response timeout
     */
    protected static final int RESPONSE_TIMEOUT = 10000;
    /**
     * Max interval between two bytes transmition
     */
    protected static final int INTER_BYTE_TIMEOUT = 50;
    /**
     * OVER EPP reply codes
     */
    protected static final int RC_OK = 0x0000; // Command correctly executed
    protected static final int RC_WRONGSEQ = 0x0001; // Offsequence command
    protected static final int RC_WRONGPARAM = 0x0002; // Parameters error
    protected static final int RC_ERRLKEY = 0x0010; // Encryption key loading incorrect
    protected static final int RC_ERRLDTBL = 0x0011; // DES table loading incorrect
    protected static final int RC_ERRWRAM = 0x0012; // RAM writing incorrect
    protected static final int RC_ERRPIN = 0x0013; // PIN input incorrect
    protected static final int RC_NOKEY = 0x0020; // No encryption key
    protected static final int RC_NODTBL = 0x0021; // No DES table
    protected static final int RC_NORAM = 0x0024; // User RAM not initialised
    protected static final int RC_NOMOREKEYS = 0x0026; // PIN pad can perform no more DUKPT encryptions (KSN space finished)
    protected static final int RC_WRONGPIN = 0x0030; // PIN incorrect
    protected static final int RC_WRONGMAC = 0x0031; // MAC incorrect
    protected static final int RC_WRONGKEY = 0x0032; // The correctness of the DUKPT initial key is not verified, after the match with ‘CHKVALUE’
    protected static final int RC_UNAUTH = 0x0032;
    protected static final int RC_KBDTIMEOUT = 0x0040; // Acquisition timeout, no character
    protected static final int RC_KEYTOBEEND = 0x0041; // Manual encryption key partially loaded, to be completed with the second half-key
    protected static final int RC_EMVBADCRTLENGTH = 0x0051; // The EMV certificate has a length different from the length of the CA Public Key Modulus.
    protected static final int RC_EMVBADTRAILER = 0x0052; // The EMV certificate has a bad Data Trailer.
    protected static final int RC_EMVBADHEADER = 0x0053; // The EMV certificate has a bad Data Header.
    protected static final int RC_EMVBADFORMAT = 0x0054; // The EMV certificate has a bad Certificate Format.
    protected static final int RC_EMVBADHASHALG = 0x0055; // The EMV certificate has a Hash Algorithm Indicator not supported.
    protected static final int RC_EMVBADHASH = 0x0056; // The EMV certificate Hash Result check failed.
    protected static final int RC_EMVBADPKALG = 0x0057; // The EMV certificate has a Public Key Algorithm Indicator not supported.
    protected static final int RC_EMVBADKEYDATA = 0x0058; // Bad data format.
    protected static final int RC_WRONGPSWD = 0x0060; // Wrong Password
    protected static final int RC_BADPSWD = 0x0061; // Bad password typed
    protected static final int RC_USERABORT = 0x0062; // If user types Abort key
    protected static final int RC_LOCKED = 0x0063; // If the command is temporary locked
    protected static final int RC_WRONGSIGN = 0x0064; // If the RSA key signature is wrong
    protected static final int RC_INVALIDKEY = 0x0066; // If the key is not valid
    /**
     * OVER EPP states
     */
    protected static final int ST_IDLE = 0x0001; // Idle
    protected static final int ST_SESSION = 0x0002; // Open session
    protected static final int ST_IMKEY = 0x0003; // Input Master Key
    protected static final int ST_IPIN = 0x0004; // Input PIN
    protected static final int ST_IPSWD = 0x0006; // Input password
    /**
     * OVER EPP contexts
     */
    protected static final int CT_OPERATIVE = 0x0000; // Operative context
    protected static final int CT_MODIFYPSWD = 0x0001; // Modify password context
    protected static final int CT_SSTACCESS = 0x0002; // Sensitive State access context
    protected static final int CT_SENSITIVESTATE = 0x0003; // Sensitive State Context
    /**
     * OVER EPP key loading modes
     */
    protected static final int EPPLK_LINE_KK = 11; // T-DES double length via software
    protected static final int EPPLK_FXORPART_KK = 14; // T-DES double length 32^32 key, first component.
    protected static final int EPPLK_SXORPART_KK = 15; // T-DES double length 32^32 key, second component.
    protected static final int EPPLK_LINE_K3 = 21; // T-DES triple length via software
    protected static final int EPPLK_FXORPART_K3 = 25; // T-DES double length 48^48 key, first component.
    protected static final int EPPLK_SXORPART_K3 = 26; // T-DES double length 48^48 key, second component.
    /**
     * OVER EPP key utilizations
     */
    protected static final int KU_PINBLOCK = 0x0010; // Enciphering of PIN Blocks
    protected static final int KU_PINLOCAL = 0x0008; // Local checking of PINs
    protected static final int KU_KEYENCKEY = 0x0001; // Enciphering if other keys
    protected static final int KU_MACING = 0x0004; // Generation of MAC codes
    protected static final int KU_CRYPT = 0x0002; // Enciphering of generic data buffers
    /**
     * OVER EPP V-DEM capabilities
     */
    protected static final int C_EXT = 0x0001; // Keyboard present
    protected static final int C_TRIPLEDES = 0x0080; // Triple DES managed
    protected static final int C_DUKPT = 0x0200; // DUKTP managed
    protected static final int C_RSA = 0x0400; // RSA managed
    protected static final int C_TRIPLEDES48 = 0x1000; // Triple DES 48 managed
    /**
     * ISO PIN BLOCK formats
     */
    protected static final int PB_ANSI = 0x0004; // Pin block ANSI
    protected static final int PB_ISO_0 = 0x0000; // Pin block ISO 0
    protected static final int PB_ISO_1 = 0x0001; // Pin block ISO 1
    protected static final int PB_ISO_3 = 0x0007; // Pin block ISO 3
    protected static final int PB_ECI_2 = 0x0005; // Pin block ECI 2
    protected static final int PB_ECI_3 = 0x0003; // Pin block ECI 3
    protected static final int PB_IBM_3624 = 0x0002; // Pin block IBM 3624
    /**
     * Input data formats
     */
    protected static final int F_ISO = 0x0000; // Data in the ISO format
    protected static final int F_HEX = 0x0001; // Data in the HEX format
    /**
     * Certificate types
     */
    protected static final int CRT_ISSUER = 0x0001; // Issuer Public Certificate
    protected static final int CRT_ICC = 0x0002; // ICC Public  Certificate
    protected static final int CRT_ICCPIN = 0x0003; // ICC PIN Public Certificate
    /**
     * Operator’s passwords
     */
    protected static final int OPPSWD_1 = 0x0001; // Operator 1
    protected static final int OPPSWD_2 = 0x0002; // Operator 2
    /**
     * Types of entered password
     */
    protected static final int FP_FIRSTINS = 0x0001; // First password input
    protected static final int FP_SECONDINS = 0x0002; // Second password input
    /**
     * Encryption algorithms
     */
    protected static final int ALG_TDES = 0; // T-DES algorithm
    protected static final int ALG_RSA = 1; // RSA algorithm
    /**
     * CA Public Key importation formats
     */
    protected static final int EMVPKCAVDEM_EMV = 1; // EMV Verification Data
    /**
     * Customisation options
     */
    protected static final int OPTVDEM_SIGNPK = 0x0001; // If enabled this options requires that the EMV public keys of CA must be signed before using them for ICC certificate verification.
    /**
     * MAC algorithms
     */
    protected static final int MACALGVDEM_ISO9807 = 0x0001; // MAC ISO 9807 (ANSI X9.19)
    /**
     * MAC buffers
     */
    protected static final int MACBFVDEM_FIRST = 0x0001; // First buffer of the chain
    protected static final int MACBFVDEM_LAST = 0x0002; // Last buffer of the chain
    /**
     * KCV calculation modes
     */
    protected static final int KCVVDEM_ZERO = 0x0001; // KCV created by an encryption of the key with zero
    /**
     * Keyboard scan-code
     */
    protected static final int SC_NOKEY = 0x0000; // Special value for “no key”.
    protected static final int SC_MASKED = 0x00FF; // Special value for “masked key”.
}

