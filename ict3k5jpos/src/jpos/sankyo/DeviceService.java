/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.sankyo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
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
public class DeviceService implements BaseService, JposServiceInstance, CardReaderListener {

    public void checkHealth(int arg0) throws JposException {
        if (!reader.isPolling()) {
            reader.poll();
        }
    }

    public void claim(int arg0) throws JposException {
        claimed = true;
    }

    public void close() throws JposException {
        if (state != JposConst.JPOS_S_CLOSED) {
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
            state = JposConst.JPOS_S_CLOSED;
            ArrayList<DeviceService> ref = portRef.get(portName);
            ref.remove(this);
            if (ref.isEmpty()) {
                reader.close();
                portRef.remove(portName);
                portMap.remove(portName);
            }
        }
    }

    public void directIO(int arg0, int[] arg1, Object arg2) throws JposException {
        if (arg0 == 0 && arg1 != null) {
            byte[] data = new byte[arg1.length];
            for (int i = 0; i < arg1.length; i++) {
                data[i] = (byte) arg1[i];
            }
            reader.execute("C" + new String(data));
        }
    }

    public String getCheckHealthText() throws JposException {
        return reader.getStatusDescription(reader.status);
    }

    public boolean getClaimed() throws JposException {
        return claimed;
    }

    public boolean getDeviceEnabled() throws JposException {
        return deviceEnabled;
    }

    public String getDeviceServiceDescription() throws JposException {
        return "Card Reader ICT3K5";
    }

    public int getDeviceServiceVersion() throws JposException {
        return SERVICE_VERSION;
    }

    public boolean getFreezeEvents() throws JposException {
        return frozeEvents;
    }

    public String getPhysicalDeviceDescription() throws JposException {
        return "Card Reader ICT3K5";
    }

    public String getPhysicalDeviceName() throws JposException {
        return "Card Reader ICT3K5";
    }

    public int getState() throws JposException {
        return state;
    }

    public void open(String logicalDeviceName, EventCallbacks ec) throws JposException {

        // Check port opened
        if (state != JposConst.JPOS_S_CLOSED) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }

        portName = jposEntry.getProp("portName").getValueAsString();
        // int baudRate = Integer.valueOf(jposEntry.getProp("baudRate").getValueAsString());

        // Open port
        try {
            if (portMap.containsKey(portName)) {
                reader = portMap.get(portName);
            } else {
                reader = new CardReader();
                reader.open(portName);
                portMap.put(portName, reader);
                portRef.put(portName, new ArrayList<DeviceService>());
            }
            ArrayList<DeviceService> ref = portRef.get(portName);
            ref.add(this);
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE,
                    getErrorDescription(JposConst.JPOS_E_NOSERVICE), e);
        }
        eventCallbacks = ec;
        state = JposConst.JPOS_S_IDLE;
        claimed = false;
        deviceEnabled = false;
        frozeEvents = false;
        dataEventEnabled = false;
        autoDisable = false;
    }

    public void release() throws JposException {
        claimed = false;
    }

    public void setDeviceEnabled(boolean arg0) throws JposException {
        if (deviceEnabled != arg0) {
            deviceEnabled = arg0;
            if (deviceEnabled) {
                // Start event queue
                eventThread = new Thread(new EventRunner());
                eventThread.start();
                reader.addListener(this);
            } else {
                // Stop event queue
                reader.removeListener(this);
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
    }

    public void setFreezeEvents(boolean arg0) throws JposException {
        synchronized (eventQueue) {
            frozeEvents = arg0;
            if (!frozeEvents) {
                eventQueue.notify();
            }
        }
    }

    public void deleteInstance() throws JposException {
    }

    //--------------------------------------------------------------------------
    // Private task and thread classes
    //
    /**
     * Status changed event
     */
    public void onChangeStatus() {
        fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                reader.getStatus(), 0, null));
    }

    /**
     * Sensor changed event
     */
    public void onChangeSensor() {
    }

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
                                        dataEventEnabled = false;
                                        if (autoDisable) {
                                            setDeviceEnabled(false);
                                        }
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
    //-------------------------------------------------------------------------- 
    // Private and protected local variables
    //
    private static final int SERVICE_VERSION = 1013000;
    private JposEntry jposEntry;
    protected boolean claimed = false;
    protected EventCallbacks eventCallbacks = null;
    protected int state = JposConst.JPOS_S_CLOSED;
    protected boolean deviceEnabled = false;
    protected boolean frozeEvents = false;
    protected boolean dataEventEnabled = false;
    protected boolean autoDisable = false;
    private Thread eventThread = null;
    protected final PriorityQueue<JposEvent> eventQueue =
            new PriorityQueue<JposEvent>(100, new EventComparator());
    protected final ArrayList<JposEvent> eventStore =
            new ArrayList<JposEvent>();
    protected static final HashMap<String, CardReader> portMap =
            new HashMap<String, CardReader>();
    protected static final HashMap<String, ArrayList<DeviceService>> portRef =
            new HashMap<String, ArrayList<DeviceService>>();
    private String portName = null;
    protected CardReader reader;
}
