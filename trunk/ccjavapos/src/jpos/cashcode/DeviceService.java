/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.cashcode;

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
import javax.comm.SerialPort;
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

/**
 * Base service for CashCode Peripheral devices
 * @author Maxim
 */
public abstract class DeviceService implements BaseService, JposServiceInstance {

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
    public void directIO(int command, int[] data, Object obj) throws JposException {
        int response[];
        if (obj instanceof String) {
            response = execute(command, hexToData(obj.toString()));
        } else if (obj instanceof StringBuilder) {
            StringBuilder s = (StringBuilder)obj;
            response = execute(command, hexToData(s.toString()));
            s.replace(0, s.length()-1, dataToHex(response, 0, response.length));
        } else {
            response = execute(command, null);
        }
        if ((response.length == 1) & (data != null))
            data[0] = response[0];
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
    public void open(String logicalDeviceName, EventCallbacks ec) throws JposException {

        // Check port opened
        if ((state != JposConst.JPOS_S_CLOSED) && (port != null))
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        
        powerNotify = JposConst.JPOS_PN_DISABLED;
        powerState = JposConst.JPOS_PS_UNKNOWN;

        // Read and check parameters
        //if (!jposEntry.getProp("deviceBus").getValueAsString().equalsIgnoreCase("RS232"))
        //    throw new JposException(JposConst.JPOS_E_NOSERVICE,
        //            getErrorDescription(JposConst.JPOS_E_NOSERVICE));
        portName = jposEntry.getProp("portName").getValueAsString();
        int baudRate = Integer.valueOf(jposEntry.getProp("baudRate").getValueAsString());
        //if (!(jposEntry.getProp("dataBits").getValueAsString().equalsIgnoreCase("8") &&
        //        jposEntry.getProp("stopBits").getValueAsString().equalsIgnoreCase("1") &&
        //        jposEntry.getProp("parity").getValueAsString().equalsIgnoreCase("None")))
        //    throw new JposException(JposConst.JPOS_E_NOSERVICE,
        //            getErrorDescription(JposConst.JPOS_E_NOSERVICE));

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
                CommPort commPort = portIdentifier.open("CASHCODE", DeviceTiming.NON_RESPONSE);
                if (commPort instanceof SerialPort) {
                    port = ((SerialPort)commPort);
                    port.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    // Init timing
                    responseTime = System.currentTimeMillis();
                    executeTime = responseTime + DeviceTiming.BUS_RESET;
                    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    port.setDTR(true);
                    port.setRTS(true);
                    port.setInputBufferSize(SIZE_BUFFER);
                    port.setOutputBufferSize(SIZE_BUFFER);
                    port.enableReceiveTimeout(DeviceTiming.INTER_BYTE);
                } else
                    throw new JposException(JposConst.JPOS_E_NOSERVICE,
                            getErrorDescription(JposConst.JPOS_E_NOSERVICE));
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_NOSERVICE,
                        getErrorDescription(JposConst.JPOS_E_NOSERVICE), e);
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

        // Initialize device
        try {
            initialize();
            state = JposConst.JPOS_S_IDLE;
        } catch (JposException e) {
            state = JposConst.JPOS_S_ERROR;
            startInit(DeviceTiming.RESET_NON_RESPONSE);
        }
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
            // Initialize device
            if (state != JposConst.JPOS_S_IDLE) {
                try {
                    initialize();
                    state = JposConst.JPOS_S_IDLE;
                } catch (JposException e) {
                    state = JposConst.JPOS_S_ERROR;
                    startInit(DeviceTiming.RESET_NON_RESPONSE);
                }
            }
            deviceEnabled = true;
            eventThread = new Thread(new EventRunner());
            eventThread.start();
            if (initTimer == null)
                startPolling();
        } else {
            deviceEnabled = false;
            stopPolling();
            stopInit();
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
     * Get port name
     * @return
     */
    public String getPortName() {
        return portName;
    }

    //--------------------------------------------------------------------------
    // Public constants
    //

    /**
     * Direct IO device status changed
     */
    public static final int DIRECTIO_STATUS_CHANGED = 0x00;

    /**
     * Direct IO trace execute command
     */
    public static final int DIRECTIO_EXECUTE_COMMAND = 0x01;

    /**
     * Direct IO trace output message event
     */
    public static final int DIRECTIO_TRACE_OUT = 0x02;

    /**
     * Direct IO trace input message event
     */
    public static final int DIRECTIO_TRACE_IN = 0x03;


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

    /**
     * Task for next reset command after RESET_NON_RESPONSE
     */
    private class InitTask extends TimerTask {

        /**
         * Implements run task
         */
        public void run() {
            try {
                initialize();
                stopInit();
                if (deviceEnabled)
                    startPolling();
                state = JposConst.JPOS_S_IDLE;
            } catch (JposException e) {
                state = JposConst.JPOS_S_ERROR;
            }
        }

    }

    /*--------------------------------------------------------------------------
     * Private and protected internal methods
     */

    /**
     * Before exiting, an application should close all open JavaPOS Devices to
     * free device resources in a timely manner, rather than relying on the Java
     * garbage collection mechanism to free resources at some indeterminate time
     * in the future.
     */
    @Override
    protected void finalize() {
        try {
            close();
        } catch (JposException e) {}
    }

    /**
     * Get peripheral address code
     * @return Peripheral address
     */
    protected abstract int getAddress();

    /**
     * Wait and receive data
     * @param timeout Waiting time in millisecond
     * @return Data byte
     * @throws JposException
     */
    private int waitReceive(int timeout) throws JposException {
        Object notifier = new Object();
        try {
            synchronized(notifier) {
                long start = System.currentTimeMillis();
                while (true) {
                    int data = is.read();
                    if (data >= 0)
                        return data;
                    if ((System.currentTimeMillis() - start) > timeout)
                        break;
                    notifier.wait(DeviceTiming.INTER_BYTE);
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

    private void clearReceive() throws JposException {
        try {
            while (is.read() > -1) {}
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
    }

    /**
     * Error detection CRC method
     * CRC - CCITT using whole byte shifting into a two-byte frame
     * P(X) = X16 + X12 + X5 + l
     * @param bufData Object section to be from and including SYNC to end of DATA
     * @param sizeData Length from and including SYNC to end of DATA
     * @return Calculated CRC
     */
    private int getCRC16(int bufData[], int sizeData) {
        final int POLYNOMIAL = 0x08408;
        int TmpCRC, CRC, i;
        int j;
        CRC = 0;
        for (i=0; i < sizeData; i++) {
            TmpCRC = CRC ^ bufData[i];
            for (j=0; j < 8; j++) {
                if ((TmpCRC & 0x0001) != 0) {
                    TmpCRC >>= 1;
                    TmpCRC ^= POLYNOMIAL;
                } else
                    TmpCRC >>= 1;
            }
            CRC = TmpCRC;
        }
        return CRC;
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
        StringBuilder s = new StringBuilder(2*len);
        for (int i = 0; i < len; i++) {
            String h = Integer.toHexString(data[i + ofs]).toUpperCase();
            if (h.length() < 2)
                s.append('0');
            s.append(h);
        }
        return s.toString();
    }

    /**
     * Convert binary data to ASCII string
     * @param data  Data array
     * @param ofs   Offset
     * @param len   Length
     * @return  ASCII string
     */
    protected String dataToAscii(int data[], int ofs, int len) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char ch = (char)data[i + ofs];
            if (ch > 0)
                s.append(ch);
            else
                break;
        }
        return s.toString();
    }

    /**
     * Send message from Controller to Peripheral
     * @param command Command code
     * @param data Command data
     * @throws JposException Peripheral transmit error
     */
    private void send(int command, int data[]) throws JposException {
        int subcommand = 0;
        if (command >= 0x100) {
            command = command >> 8;
            subcommand = command & 0xFF;
        }
        // SYNC Message transmission start code [02H], fixed
        int index = 0;
        buffer[index] = SYNC_DATA;
        index++;
        // ADR Peripheral address
        buffer[index] = getAddress();
        index++;
        // LNG Data length (Total number of bytes including SYNC and CRC)
        int sizeData = 6;
        if (subcommand != 0)
            sizeData++;
        if (data != null)
            sizeData = sizeData + data.length;
        // if a package cannot be fitted into 250-byte frame a wider frame may
        // be used by setting LNG to 0; the actual packet length is inserted
        // into DATA block bytes 0 and 1
        if (sizeData > 255) {
            sizeData = sizeData + 2;
            buffer[index] = 0;
        } else
            buffer[index] = sizeData;
        index++;
        // CMD Command
        buffer[index] = command;
        index++;
        if (subcommand != 0) {
            buffer[index] = subcommand;
            index++;
        }
        // two-int LNG always follows MSB first
        if (sizeData > 255) {
            // LNG HIGH
            buffer[index] = (sizeData >> 8);
            index++;
            // LNG LOW
            buffer[index] = (sizeData & 0xFF);
            index++;
        }
        // DATA Data necessary for command (omitted if not required by CMD)
        if (data != null)
            for (int i = 0; i < data.length; i++) {
                buffer[index] = data[i];
                index++;
            }
        // CRC Check code by CRC method, LSB first
        // Object section to be from and including SYNC to end of DATA
        // (Initial value  = 0)
        int crc = getCRC16(buffer, sizeData - 2);
        buffer[index] = (crc & 0xFF);
        index++;
        buffer[index] = (crc >> 8);
        index++;
        // Fire DirectIOEvent to trace output
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                DIRECTIO_TRACE_OUT, command, dataToHex(buffer, 0, sizeData)));
        // Send message to serial port
        try {
            for (int i = 0; i < sizeData; i++)
                os.write(buffer[i]);
            os.flush();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
    }

    /**
     * Receive message from Peripheral to Controller
     * @return Received data length
     * @throws JposException Peripheral transmit error
     */
    private int[] receive() throws JposException {
        // SYNC Message transmission start code [02H], fixed
        int index = 0;
        buffer[index] = waitReceive(DeviceTiming.RESPONSE);
        if (buffer[index] != SYNC_DATA)
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        index++;
        // ADR Peripheral address
        buffer[index] = waitReceive(DeviceTiming.INTER_BYTE);
        if (buffer[index] != getAddress())
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        index++;
        // LNG Data length (Total number of bytes including SYNC and CRC)
        buffer[index] = waitReceive(DeviceTiming.INTER_BYTE);
        int sizeData = buffer[index];
        index++;
        // if a package cannot be fitted into 250-byte frame a wider frame may
        // be used by setting LNG to 0;
        if (sizeData == 0) {
            buffer[index] = waitReceive(DeviceTiming.INTER_BYTE);
            sizeData = buffer[index];
            index++;
            buffer[index] = waitReceive(DeviceTiming.INTER_BYTE);
            sizeData = sizeData << 8 | buffer[index];
            index++;
        }
        // Read message to the end
        for (int i = index; i < sizeData; i++)
            buffer[i] = waitReceive(DeviceTiming.INTER_BYTE);

        // Data necessary for command (omitted if not required by CMD)
        int len = sizeData - index - 2;
        int[] data = null;
        if (len > 0) {
            data = new int[len];
            for (int i = 0; i < len; i++) {
                data[i] = buffer[index];
                index++;
            }
        }

        // CRC Check code by CRC method, LSB first
        int crc = buffer[index];
        index++;
        crc = (buffer[index] << 8) | crc;
        index++;
        if (crc != getCRC16(buffer, sizeData - 2))
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));

        // Fire DirectIOEvent to trace input
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                DIRECTIO_TRACE_IN, 0, dataToHex(buffer, 0, sizeData)));
        // return data
        return data;
    }

    /**
     * Make pause during free time from previous commmand
     * @throws JposException Thread interrupted
     */
    protected void pause(long timeout) throws JposException {
        Object notifier = new Object();
        try {
            synchronized(notifier) {
                if (timeout > 0)
                    notifier.wait(timeout);
            }
        } catch (InterruptedException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Executing command on Peripheral.
     * @param command Command code
     * @param data Command data
     * @return Received data 
     * @throws JposException Peripheral transmit error
     */
    protected int[] execute(int command, int data[]) throws JposException {
        // Check opened port
        Object monitor;
        if (port != null)
            monitor = port;
        else
            throw new JposException(JposConst.JPOS_E_CLOSED,
                    getErrorDescription(JposConst.JPOS_E_CLOSED));
        // Fire DirectIOEvent to trace executing
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                DIRECTIO_EXECUTE_COMMAND, command, getCommandDescription(command)));
        int retryCount = 0;
        while (true) try {
            synchronized (monitor) {
                // Check executed
                while (executed)
                    monitor.wait();
                executed = true;
                int[] response = null;
                try {
                    // The tfree must be obeyed by the Controller between the end of any
                    // ACK or NAK confirmation response and start of the next command transmission.
                    pause(DeviceTiming.FREE - (System.currentTimeMillis() - executeTime));

                    // Clear serial port buffer
                    clearReceive();

                    // Send message
                    send(command, data);

                    // Last command time + timeout, set even no response
                    executeTime = System.currentTimeMillis() + DeviceTiming.RESPONSE;

                    try {
                        // Receive message
                        response = receive();
                    } catch (JposException e) {
                        // Send NAK message
                        send(NAK_DATA, null);
                        // Last execute time
                        executeTime = System.currentTimeMillis();
                        throw e;
                    }

                    // Last response time
                    responseTime = System.currentTimeMillis();

                    // Analyze response
                    boolean completed = false;
                    if (response.length == 1) {
                        switch (response[0]) {
                            case ACK_DATA:
                                completed = true;
                                break;
                            case NAK_DATA:
                                throw new JposException(JposConst.JPOS_E_FAILURE,
                                    getErrorDescription(JposConst.JPOS_E_FAILURE));
                            case ILLEGAL_COMMAND:
                                throw new JposException(JposConst.JPOS_E_ILLEGAL,
                                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
                        }
                    }

                    // Send ACK from Controller to Peripheral
                    if (!completed)
                        send(ACK_DATA, null);

                    // Last execute time
                    executeTime = System.currentTimeMillis();
                } finally {
                    executed = false;
                    monitor.notify();
                }
                // Return data
                return response;
            }
        } catch (InterruptedException e) {
            throw new JposException(JposConst.JPOS_E_TIMEOUT,
                getErrorDescription(JposConst.JPOS_E_TIMEOUT));
        } catch (JposException e) {
            retryCount++;
            if (retryCount < MAX_RETRY) 
                pause(DeviceTiming.REPEAT_PAUSE);
            else
                throw e;
        }
    }

    /**
     * Get poll command code
     * @return Poll command code
     */
    protected abstract int getPollCommand();

    /**
     * Get reset command code
     * @return Reset command code
     */
    protected abstract int getResetCommand();

    /**
     * Get initialize status code
     * @return Initialize state code
     */
    protected abstract int getInitStatus();

    /**
     * Get status description
     * @return State description
     */
    protected abstract String getStatusDescription(int state);

    /**
     * Get command description
     * @return State description
     */
    protected abstract String getCommandDescription(int command);

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
     * Power state changed event
     */
    protected abstract void powerStateChanged();

    /**
     * Set current power state
     */
    protected void setPowerState(int powerState) {
        if (powerState != this.powerState) {
            this.powerState = powerState;
            powerStateChanged();
        }
    }

    /**
     * Peripheral status changed event
     */
    protected void statusChanged() {
        // Process state changed
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                DIRECTIO_STATUS_CHANGED, status,
                getStatusDescription(status)));
    }

    /**
     * Refresh current state by executing poll command
     * @return Peripheral state
     */
    protected void poll() throws JposException {
        int statusData[];
        try {
            statusData = execute(getPollCommand(), null);
            if (state == JposConst.JPOS_S_BUSY)
                state = JposConst.JPOS_S_IDLE;
        } catch (JposException e) {
            if (state == JposConst.JPOS_S_IDLE)
                state = JposConst.JPOS_S_BUSY;
            // If the Peripheral has not responded to a poll for its maximum
            // non-response time, the Controller must continue to poll the
            // Peripheral at least every ten seconds with a RESET command.
            if ((isPolling()) &&
                    ((System.currentTimeMillis() - responseTime) > DeviceTiming.NON_RESPONSE)) {
                setPowerState(JposConst.JPOS_PS_OFF_OFFLINE);
                state = JposConst.JPOS_S_ERROR;
                startInit(DeviceTiming.RESET_NON_RESPONSE);
            }
            throw e;
        }
        int newStatus;
        switch (statusData.length) {
            case 2:
                // Set two-bytes state
                newStatus = (statusData[0] << 8) | statusData[1];
                break;
            case 1:
                // Set one-byte state
                newStatus = statusData[0] << 8;
                break;
            default:
                // Invalid response - poll response must have 1 or 2 bytes date length
                throw new JposException(JposConst.JPOS_E_FAILURE, 
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        if (status != newStatus) {
            status = newStatus;
            statusChanged();
        }
    }

    /**
     * Reset peripheral by execute reset command
     * If communication error next reset scheduled on 10 sec.
     */
    protected void reset() throws JposException {
        execute(getResetCommand(), null);
    }

    /**
     * Initialize peripheral object
     * @throws JposException
     */
    protected void initialize() throws JposException {
        status = 0;
        setPowerState(JposConst.JPOS_PS_OFF_OFFLINE);
        reset();
        setPowerState(JposConst.JPOS_PS_ONLINE);
        waitStatus(getInitStatus(), DeviceTiming.NON_RESPONSE);
    }

    /**
     * Start schedule initialization
     * @param timeout Timeout before first start
     */
    private void startInit(int timeout) {
        // If the Peripheral has not responded to a poll for its maximum
        // non-response time, the Controller must continue to poll the
        // Peripheral at least every ten seconds with a RESET command.
        stopPolling();
        if (initTimer == null) {
            initTimer = new Timer();
            initTimer.schedule(new InitTask(), timeout,
                    DeviceTiming.RESET_NON_RESPONSE);
        }
    }

    /**
     * Stop schedule initialization
     */
    private void stopInit() {
        if (initTimer != null) {
            initTimer.cancel();
            initTimer.purge();
            initTimer = null;
        }
    }

    /**
     * Start polling process
     */
    protected void startPolling() {
        if (!isPolling()) {
            pollingTimer = new Timer();
            pollingTimer.scheduleAtFixedRate(new PollingTask(), DeviceTiming.FREE,
                    DeviceTiming.POLL);
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
     * Check polling mode
     * @return
     */
    protected boolean isPolling() {
        return pollingTimer != null;
    }

    /**
     * Wait status during timout ms
     * @param state Target status
     * @param timeout Waiting timeout
     */
    protected void waitStatus(int status, int timeout) throws JposException {
        boolean localPolling = !isPolling();
        Object notifier = new Object();
        long start = System.currentTimeMillis();
        try {
            while (((System.currentTimeMillis() - start) <= timeout) &&
                    (status != (this.status & 0xFF00))){
                synchronized(notifier) {
                    if (localPolling) {
                        notifier.wait(DeviceTiming.POLL);
                        poll();
                    } else
                        notifier.wait(DeviceTiming.FREE);
                }
            }
            if (status != this.status)
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT));
        } catch (InterruptedException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE), e);
        }
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

    //--------------------------------------------------------------------------
    // Static collections
    //

    private static final HashMap<DeviceService, SerialPort> deviceServices =
            new HashMap<DeviceService, SerialPort>();

    //--------------------------------------------------------------------------
    // Private and protected local variables
    //

    protected JposEntry jposEntry;
    private long executeTime = 0;
    private boolean executed = false;
    protected long responseTime = 0;
    private int buffer[] = new int[SIZE_BUFFER];
    private SerialPort port = null;
    private InputStream is = null;
    private OutputStream os = null;
    private String portName = null;
    protected boolean claimed = false;
    protected EventCallbacks eventCallbacks = null;
    protected int state = JposConst.JPOS_S_CLOSED;
    protected int status = 0;
    protected boolean deviceEnabled = false;
    protected int powerState = JposConst.JPOS_PS_UNKNOWN;
    protected int powerNotify = JposConst.JPOS_PN_DISABLED;
    private Timer pollingTimer = null;
    private Timer initTimer = null;
    protected boolean frozeEvents = false;
    protected boolean dataEventEnabled = false;
    private Thread eventThread = null;
    protected final PriorityQueue<JposEvent> eventQueue = 
            new PriorityQueue<JposEvent>(100, new EventComparator());
    protected final ArrayList<JposEvent> eventStore =
            new ArrayList<JposEvent>();

    //--------------------------------------------------------------------------
    // Common constants
    //

    /**
     * Message transmission start code [02H], fixed
     */
    private static final int SYNC_DATA = 0x02;

    /**
     * ACK response
     */
    private static final int ACK_DATA = 0x00;

    /**
     * Illegal command
     */
    private static final int ILLEGAL_COMMAND = 0x30;

    /**
     * NAK response
     */
    private static final int NAK_DATA = 0xFF;

    /**
     * Buffer max size
     */
    private static final int SIZE_BUFFER = 1024;

    /**
     * Retrive count for executing
     */
    private static final int MAX_RETRY = 2;

}
