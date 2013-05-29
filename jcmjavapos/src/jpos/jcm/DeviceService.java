/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.jcm;

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
 *
 * @author Maxim
 */
public class DeviceService implements BaseService, JposServiceInstance {

    //--------------------------------------------------------------------------
    // Public methods for JavaPOS Service
    //
    @Override
    public String getDeviceServiceDescription() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_DESCRIPTION_PROP_NAME).getValueAsString();
    }

    @Override
    public int getDeviceServiceVersion() throws JposException {
        return SERVICE_VERSION;
    }

    @Override
    public String getPhysicalDeviceDescription() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_DESCRIPTION_PROP_NAME).getValueAsString();
    }

    @Override
    public String getPhysicalDeviceName() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_NAME_PROP_NAME).getValueAsString();
    }

    @Override
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

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        // Check port opened
        if ((state != JposConst.JPOS_S_CLOSED) && (port != null)) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }

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
                CommPort commPort = portIdentifier.open("JCM", NON_RESPONSE_TIME);
                if (commPort instanceof SerialPort) {
                    port = ((SerialPort) commPort);
                    port.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                    // Init timing
                    responseTime = System.currentTimeMillis();
                    executeTime = responseTime + BUS_RESET_TIME;
                    port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    port.setDTR(true);
                    port.setRTS(true);
                    port.setInputBufferSize(SIZE_BUFFER);
                    port.setOutputBufferSize(SIZE_BUFFER);
                    port.enableReceiveTimeout(INTER_BYTE);
                } else {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE,
                            getErrorDescription(JposConst.JPOS_E_NOSERVICE));
                }
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
            startInit(RESET_NON_RESPONSE_TIME);
        }
    }

    /**
     * Requests exclusive access to the device. 
     * @throws JposException
     */
    @Override
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
     * Get claimed device status
     * If true, the device is claimed for exclusive access. If false, the device
     * is released for sharing with other applications.
     * @return
     * @throws JposException
     */
    @Override
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
    @Override
    public boolean getDeviceEnabled() throws JposException {
        return deviceEnabled;
    }

    /**
     * Holds the current state of the Device.
     */
    @Override
    public int getState() throws JposException {
        return state;
    }

    /**
     * Releases exclusive access to the device.
     * @throws JposException
     */
    @Override
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
     * Set device operation state
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
    @Override
    public void setDeviceEnabled(boolean bln) throws JposException {
        if (bln) {
            // Initialize device
            if (state != JposConst.JPOS_S_IDLE) {
                try {
                    initialize();
                    state = JposConst.JPOS_S_IDLE;
                } catch (JposException e) {
                    state = JposConst.JPOS_S_ERROR;
                    startInit(RESET_NON_RESPONSE_TIME);
                }
            }
            deviceEnabled = true;
            eventThread = new Thread(new EventRunner());
            eventThread.start();
            if (initTimer == null) {
                startPolling();
            }
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
     * Tests the state of a device. 
     * A text description of the results of this method is placed in the
     * CheckHealthText property.
     * @throws JposException
     */
    @Override
    public void checkHealth(int level) throws JposException {
        if (!isPolling()) {
            poll();
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
    @Override
    public void directIO(int command, int[] data, Object obj) throws JposException {
        int response[];
        if (obj instanceof String) {
            response = execute(command, hexToData(obj.toString()));
        } else if (obj instanceof StringBuilder) {
            StringBuilder s = (StringBuilder) obj;
            response = execute(command, hexToData(s.toString()));
            s.replace(0, s.length() - 1, dataToHex(response, 0, response.length));
        } else {
            response = execute(command, null);
        }
        if ((response.length == 1) & (data != null)) {
            data[0] = response[0];
        }
    }

    /**
     * Return the results of the most recent call to the checkHealth method.
     * @return Health status description
     * @throws JposException
     */
    @Override
    public String getCheckHealthText() throws JposException {
        return getStatusDescription(status, statusdata);
    }

    /**
     * Get freeze deliver events
     * If true, the UnifiedPOS Control will not deliver events.
     * @return Freeze event state
     * @throws JposException
     */
    @Override
    public boolean getFreezeEvents() throws JposException {
        return frozeEvents;
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
    @Override
    public void setFreezeEvents(boolean bln) throws JposException {
        synchronized (eventQueue) {
            frozeEvents = bln;
            if (!frozeEvents) {
                eventQueue.notify();
            }
        }
    }
    
    

    //--------------------------------------------------------------------------
    // Protected methods
    //
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
    protected void powerStateChanged() {
            if (powerNotify == JposConst.JPOS_PN_ENABLED) {
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), powerState));
            }
    }

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
                getStatusDescription(status, statusdata)));
    }

    /**
     * Refresh current state by executing poll command
     * @return Peripheral state
     */
    protected void poll() throws JposException {
        int statusData[];
        try {
            statusData = execute(COMMAND_POLL, null);
            if (state == JposConst.JPOS_S_BUSY) {
                state = JposConst.JPOS_S_IDLE;
            }
        } catch (JposException e) {
            if (state == JposConst.JPOS_S_IDLE) {
                state = JposConst.JPOS_S_BUSY;
            }
            // If the Peripheral has not responded to a poll for its maximum
            // non-response time, the Controller must continue to poll the
            // Peripheral at least every ten seconds with a RESET command.
            if ((isPolling())
                    && ((System.currentTimeMillis() - responseTime) > NON_RESPONSE_TIME)) {
                setPowerState(JposConst.JPOS_PS_OFF_OFFLINE);
                state = JposConst.JPOS_S_ERROR;
                startInit(RESET_NON_RESPONSE_TIME);
            }
            throw e;
        }
        int newStatus;
        int newStatusData;
        switch (statusData.length) {
            case 2:
                // Set two-bytes state
                newStatus = statusData[0];
                newStatusData = statusData[1];
                break;
            case 1:
                // Set one-byte state
                newStatus = statusData[0];
                newStatusData = 0;
                break;
            default:
                // Invalid response - poll response must have 1 or 2 bytes date length
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        if ((status != newStatus) || (statusdata != newStatusData)) {
            status = newStatus;
            statusdata = newStatusData;
            statusChanged();
        }
    }

    /**
     * Reset peripheral by execute reset command
     * If communication error next reset scheduled on 10 sec.
     */
    protected void reset() throws JposException {
        execute(COMMAND_RESET, null);
    }

    /**
     * Initialize peripheral object
     * @throws JposException
     */
    protected void initialize() throws JposException {
        status = 0;
        statusdata = 0;
        setPowerState(JposConst.JPOS_PS_OFF_OFFLINE);
        reset();
        setPowerState(JposConst.JPOS_PS_ONLINE);
        waitStatus(STATUS_INITIALIZE, NON_RESPONSE_TIME);
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
                    RESET_NON_RESPONSE_TIME);
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
            pollingTimer.scheduleAtFixedRate(new PollingTask(), FREE_INTERVAL,
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
            while (((System.currentTimeMillis() - start) <= timeout)
                    && (status != this.status)) {
                synchronized (notifier) {
                    if (localPolling) {
                        notifier.wait(POLL_INTERVAL);
                        poll();
                    } else {
                        notifier.wait(FREE_INTERVAL);
                    }
                }
            }
            if (status != this.status) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT));
            }
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

        @Override
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

        @Override
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
        @Override
        public void run() {
            try {
                Object monitor = port;
                synchronized (monitor) {
                    if (!executed) {
                        poll();
                    }
                }
            } catch (JposException e) {
            }
        }
    }

    /**
     * Task for next reset command after RESET_NON_RESPONSE_TIME
     */
    private class InitTask extends TimerTask {

        /**
         * Implements run task
         */
        @Override
        public void run() {
            try {
                initialize();
                stopInit();
                if (deviceEnabled) {
                    startPolling();
                }
                state = JposConst.JPOS_S_IDLE;
            } catch (JposException e) {
                state = JposConst.JPOS_S_ERROR;
            }
        }
    }

    /**
     * Get port name
     * @return
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Wait and receive data
     * @param timeout Waiting time in millisecond
     * @return Data byte
     * @throws JposException
     */
    private int waitReceive(int timeout) throws JposException {
        Object notifier = new Object();
        try {
            synchronized (notifier) {
                long start = System.currentTimeMillis();
                while (true) {
                    int data = is.read();
                    if (data >= 0) {
                        return data;
                    }
                    if ((System.currentTimeMillis() - start) > timeout) {
                        break;
                    }
                    notifier.wait(INTER_BYTE);
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
            while (is.read() > -1) {
            }
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
        for (i = 0; i < sizeData; i++) {
            TmpCRC = CRC ^ bufData[i];
            for (j = 0; j < 8; j++) {
                if ((TmpCRC & 0x0001) != 0) {
                    TmpCRC >>= 1;
                    TmpCRC ^= POLYNOMIAL;
                } else {
                    TmpCRC >>= 1;
                }
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
            char ch = (char) data[i + ofs];
            if (ch > 0) {
                s.append(ch);
            } else {
                break;
            }
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
        // SYNC [FCh]
        int index = 0;
        buffer[index] = SYNC_DATA;
        index++;
        // LNG (Total number of bytes including SYNC and CRC)
        int sizeData = 5;
        if (data != null) {
            sizeData = sizeData + data.length;
        }
        buffer[index] = sizeData;
        index++;
        // CMD 
        buffer[index] = command;
        index++;
        // DATA Data necessary for command (omitted if not required by CMD)
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                buffer[index] = data[i];
                index++;
            }
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
            for (int i = 0; i < sizeData; i++) {
                os.write(buffer[i]);
            }
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
        buffer[index] = waitReceive(RESPONSE_TIME);
        if (buffer[index] != SYNC_DATA) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        index++;
        // LNG Data length (Total number of bytes including SYNC and CRC)
        buffer[index] = waitReceive(INTER_BYTE);
        int sizeData = buffer[index];
        index++;
        // Read message to the end
        for (int i = index; i < sizeData; i++) {
            buffer[i] = waitReceive(INTER_BYTE);
        }

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
        if (crc != getCRC16(buffer, sizeData - 2)) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }

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
            synchronized (notifier) {
                if (timeout > 0) {
                    notifier.wait(timeout);
                }
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
        if (port != null) {
            monitor = port;
        } else {
            throw new JposException(JposConst.JPOS_E_CLOSED,
                    getErrorDescription(JposConst.JPOS_E_CLOSED));
        }
        // Fire DirectIOEvent to trace executing
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                DIRECTIO_EXECUTE_COMMAND, command, getCommandDescription(command)));
        int retryCount = 0;
        while (true) {
            try {
                synchronized (monitor) {
                    // Check executed
                    while (executed) {
                        monitor.wait();
                    }
                    executed = true;
                    int[] response = null;
                    try {
                        // The tfree must be obeyed by the Controller between the end of any
                        // ACK or NAK confirmation response and start of the next command transmission.
                        pause(FREE_INTERVAL - (System.currentTimeMillis() - executeTime));

                        // Clear serial port buffer
                        clearReceive();

                        // Send message
                        send(command, data);

                        // Last command time + timeout, set even no response
                        executeTime = System.currentTimeMillis() + RESPONSE_TIME;

                        try {
                            // Receive message
                            response = receive();
                        } catch (JposException e) {
                            // Last execute time
                            executeTime = System.currentTimeMillis();
                            throw e;
                        }

                        // Last response time
                        responseTime = System.currentTimeMillis();

                        // Analyze response
                        if (response.length == 1) {
                            switch (response[0]) {
                                case ACK_DATA:
                                    break;
                                case COMMUNICATION_ERROR:
                                    throw new JposException(JposConst.JPOS_E_FAILURE,
                                            getErrorDescription(JposConst.JPOS_E_FAILURE));
                                case INVALID_COMMAND:
                                    throw new JposException(JposConst.JPOS_E_ILLEGAL,
                                            getErrorDescription(JposConst.JPOS_E_ILLEGAL));
                                case STATUS_VEND_VALID:
                                    // Send ACK echo response on validation
                                    send(ACK_DATA, null);
                                    break;
                            }
                        }

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
                if (retryCount < MAX_RETRY) {
                    pause(REPEAT_PAUSE);
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Get state code description
     * @param code
     * @return
     */
    public static String getStatusDescription(int status, int statusdata) {
        switch (status) {
            case STATUS_POWER_UP:
                return "Power Up";
            case STATUS_POWER_BILL_ACCEPTOR:
                return "Power Up with Bill in Acceptor";
            case STATUS_POWER_BILL_STACKER:
                return "Power up with bill in Stacker";
            case STATUS_INITIALIZE:
                return "Initialize";
            case STATUS_ENABLE:
                return "Idling";
            case STATUS_ACCEPTING:
                return "Accepting";
            case STATUS_STACKING:
                return "Stacking";
            case STATUS_RETURNING:
                return "Returning";
            case STATUS_DISABLE:
                return "Unit Disabled";
            case STATUS_HOLDING:
                return "Holding";
            case STATUS_REJECTING:
                switch (statusdata) {
                    case 0x71:
                        return "Rejecting: Insertation error";
                    case 0x72:
                        return "Rejecting: Mag error";
                    case 0x73:
                        return "Rejecting: Remaining of bill";
                    case 0x74:
                        return "Rejecting: Compensation error";
                    case 0x75:
                        return "Rejecting: Conveying error";
                    case 0x76:
                        return "Rejecting: Denomination assessing error";
                    case 0x77:
                        return "Rejecting: Photo pattern error 1";
                    case 0x78:
                        return "Rejecting: Photo level error";
                    case 0x79:
                        return "Rejecting: Inhibit/insertation direction, denomination error";
                    case 0x7B:
                        return "Rejecting: Operation error";
                    case 0x7C:
                        return "Rejecting: Remaining of bill";
                    case 0x7D:
                        return "Rejecting: Length error";
                    case 0x7E:
                        return "Rejecting: Photo pattern error 2";
                    default:
                        return "Rejecting: other";
                }
            case STATUS_STACKER_FULL:
                return "Drop Cassette Full";
            case STATUS_STACKER_OPEN:
                return "Drop Cassette out of position";
            case STATUS_JAM_ACCEPTOR:
                return "Bill Accepter Jammed";
            case STATUS_JAM_STACKER:
                return "Cassette Jammed";
            case STATUS_CHEATED:
                return "Cheated";
            case STATUS_PAUSE:
                return "Pause";
            case STATUS_FAILURE:
                switch (statusdata) {
                    case 0xA2:
                        return "Stock motor failure";
                    case 0xA5:
                        return "Transport motor speed failure";
                    case 0xA6:
                        return "Transport motor failure";
                    case 0xAB:
                        return "Cash box bot ready";
                    case 0xAF:
                        return "Validator head remove";
                    case 0xB0:
                        return "BOOT ROM failure";
                    case 0xB1:
                        return "External ROM failure";
                    case 0xB2:
                        return "ROM failure";
                    case 0xB3:
                        return "External ROM write failure";
                    default:
                        return "General failure";
                }
            case STATUS_ESCROW:
                return "Escrow position " + (statusdata - 0x60) + " denomination";
            case STATUS_STACKED:
                return "Bill stacked";
            case STATUS_VEND_VALID:
                return "Bill acceptance confirm";
        }
        return "Unknown state";
    }

    /**
     * Get command description
     * @param command
     * @return 
     */
    protected String getCommandDescription(int command) {
        switch (command) {
            case COMMAND_POLL:
                return "Status request";
            case COMMAND_RESET:
                return "Reset";
            case COMMAND_STACK1:
                return "Stack 1";
            case COMMAND_STACK2:
                return "Stack 2";
            case COMMAND_RETURN:
                return "Return";
            case COMMAND_HOLD:
                return "Hold";
            case COMMAND_WAIT:
                return "Wait";
            case SETTING_ENABLE:
                return "Enable setting";
            case SETTING_SECURITY:
                return "Security setting";
            case SETTING_INHIBIT:
                return "Inhibit setting";
            case SETTING_DIRECTION:
                return "Direction setting";
            case SETTING_OPTIONAL:
                return "Optional setting";
            case REQUEST_ENABLE:
                return "Request enable setting";
            case REQUEST_SECURITY:
                return "Request security setting";
            case REQUEST_INHIBIT:
                return "Request inhibit setting";
            case REQUEST_DIRECTION:
                return "Request direction setting";
            case REQUEST_OPTIONAL:
                return "Request optional setting";
            case REQUEST_VERSION:
                return "Request version";
            case REQUEST_BOOT_VERSION:
                return "Request boot version";
        }
        return "Unknown command";
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
     * Called when the JposServiceConnection is disconnected (i.e. service is closed)
     * @since 1.2 (NY2K meeting)
     */
    @Override
    public void deleteInstance() throws JposException {
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
    protected int statusdata = 0;
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
    // Constants
    //
    protected static final int SERVICE_VERSION = 1013000;
    // Buffer size
    protected static final int SIZE_BUFFER = 1024;
    // Frequency of poll ms
    protected static final int POLL_INTERVAL = 200;
    // Interval between start of commands ms
    protected static final int FREE_INTERVAL = 50;
    // Retry interval ms
    protected static final int REPEAT_PAUSE = 200;
    // Retry count 
    protected static final int MAX_RETRY = 2;
    // Non response timeout 2 sec
    protected static final int NON_RESPONSE_TIME = 2000;
    // Reset device when non response after 10 sec
    protected static final int RESET_NON_RESPONSE_TIME = 10000;
    // Time for delivary bill from escrow position 50 sec
    protected static final int DELIVERY_TIME = 50000;
    // Inter byte
    protected static final int INTER_BYTE = 20;
    // Response time out
    protected static final int RESPONSE_TIME = 100;
    // Bus reset time
    protected static final int BUS_RESET_TIME = 100;
    //--------------------------------------------------------------------------
    // DirectIO constants
    //
    // Direct IO device status changed
    public static final int DIRECTIO_STATUS_CHANGED = 0x00;
    // Direct IO trace execute command
    public static final int DIRECTIO_EXECUTE_COMMAND = 0x01;
    // Direct IO trace output message event
    public static final int DIRECTIO_TRACE_OUT = 0x02;
    // Direct IO trace input message event
    public static final int DIRECTIO_TRACE_IN = 0x03;
    //--------------------------------------------------------------------------
    // Protocol constants
    //
    protected static final int SYNC_DATA = 0xFC;
    protected static final int ACK_DATA = 0x50;
    protected static final int INVALID_COMMAND = 0x4B;
    protected static final int COMMUNICATION_ERROR = 0x4A;
    //--------------------------------------------------------------------------
    // Status constants
    //
    protected static final int STATUS_ENABLE = 0x11;
    protected static final int STATUS_ACCEPTING = 0x12;
    protected static final int STATUS_ESCROW = 0x13;
    protected static final int STATUS_STACKING = 0x14;
    protected static final int STATUS_VEND_VALID = 0x15;
    protected static final int STATUS_STACKED = 0x16;
    protected static final int STATUS_REJECTING = 0x17;
    protected static final int STATUS_RETURNING = 0x18;
    protected static final int STATUS_HOLDING = 0x19;
    protected static final int STATUS_DISABLE = 0x1A;
    protected static final int STATUS_INITIALIZE = 0x1B;
    protected static final int STATUS_POWER_UP = 0x40;
    protected static final int STATUS_POWER_BILL_ACCEPTOR = 0x41;
    protected static final int STATUS_POWER_BILL_STACKER = 0x42;
    protected static final int STATUS_STACKER_FULL = 0x43;
    protected static final int STATUS_STACKER_OPEN = 0x44;
    protected static final int STATUS_JAM_ACCEPTOR = 0x45;
    protected static final int STATUS_JAM_STACKER = 0x46;
    protected static final int STATUS_PAUSE = 0x47;
    protected static final int STATUS_CHEATED = 0x48;
    protected static final int STATUS_FAILURE = 0x49;
    //--------------------------------------------------------------------------
    // Command constants
    //
    protected static final int COMMAND_POLL = 0x11;
    protected static final int COMMAND_RESET = 0x40;
    protected static final int COMMAND_STACK1 = 0x41;
    protected static final int COMMAND_STACK2 = 0x42;
    protected static final int COMMAND_RETURN = 0x43;
    protected static final int COMMAND_HOLD = 0x44;
    protected static final int COMMAND_WAIT = 0x45;
    protected static final int SETTING_ENABLE = 0xC0;
    protected static final int SETTING_SECURITY = 0xC1;
    protected static final int SETTING_INHIBIT = 0xC3;
    protected static final int SETTING_DIRECTION = 0xC4;
    protected static final int SETTING_OPTIONAL = 0xC5;
    protected static final int REQUEST_ENABLE = 0x80;
    protected static final int REQUEST_SECURITY = 0x81;
    protected static final int REQUEST_INHIBIT = 0x83;
    protected static final int REQUEST_DIRECTION = 0x84;
    protected static final int REQUEST_OPTIONAL = 0x85;
    protected static final int REQUEST_VERSION = 0x88;
    protected static final int REQUEST_BILLTABLE = 0x8A;
    protected static final int REQUEST_BOOT_VERSION = 0x89;
}
