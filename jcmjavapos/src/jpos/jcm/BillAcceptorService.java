/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.jcm;

import java.util.HashSet;
import jpos.BillAcceptorConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.events.DataEvent;
import jpos.events.StatusUpdateEvent;
import jpos.services.BillAcceptorService113;
import jpos.services.EventCallbacks;

/**
 *
 * @author Maxim
 */
public class BillAcceptorService extends DeviceService implements BillAcceptorService113 {

    /**
     * This method is called to set the initial amounts in the Bill Acceptor
     * after initial setup
     * @param string
     * @throws JposException
     */
    @Override
    public void adjustCashCounts(String string) throws JposException {
        cashCountsBuffer = string;
        setCashCounts(string);
    }

    /**
     * Cash acceptance is started
     * @throws JposException
     */
    @Override
    public void beginDeposit() throws JposException {
        clearDepositCounts();
        enableSequence();
    }

    /**
     * Clears all device input that has been buffered.
     * @throws JposException
     */
    @Override
    public void clearInput() throws JposException {
        eventStore.clear();
    }

    /**
     * Releases the device and its resources.
     * @throws JposException
     */
    @Override
    public void close() throws JposException {
        try {

            if (status != STATUS_DISABLE) {
                fixDeposit();
                endDeposit(BillAcceptorConst.BACC_DEPOSIT_COMPLETE);
            }
        } catch (JposException e) {
        }
        super.close();
    }

    /**
     * This method determines whether the version of the firmware contained in
     * the specified file is newer than, older than, or the same as the version
     * of the firmware in the physical device.
     * @param firmwareFileName Specifies either the name of the file containing
     * the firmware
     * @param result Location in which to return the result of the comparison.
     * @throws JposException
     */
    @Override
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Cash acceptance is completed.
     * @param status  The success parameter holds the value of how to deal with
     * the cash that was deposited
     * @throws JposException
     */
    @Override
    public void endDeposit(int status) throws JposException {
        if (status == BillAcceptorConst.BACC_DEPOSIT_COMPLETE) {
            // Check exists unaccepted counts
            if (unacceptedExists()) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL,
                        getErrorDescription(JposConst.JPOS_E_ILLEGAL));
            }
        }
    }

    /**
     * When this method is called, all property values are updated to reflect
     * the current values in the Bill Acceptor.
     * @throws JposException
     */
    @Override
    public void fixDeposit() throws JposException {
        disableSequence();
        acceptDepositCounts();
    }

    /**
     * If true, then the Service/device supports comparing the version of the
     * firmware in the physical device against that of a firmware file.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    /**
     * If true, the readCashCounts method can report effective discrepancy
     * values.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapDiscrepancy() throws JposException {
        return false;
    }

    /**
     * If true, the Bill Acceptor can report the condition that some cash slots
     * are full.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapFullSensor() throws JposException {
        return true;
    }

    /**
     * If true, the bill acceptor can report a mechanical jam or failure
     * condition.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapJamSensor() throws JposException {
        return true;
    }

    /**
     * If true, the Bill Acceptor can report the condition that some cash slots 
     * are nearly full.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapNearFullSensor() throws JposException {
        return true;
    }

    /**
     * If true, the Bill Acceptor has the capability to suspend cash acceptance
     * processing temporarily.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapPauseDeposit() throws JposException {
        return true;
    }

    /**
     * Identifies the reporting capabilities of the Device. 
     * @return
     * @throws JposException
     */
    @Override
    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_STANDARD;
    }

    /**
     * If true, the device is able to supply data as the money is being accepted
     * (вЂњreal timeвЂќ).
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapRealTimeData() throws JposException {
        return true;
    }

    /**
     * If true, the device accumulates and can provide various statistics
     * regarding usage; otherwise no usage statistics are accumulated.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        return false; // TODO Make stactistic request
    }

    /**
     * If true, then the device's firmware can be updated via the updateFirmware
     * method.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        return false; // TODO Make firmware update
    }

    /**
     * If true, the device statistics, or some of the statistics, can be reset 
     * to zero using the resetStatistics method, or updated using the 
     * updateStatistics method. If CapStatisticsReporting is false, then
     * CapUpdateStatistics is also false.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    /**
     * Contains the active currency code to be used by Bill Acceptor operations.
     * @return
     * @throws JposException
     */
    @Override
    public String getCurrencyCode() throws JposException {
        return currencyCode;
    }

    /**
     * Holds the number of enqueued DataEvents.
     * @return
     * @throws JposException
     */
    @Override
    public int getDataCount() throws JposException {
        return eventQueue.size() + eventStore.size();
    }

    /**
     * If true, a DataEvent will be delivered as soon as input data is enqueued.
     * If changed to true and some input data is already queued, then a
     * DataEvent is delivered immediately. 
     * If false, input data is enqueued for later delivery to the application.
     * Also, if an input error occurs, the ErrorEvent is not delivered while
     * this property is false.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getDataEventEnabled() throws JposException {
        return dataEventEnabled;
    }

    /**
     * The total amount of deposited cash.
     * @return
     * @throws JposException
     */
    @Override
    public int getDepositAmount() throws JposException {
        return depositAmount;
    }

    /**
     * Holds the cash units supported in the Bill Acceptor for the currency 
     * represented by the CurrencyCode property. 
     * @return
     * @throws JposException
     */
    @Override
    public String getDepositCashList() throws JposException {
        StringBuilder s = new StringBuilder(8 * 3);
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            for (int i = 0; i < 8; i++) {
                if (currencyCode.equals(depositCodeList[i])) {
                    if (s.length() > 0) {
                        s.append(",");
                    }
                    s.append(depositCashList[i]);
                }
            }
        }
        return ";" + s.toString();
    }

    /**
     * Holds the currency code indicators for cash accepted.
     * @return
     * @throws JposException
     */
    @Override
    public String getDepositCodeList() throws JposException {
        StringBuilder s = new StringBuilder(8 * 3);
        HashSet<String> ks = new HashSet<String>();
        for (int i = 0; i < 8; i++) {
            String curCode = depositCodeList[i];
            if ((curCode.length() > 0) && (!ks.contains(curCode))) {
                if (s.length() > 0) {
                    s.append(",");
                }
                s.append(curCode);
                ks.add(curCode);
            }
        }
        return s.toString();
    }

    /**
     * Holds the total of the cash accepted by the bill acceptor. Cash units
     * inside the string are the same as the DepositCashList property, and are
     * in the same order.
     * @return
     * @throws JposException
     */
    @Override
    public String getDepositCounts() throws JposException {
        StringBuilder s = new StringBuilder(80);
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            for (int i = 0; i < 8; i++) {
                if (currencyCode.equals(depositCodeList[i])) {
                    if (s.length() > 0) {
                        s.append(",");
                    }
                    s.append(depositCashList[i]);
                    s.append(":");
                    s.append(depositCounts[i]);

                }
            }
        }
        return ";" + s.toString();
    }

    /**
     * Holds the current status of the cash acceptance operation.
     * @return
     * @throws JposException
     */
    @Override
    public int getDepositStatus() throws JposException {
        switch (status) {
            case STATUS_ACCEPTING:
            case STATUS_REJECTING:
            case STATUS_STACKING:
            case STATUS_RETURNING:
            case STATUS_HOLDING:
            case STATUS_ESCROW:
            case STATUS_STACKED:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_COUNT;
            case STATUS_STACKER_FULL:
            case STATUS_FAILURE:
            case STATUS_STACKER_OPEN:
            case STATUS_JAM_ACCEPTOR:
            case STATUS_JAM_STACKER:
            case STATUS_CHEATED:
            case STATUS_PAUSE:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM;
            case STATUS_ENABLE:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_START;
            case STATUS_DISABLE:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_END;
            default:
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Holds the current full status of the cash slots.
     * @return
     * @throws JposException
     */
    @Override
    public int getFullStatus() throws JposException {
        if (fullStatus == STATUS_STACKER_FULL) {
            return BillAcceptorConst.BACC_STATUS_NEARFULL;
        } else if (fullStatus == STATUS_STACKER_OPEN) {
            return BillAcceptorConst.BACC_STATUS_FULL;
        } else {
            return BillAcceptorConst.BACC_STATUS_OK;
        }
    }

    /**
     * Contains the type of power notification selection made by the Application.
     * @return
     * @throws JposException
     */
    @Override
    public int getPowerNotify() throws JposException {
        return powerNotify;
    }

    /**
     * Identifies the current power condition of the device, if it can be
     * determined.
     * @return
     * @throws JposException
     */
    @Override
    public int getPowerState() throws JposException {
        return powerState;
    }

    /**
     * If true and CapRealTimeData is true, each data event fired will update
     * the DepositAmount and DepositCounts properties.
     * @return
     * @throws JposException
     */
    @Override
    public boolean getRealTimeDataEnabled() throws JposException {
        return realTimeDataEnabled;
    }

    /**
     * Opens a device for subsequent I/O.
     * @param logicalDeviceName Parameter specifies the device name to open
     * @param ec Callback event handler
     * @throws JposException
     */
    @Override
    public void open(String logicalDeviceName, EventCallbacks ec) throws JposException {
        // Initialize variables
        jamStatus = 0;
        fullStatus = 0;
        realTimeDataEnabled = false;
        depositAmount = 0;
        cashCountsBuffer = ";";
        currencyCode = "";
        for (int i = 0; i < 8; i++) {
            cashCounts[i] = 0;
            depositCounts[i] = 0;
            unacceptedCounts[i] = 0;
            depositCashList[i] = 0;
            depositCodeList[i] = "";
        }
        deviceDescription = "";

        // Read restricted list
        restrictedList = null;
        if (jposEntry.hasPropertyWithName("restricted")) {
            String restricted = jposEntry.getProp("restricted").getValueAsString();
            if (restricted != null) {
                restrictedList = restricted.split(",");
                for (int i = 0; i < restrictedList.length; i++) {
                    restrictedList[i] = restrictedList[i].trim();
                }
            }
        }

        // Call super open method
        super.open(logicalDeviceName, ec);
    }

    /**
     * Called to suspend or resume the process of depositing cash.
     * @param control
     * @throws JposException
     */
    @Override
    public void pauseDeposit(int control) throws JposException {
        if (control == BillAcceptorConst.BACC_DEPOSIT_PAUSE) {
            acceptDepositCounts();
            disableSequence();
        } else if (control == BillAcceptorConst.BACC_DEPOSIT_RESTART) {
            enableSequence();
        }
    }

    /**
     * Each unit in cashCounts matches a unit in the DepositCashList property,
     * and is in the same order.
     * @param strings
     * @param discrepancy
     * @throws JposException
     */
    @Override
    public void readCashCounts(String[] strings, boolean[] discrepancy) throws JposException {
        discrepancy[0] = false;
        strings[0] = getCashCounts();
    }

    /**
     * Resets the defined resettable statistics in a device to zero.
     * @param statisticsBuffer The data buffer defining the statistics that are
     * to be reset.
     * @throws JposException
     */
    @Override
    public void resetStatistics(String statisticsBuffer) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Retrieves the requested statistics from a device.
     * @param statisticsBuffer The data buffer defining the statistics to be
     * retrieved and in which the retrieved statistics are placed.
     * @throws JposException
     */
    @Override
    public void retrieveStatistics(String[] statisticsBuffer) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Contains the active currency code to be used by Bill Acceptor operations.
     * @param string
     * @throws JposException
     */
    @Override
    public void setCurrencyCode(String string) throws JposException {
        boolean exists = false;
        for (int i = 0; i < 8; i++) {
            if (depositCodeList[i].equalsIgnoreCase(string)) {
                exists = true;
            }
        }
        if (exists) {
            currencyCode = string.toUpperCase();
            cashCountsBuffer = getCashCounts();
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
    }

    /**
     * If true, a DataEvent will be delivered as soon as input data is enqueued.
     * If changed to true and some input data is already queued, then a
     * DataEvent is delivered immediately.
     * If false, input data is enqueued for later delivery to the application.
     * Also, if an input error occurs, the ErrorEvent is not delivered while
     * this property is false.
     * @return
     * @throws JposException
     */
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

    /**
     * Contains the type of power notification selection made by the Application.
     * @param i
     * @exception JposException
     */
    @Override
    public void setPowerNotify(int i) throws JposException {
        if (deviceEnabled) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        powerNotify = i;
        powerStateChanged();
    }

    /**
     * If true and CapRealTimeData is true, each data event fired will update
     * the DepositAmount and DepositCounts properties.
     * Otherwise, DepositAmount and DepositCounts are updated with the value of
     * the money collected when fixDeposit is called. Setting
     * RealTimeDataEnabled will not cause any change in system behavior until a
     * subsequent beginDeposit method is performed. This prevents confusion
     * regarding what would happen if it were modified between a beginDeposit -
     * endDeposit pairing.
     * @param bln
     * @throws JposException
     */
    @Override
    public void setRealTimeDataEnabled(boolean bln) throws JposException {
        realTimeDataEnabled = bln;
    }

    /**
     * This method updates the firmware of a device with the version of the
     * firmware contained or defined in the file specified by the firmware.
     * @param firmwareFileName Specifies either the name of the file containing
     * the firmware or a file containing a set of firmware files that are to be
     * downloaded into the device.
     * @throws JposException
     */
    @Override
    public void updateFirmware(String firmwareFileName) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Updates the defined resettable statistics in a device.
     * @param statisticsBuffer The data buffer defining the statistics with
     * values that are to be updated.
     * @throws JposException
     */
    @Override
    public void updateStatistics(String statisticsBuffer) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Return an identifier for the physical device.
     * @return
     * @throws JposException
     */
    @Override
    public String getPhysicalDeviceDescription() throws JposException {
        return deviceDescription;
    }

    /**
     * Return a short name identifying the physical device. This is a short
     * version of PhysicalDeviceDescription and should be limited to 30
     * characters.
     * @return
     * @throws JposException
     */
    @Override
    public String getPhysicalDeviceName() throws JposException {
        return deviceDescription.split(" ")[0];
    }

    /**
     * Set current peripheral status
     * @param status New peripheral status
     */
    @Override
    protected void statusChanged() {
        super.statusChanged();
        switch (status) {
            case STATUS_ACCEPTING:
            case STATUS_REJECTING:
            case STATUS_STACKING:
            case STATUS_RETURNING:
                break;
            case STATUS_ESCROW:
                try {
                    // Stack or return bill from escrow possition
                    int billType = statusdata - 0x61;
                    // Check currency
                    boolean accept = currencyCode.equalsIgnoreCase(depositCodeList[billType]);
                    if (accept && restrictedList != null) {
                        // Check restricted table
                        String banknote = depositCashList[billType] + " " + currencyCode;
                        for (int i = 0; i < restrictedList.length; i++) {
                            if (banknote.equalsIgnoreCase(restrictedList[i])) {
                                accept = false;
                                break;
                            }
                        }
                    }
                    if (accept) {
                        stackBillType = billType;
                        stackBill();
                    } else {
                        returnBill();
                    }
                } catch (JposException e) {
                    state = JposConst.JPOS_S_ERROR;
                }
                break;
            case STATUS_STACKED:
                if (stackBillType >= 0) {
                    int billType = stackBillType;
                    stackBillType = -1;
                    countBill(billType);
                    if (realTimeDataEnabled) {
                        acceptDepositCounts();
                    }
                    fireEvent(new DataEvent(eventCallbacks.getEventSource(), 0));
                }
                break;
            case STATUS_STACKER_FULL:
                if (status != fullStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_NEARFULL));
                    fullStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case STATUS_STACKER_OPEN:
                if (status != fullStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_FULL));
                    fullStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case STATUS_FAILURE:
            case STATUS_JAM_STACKER:
            case STATUS_JAM_ACCEPTOR:
            case STATUS_CHEATED:
            case STATUS_PAUSE:
                if (status != jamStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_JAM));
                    jamStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case STATUS_ENABLE:
            case STATUS_DISABLE:
                if (0 != jamStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_JAMOK));
                    jamStatus = 0;
                }
                if (0 != fullStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_FULLOK));
                    fullStatus = 0;
                }
                state = JposConst.JPOS_S_IDLE;
                break;
        }
    }

    /**
     * Add bill to unaccepted amount
     * @param billType
     */
    private void countBill(int billType) {
        synchronized (depositCounts) {
            unacceptedCounts[billType]++;
            cashCounts[billType]++;
            cashCountsBuffer = getCashCounts();
        }
    }

    /**
     * Check exists unaccepted amount
     * @return
     */
    private boolean unacceptedExists() {
        synchronized (depositCounts) {
            for (int i = 0; i < 8; i++) {
                if (unacceptedCounts[i] != 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Accept amount and counts
     */
    private void acceptDepositCounts() {
        synchronized (depositCounts) {
            for (int i = 0; i < 8; i++) {
                if (currencyCode.equals(depositCodeList[i])) {
                    depositCounts[i] = depositCounts[i] + unacceptedCounts[i];
                    depositAmount = depositAmount
                            + unacceptedCounts[i] * depositCashList[i];
                }
                unacceptedCounts[i] = 0;
            }
        }
    }

    /**
     * Clear deposit counts
     */
    private void clearDepositCounts() {
        synchronized (depositCounts) {
            for (int i = 0; i < 8; i++) {
                unacceptedCounts[i] = 0;
                depositCounts[i] = 0;
            }
            depositAmount = 0;
        }
    }

    /**
     * Adjust cash counts
     * @param ints
     */
    private void setCashCounts(int ints[]) {
        synchronized (depositCounts) {
            System.arraycopy(ints, 0, cashCounts, 0, 8);
            cashCountsBuffer = getCashCounts();
        }
    }

    /**
     * Initialize Bill Validator
     * @throws JposException
     */
    @Override
    protected void initialize() throws JposException {
        super.initialize();
        // Initialize parameters
        identification();
        poll();
        requestBillTable();
        poll();
        try {
            settingInit();
            waitStatus(STATUS_DISABLE, RESET_NON_RESPONSE_TIME);
        } catch (Exception e) {
        }
    }

    /**
     * Initialize setting parameters
     * @throws JposException 
     */
    protected void settingInit() throws JposException {
        settingDone = false;
        execute(SETTING_SECURITY, new int[]{0x00, 0x00});
        poll();
        execute(SETTING_ENABLE, new int[]{0x00, 0x00});
        poll();
        execute(SETTING_OPTIONAL, new int[]{0x03, 0x00});
        poll();
        execute(0xC6, new int[]{0x01, 0x12}); // ??
        poll();
        execute(0xC7, new int[]{0xFC}); // ??
        poll();
        execute(0xC2, new int[]{0x00}); // ??
        settingDone = true;
    }

    /**
     * Peripheral identification command
     */
    protected void identification()
            throws JposException {
        int identData[] = execute(REQUEST_VERSION, null);
        // Get device description
        deviceDescription = dataToAscii(identData, 1, identData.length - 1);
    }

    /**
     * Request bill table
     */
    protected void requestBillTable() throws JposException {
        int billTableData[] = execute(REQUEST_BILLTABLE, null);
        int billTypeCount = (billTableData.length - 1) / 4;
        for (int i = 0; i < billTypeCount; i++) {
            int billType = billTableData[i * 4 + 1] - 0x61;
            if (billTableData[i * 4 + 2] == 0x27) {
                depositCodeList[billType] = "RUB";
            } else {
                depositCodeList[billType] = "";
            }
            if ((!depositCodeList[billType].isEmpty()) && (currencyCode == null ? true : currencyCode.isEmpty())) {
                currencyCode = depositCodeList[i];
            }
            int denom = billTableData[i * 4 + 3];
            int decimal = billTableData[i * 4 + 4];
            int l = 1;
            for (int j = 0; j < (decimal & 0x7F); j++) {
                l = l * 10;
            }
            if ((decimal & 0x80) == 0) {
                denom = denom * l;
            } else {
                denom = denom / l;
            }
            depositCashList[billType] = denom;
        }
        setCashCounts(cashCountsBuffer);
    }

    /**
     * Request sequence status
     * @return Sequence enabled status
     */
    protected int[] requestStatus() throws JposException {
        int billStatusData[] = execute(COMMAND_POLL, null);
        if (billStatusData.length == 6) {
            return billStatusData;
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Enable sequence
     */
    protected void enableSequence() throws JposException {
        if (status == STATUS_ENABLE) {
            return;
        }
        if (!settingDone) {
            settingInit();
        }
        if (getDepositStatus() != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) {
            waitStatus(STATUS_DISABLE, NON_RESPONSE_TIME);
            int enableData[] = {0x00};
            execute(SETTING_INHIBIT, enableData);
            waitStatus(STATUS_ENABLE, NON_RESPONSE_TIME);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Disable sequence
     */
    protected void disableSequence() throws JposException {
        if (status == STATUS_DISABLE) {
            return;
        }
        if (!settingDone) {
            settingInit();
        }
        int disableData[] = {0x01};
        execute(SETTING_INHIBIT, disableData);
        if (getDepositStatus() != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) {
            waitStatus(STATUS_DISABLE, DELIVERY_TIME);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * This command forces a bill in escrow position to be sent to drop cassette
     * or one of the recycling cassettes.
     */
    protected void stackBill() throws JposException {
        execute(COMMAND_STACK1, null);
    }

    /**
     * This command causes the Bill Validator to return a bill in escrow
     * position to the customer.
     * COMMAND message is returned.
     */
    protected void returnBill() throws JposException {
        execute(COMMAND_RETURN, null);
    }

    /**
     * This command allows the Controller to hold Bill Validator in a state
     * Escrow during 10 s. After this time the Controller should send the STACK
     * or RETURN command. For continued holding in an Escrow state it is necessary
     * to resend this command. Otherwise Bill Validator will execute return
     * of a bill.
     */
    protected void holdBill() throws JposException {
        execute(COMMAND_HOLD, null);
    }

    /**
     * Get cash counts buffer
     */
    private String getCashCounts() {
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            StringBuilder s = new StringBuilder(80);
            for (int i = 0; i < 8; i++) {
                if (currencyCode.equals(depositCodeList[i])) {
                    if (s.length() > 0) {
                        s.append(",");
                    }
                    s.append(depositCashList[i]);
                    s.append(":");
                    s.append(cashCounts[i]);
                }
            }
            return ";" + s.toString();
        } else {
            return cashCountsBuffer;
        }
    }

    /**
     * Set cash counts buffer
     */
    private void setCashCounts(String string) {
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            String[] as = null;
            if (string != null ? (string.length() > 1
                    ? string.charAt(0) == ';' : false) : false) {
                as = string.substring(1).split(",");
            }
            int counts[] = new int[8];
            for (int i = 0; i < 8; i++) {
                if (currencyCode.equals(depositCodeList[i])) {
                    counts[i] = 0;
                    if (as != null) {
                        for (int j = 0; j < as.length; j++) {
                            if (as[j] != null) {
                                String ar[] = as[j].split(":");
                                if ((ar.length == 2)
                                        && (Integer.valueOf(ar[0]) == depositCashList[i])) {
                                    counts[i] = Integer.valueOf(ar[1]);
                                    as[j] = null;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            setCashCounts(counts);
        }
    }
    //--------------------------------------------------------------------------
    // Protected and private local constant and variables
    //
    protected int jamStatus = 0;
    protected int fullStatus = 0;
    protected boolean realTimeDataEnabled = false;
    protected String currencyCode = null;
    protected int depositAmount = 0;
    protected String cashCountsBuffer = null;
    protected final int cashCounts[] = new int[8];
    protected final int depositCounts[] = new int[8];
    protected final int unacceptedCounts[] = new int[8];
    protected final int depositCashList[] = new int[8];
    protected final String depositCodeList[] = new String[8];
    protected int stackBillType = -1;
    protected boolean settingDone = false;
    protected String deviceDescription = null;
    protected String restrictedList[] = null;
}
