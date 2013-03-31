package jpos.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import jpos.util.tracing.Tracer;
import jpos.util.tracing.TracerFactory;

public class DeviceService implements BaseService, JposServiceInstance {

    @Override
    public String getCheckHealthText() throws JposException {
        return "";
    }

    @Override
    public boolean getClaimed() throws JposException {
        return claimed;
    }

    @Override
    public boolean getDeviceEnabled() throws JposException {
        return deviceEnabled;
    }

    @Override
    public void setDeviceEnabled(boolean bln) throws JposException {
        if (bln) {
            eventThread = new Thread(new EventRunner());
            eventThread.start();
            deviceEnabled = true;
        } else {
            deviceEnabled = false;
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

    @Override
    public String getDeviceServiceDescription() throws JposException {
        return jposEntry.getProp(JposEntry.PRODUCT_DESCRIPTION_PROP_NAME).getValueAsString();
    }

    @Override
    public int getDeviceServiceVersion() throws JposException {
        return SERVICE_VERSION;
    }

    @Override
    public boolean getFreezeEvents() throws JposException {
        return frozeEvents;
    }

    @Override
    public void setFreezeEvents(boolean bln) throws JposException {
        synchronized (eventQueue) {
            frozeEvents = bln;
            if (!frozeEvents) {
                eventQueue.notify();
            }
        }
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
    public int getState() throws JposException {
        return state;
    }

    @Override
    public void claim(int i) throws JposException {
        if (claimed) {
            try {
                Thread.sleep(i);
            } catch (Exception e) {
            }
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        claimed = true;
    }

    @Override
    public void close() throws JposException {
        if (state != JposConst.JPOS_S_CLOSED) {
            try {
                if (deviceEnabled) {
                    setDeviceEnabled(false);
                }
            } catch (JposException e) {
            }
            try {
                if (claimed) {
                    release();
                }
            } catch (JposException e) {
            }
            state = JposConst.JPOS_S_CLOSED;
        }
        try {
            greeting.close();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        tracer.println("Simulator service stopped");
    }

    @Override
    public void checkHealth(int i) throws JposException {
    }

    @Override
    public void directIO(int i, int[] ints, Object o) throws JposException {
    }

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        tracer = TracerFactory.getInstance().createTracer(string);
        if (state != JposConst.JPOS_S_CLOSED) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        powerNotify = JposConst.JPOS_PN_DISABLED;
        powerState = JposConst.JPOS_PS_UNKNOWN;
        state = JposConst.JPOS_S_ERROR;
        eventCallbacks = ec;
        state = JposConst.JPOS_S_IDLE;
        int port = Integer.parseInt(jposEntry.getProp("port").getValueAsString());
        try {
            greeting = new GreetingServer(port);
            greeting.start();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        tracer.println("Simulator service started on port " + port);
    }

    @Override
    public void release() throws JposException {
        if (!claimed) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        try {
            if (deviceEnabled) {
                setDeviceEnabled(false);
            }
        } catch (JposException e) {
        }
        claimed = false;
    }

    @Override
    public void deleteInstance() throws JposException {
    }

    void setJposEntry(JposEntry entry) {
        jposEntry = entry;
    }

    JposEntry getJposEntry() {
        return jposEntry;
    }

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

    private class EventRunner implements Runnable {

        @Override
        public void run() {
            try {
                while (deviceEnabled) {
                    if (!frozeEvents) {
                        while (true) {
                            JposEvent event;
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
    private static final int SERVICE_VERSION = 1013000;
    protected JposEntry jposEntry;
    protected boolean claimed = false;
    protected int state = JposConst.JPOS_S_CLOSED;
    protected boolean deviceEnabled = false;
    protected boolean frozeEvents = false;
    protected final PriorityQueue<JposEvent> eventQueue =
            new PriorityQueue<>(100, new EventComparator());
    protected final ArrayList<JposEvent> eventStore =
            new ArrayList<>();
    protected EventCallbacks eventCallbacks = null;
    protected int powerState = JposConst.JPOS_PS_UNKNOWN;
    protected int powerNotify = JposConst.JPOS_PN_DISABLED;
    protected boolean dataEventEnabled = false;
    protected GreetingServer greeting = null;
    private Thread eventThread = null;
    protected Tracer tracer = null;
}
