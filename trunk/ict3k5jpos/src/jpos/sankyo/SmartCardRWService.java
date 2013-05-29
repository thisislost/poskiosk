/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.sankyo;

import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import jpos.JposConst;
import jpos.JposException;
import jpos.SmartCardRWConst;
import jpos.events.DataEvent;
import jpos.events.ErrorEvent;
import jpos.events.OutputCompleteEvent;
import jpos.events.StatusUpdateEvent;
import jpos.services.SmartCardRWService113;

/**
 *
 * @author Maxim
 */
public class SmartCardRWService extends DeviceService implements SmartCardRWService113 {

    public void clearInputProperties() throws JposException {
    }

    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    public boolean getCapUpdateFirmware() throws JposException {
        return false;
    }

    public void updateFirmware(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void beginInsertion(int i) throws JposException {
        if (i == 0) {
            if (!reader.isEnabled()) {
                reader.enable();
            }
        } else {
            if (reader.isEnabled()) {
                reader.disable();
            }
            reader.entry(i);
        }
        insertationMode = true;
    }

    public void beginRemoval(int i) throws JposException {
        synchronized (ejectNotifier) {
            reader.eject();
            if (i != 0) {
                try {
                    ejectNotifier.wait(i);
                } catch (InterruptedException e) {
                    throw new JposException(JposConst.JPOS_E_TIMEOUT,
                            getErrorDescription(JposConst.JPOS_E_TIMEOUT));
                }
                if (reader.status != 0x00) {
                    // If timeout - capture card
                    if (reader.status == 0x01) {
                        reader.retrive();
                        reader.capture();
                    }
                    throw new JposException(JposConst.JPOS_E_TIMEOUT,
                            getErrorDescription(JposConst.JPOS_E_TIMEOUT));
                }
            }
            removalMode = true;
        }
    }

    public void clearInput() throws JposException {
        eventStore.clear();
    }

    public void clearOutput() throws JposException {
        dataQueue.clear();
    }

    public void endInsertion() throws JposException {
        if (!insertationMode) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        insertationMode = false;
        if (reader.isEnabled()) {
            reader.disable();
        }
        if (reader.getStatus() != 0x02) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    public void endRemoval() throws JposException {
        if (!removalMode) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        removalMode = false;
        if (reader.getStatus() != 0x00) {
            // If no user action - capture card
            if (reader.status == 0x01) {
                reader.retrive();
                reader.capture();
            }
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    public boolean getCapCardErrorDetection() throws JposException {
        return false;
    }

    public int getCapInterfaceMode() throws JposException {
        return SmartCardRWConst.SC_CMODE_APDU;
    }

    public int getCapIsoEmvMode() throws JposException {
        return SmartCardRWConst.SC_CMODE_EMV | SmartCardRWConst.SC_CMODE_ISO;
    }

    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_NONE;
    }

    public int getCapSCPresentSensor() throws JposException {
        return 1;
    }

    public int getCapSCSlots() throws JposException {
        return 1;
    }

    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    public int getCapTransmissionProtocol() throws JposException {
        return SmartCardRWConst.SC_TRANS_PROTOCOL_T0 | SmartCardRWConst.SC_TRANS_PROTOCOL_T1;
    }

    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    public int getDataCount() throws JposException {
        return eventQueue.size() + eventStore.size();
    }

    public boolean getDataEventEnabled() throws JposException {
        return dataEventEnabled;
    }

    public int getInterfaceMode() throws JposException {
        return interfaceMode;
    }

    public int getIsoEmvMode() throws JposException {
        return isoEmvMode;
    }

    public int getOutputID() throws JposException {
        return outputID;
    }

    public int getPowerNotify() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getPowerState() throws JposException {
        return JposConst.JPOS_PS_UNKNOWN;
    }

    public int getSCPresentSensor() throws JposException {
        if (reader.getStatus() == 0x02) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getSCSlot() throws JposException {
        return 1;
    }

    public boolean getTransactionInProgress() throws JposException {
        return transactionInProgress;
    }

    public int getTransmissionProtocol() throws JposException {
        return transmitionProtocol;
    }

    public void readData(int i, int[] ints, String[] strings) throws JposException {
        // Read data
        fireEvent(new DataEvent(this, 0));
    }

    public void resetStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void retrieveStatistics(String[] strings) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setDataEventEnabled(boolean bln) throws JposException {
        synchronized (eventQueue) {
            dataEventEnabled = bln;
            if (dataEventEnabled) {
                synchronized (eventStore) {
                    if (!eventStore.isEmpty()) {
                        eventQueue.addAll(eventStore);
                        eventStore.clear();
                    }
                }
                eventQueue.notify();
            }
        }
    }

    public void setInterfaceMode(int i) throws JposException {
        if (i != SmartCardRWConst.SC_CMODE_APDU) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        } else {
            interfaceMode = i;
        }
    }

    public void setIsoEmvMode(int i) throws JposException {
        if (i != SmartCardRWConst.SC_CMODE_EMV && i != SmartCardRWConst.SC_CMODE_ISO) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        } else {
            isoEmvMode = i;
        }
    }

    public void setPowerNotify(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setSCSlot(int i) throws JposException {
        if (i != 1) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
    }

    public void updateStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void writeData(int i, int i1, String string) throws JposException {
        // Output data to reader
        // reader.writeData
        fireEvent(new OutputCompleteEvent(this, outputID));
        outputID = outputID + 1;
    }

    @Override
    public void onChangeStatus() {
        super.onChangeStatus();
        if (reader.status == 0x02) {
            transactionInProgress = true;
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), SmartCardRWConst.SC_SUE_CARD_PRESENT));
            fireEvent(new DataEvent(eventCallbacks.getEventSource(), 0));
        } else if (reader.status == 0x00) {
            if (transactionInProgress) {
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), SmartCardRWConst.SC_SUE_NO_CARD));
                transactionInProgress = false;
            }
            synchronized (ejectNotifier) {
                ejectNotifier.notifyAll();
            }
        } else if (reader.status >= 0x100) {
            fireEvent(new ErrorEvent(eventCallbacks.getEventSource(), JposConst.JPOS_E_FAILURE, 0,
                    JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));
        }
    }

    private class DataToWrite {

        int action;
        int count;
        String data;
    }
    protected int interfaceMode;
    protected int isoEmvMode;
    protected int transmitionProtocol;
    protected boolean transactionInProgress;
    protected int outputID;
    protected boolean removalMode = false;
    protected boolean insertationMode = false;
    protected int EJECT_TIMEOUT = 30000; // 30 sec before capture card
    protected final Object ejectNotifier = new Object();
    private final SynchronousQueue<DataToWrite> dataQueue = new SynchronousQueue<DataToWrite>();
}
