/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.szzt;

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
    public void directIO(int command, int[] data, Object obj) throws JposException {}

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
                CommPort commPort = portIdentifier.open("SZZT", PORT_OPEN_TIMEOUT);
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
            if (dataEventEnabled ||
                    (!(event instanceof DataEvent)) ||
                    (!(event instanceof ErrorEvent))) {
                synchronized (eventQueue) {
                    eventQueue.add(event);
                    eventQueue.notify();
                }
            } else
                eventStore.add(event);
        }
    }

    /**
     * Get status description
     * @return State description
     */
    protected abstract String getStatusDescription(int status);

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
                poll();
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

    //--------------------------------------------------------------------------
    // Constants
    //

    /**
     * command max size
     */
    protected static final int SIZE_BUFFER = 1024;

    /**
     * Status polling interval
     */
    private static final int POLL_INTERVAL = 200;

    /**
     * Port open timeout
     */
    private static final int PORT_OPEN_TIMEOUT = 10000;

    /**
     * Service version
     */
    private static final int SERVICE_VERSION = 1013000;
}

