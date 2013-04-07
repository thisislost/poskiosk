/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.simulator;

import java.util.HashMap;
import jpos.BillAcceptorConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.events.DataEvent;
import jpos.events.StatusUpdateEvent;
import jpos.services.BillAcceptorService113;
import jpos.services.EventCallbacks;

/**
 *
 * @author ryabochkin_mr
 */
public class BillAcceptorService extends DeviceService implements BillAcceptorService113 {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        super.open(string, ec);
        greeting.registerResource("accept", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                int nominal = Integer.parseInt(params.get("nominal"));
                String s = nominal + " " + currencyCode;
                tracer.println("Accept cash " + s);
                acceptCash(nominal);
                return s;
            }
        });
        greeting.registerResource("jam", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String s = params.get("status");
                int status;
                if ("JAM".equalsIgnoreCase(s)) {
                    status = BillAcceptorConst.BACC_STATUS_JAM;
                    s = "JAM";
                } else {
                    status = BillAcceptorConst.BACC_STATUS_JAMOK;
                    s = "JAMOK";
                }
                tracer.println("Jam status is " + s);
                setJamStatus(status);
                return s;
            }
        });
        greeting.registerResource("full", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String s = params.get("status");
                int status;
                if ("FULL".equalsIgnoreCase(s)) {
                    status = BillAcceptorConst.BACC_STATUS_FULL;
                    s = "FULL";
                } else if ("NEARFULL".equalsIgnoreCase(s)) {
                    status = BillAcceptorConst.BACC_STATUS_NEARFULL;
                    s = "NEARFULL";
                } else {
                    status = BillAcceptorConst.BACC_STATUS_FULLOK;
                    s = "FULLOK";
                }
                tracer.println("Full status is " + s);
                setFullStatus(status);
                return s;
            }
        });
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDiscrepancy() throws JposException {
        return false;
    }

    @Override
    public boolean getCapFullSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJamSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapNearFullSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapPauseDeposit() throws JposException {
        return true;
    }

    @Override
    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_STANDARD;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return true;
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    @Override
    public String getCurrencyCode() throws JposException {
        return currencyCode;
    }

    @Override
    public void setCurrencyCode(String string) throws JposException {
        currencyCode = string;

    }

    @Override
    public int getDataCount() throws JposException {
        return eventQueue.size() + eventStore.size();
    }

    @Override
    public boolean getDataEventEnabled() throws JposException {
        return dataEventEnabled;
    }

    @Override
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

    @Override
    public int getDepositAmount() throws JposException {
        return depositAmount;
    }

    @Override
    public String getDepositCashList() throws JposException {
        return ";10,50,100,500,1000,5000";
    }

    @Override
    public String getDepositCodeList() throws JposException {
        return "USD,EUR,RUB";
    }

    @Override
    public String getDepositCounts() throws JposException {
        StringBuilder sb = new StringBuilder();
        sb.append(";");
        for (Integer key : depositCounts.keySet()) {
            sb.append(key);
            sb.append(":");
            sb.append(depositCounts.get(key));
        }
        return sb.toString();
    }

    @Override
    public int getDepositStatus() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getFullStatus() throws JposException {
        return fullStatus;
    }

    @Override
    public boolean getRealTimeDataEnabled() throws JposException {
        return realTimeDataEnabled;
    }

    @Override
    public void setRealTimeDataEnabled(boolean bln) throws JposException {
        realTimeDataEnabled = bln;
    }

    @Override
    public void adjustCashCounts(String string) throws JposException {
        cashCounts.clear();
        String[] as;
        if (string != null ? (string.length() > 1
                ? string.charAt(0) == ';' : false) : false) {
            as = string.substring(1).split(",");
            for (int j = 0; j < as.length; j++) {
                if (as[j] != null) {
                    String ar[] = as[j].split(":");
                    if (ar.length == 2) {
                        cashCounts.put(Integer.valueOf(ar[0]), Integer.valueOf(ar[1]));
                    }
                }
            }
        }
    }

    @Override
    public void beginDeposit() throws JposException {
        depositStatus = BillAcceptorConst.BACC_STATUS_DEPOSIT_START;
        depositCounts.clear();
        depositAmount = 0;
    }

    @Override
    public void clearInput() throws JposException {
        eventStore.clear();
    }

    @Override
    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    @Override
    public void endDeposit(int i) throws JposException {
        if (unacceptedCounts.size() > 0) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        depositStatus = BillAcceptorConst.BACC_STATUS_DEPOSIT_END;
    }

    @Override
    public void fixDeposit() throws JposException {
        acceptDepositCounts();
    }

    @Override
    public void pauseDeposit(int i) throws JposException {
        if (i == BillAcceptorConst.BACC_DEPOSIT_PAUSE) {
            acceptDepositCounts();
        } else if (i == BillAcceptorConst.BACC_DEPOSIT_RESTART) {
        }
    }

    @Override
    public void readCashCounts(String[] strings, boolean[] blns) throws JposException {
        StringBuilder sb = new StringBuilder();
        sb.append(";");
        for (Integer key : cashCounts.keySet()) {
            sb.append(key);
            sb.append(":");
            sb.append(cashCounts.get(key));
        }
        strings[0] = sb.toString();
        blns[0] = true;
    }

    @Override
    public void resetStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    @Override
    public void retrieveStatistics(String[] strings) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    @Override
    public void updateFirmware(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    @Override
    public void updateStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void acceptCash(int i) {
        if (depositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_START) {
            unacceptedCounts.put(i, nvl(unacceptedCounts.get(i)) + 1);
            if (realTimeDataEnabled) {
                acceptDepositCounts();
            }
            cashCounts.put(i, nvl(cashCounts.get(i)) + 1);
            fireEvent(new DataEvent(eventCallbacks.getEventSource(), 0));
        }
    }

    public void setFullStatus(int i) {
        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), i));
        fullStatus = i;
    }

    public void setJamStatus(int i) {
        fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), i));
        jamStatus = i;
    }

    private void acceptDepositCounts() {
        for (int key : unacceptedCounts.keySet()) {
            int value = nvl(depositCounts.get(key)) + unacceptedCounts.get(key);
            depositCounts.put(key, value);
            depositAmount = depositAmount + key * value;
        }
    }

    private int nvl(Integer i) {
        if (i != null) {
            return i;
        } else {
            return 0;
        }
    }
    protected String currencyCode;
    protected boolean realTimeDataEnabled;
    protected int depositAmount = 0;
    protected int fullStatus = BillAcceptorConst.BACC_STATUS_FULLOK;
    protected int jamStatus = BillAcceptorConst.BACC_STATUS_JAMOK;
    protected int depositStatus = BillAcceptorConst.BACC_STATUS_DEPOSIT_END;
    protected final HashMap<Integer, Integer> cashCounts = new HashMap<>();
    protected final HashMap<Integer, Integer> unacceptedCounts = new HashMap<>();
    protected final HashMap<Integer, Integer> depositCounts = new HashMap<>();
}
