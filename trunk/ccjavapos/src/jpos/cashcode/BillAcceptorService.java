/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.cashcode;

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
    public void adjustCashCounts(String string) throws JposException {
        cashCountsBuffer = string;
        setCashCounts(string);
    }

    /**
     * Cash acceptance is started
     * @throws JposException
     */
    public void beginDeposit() throws JposException {
        clearDepositCounts();
        enableSequence();
    }

    /**
     * Clears all device input that has been buffered.
     * @throws JposException
     */
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
            if (status != BillAcceptorStatus.UNIT_DISABLED) {
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
    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    /**
     * If true, the readCashCounts method can report effective discrepancy
     * values.
     * @return
     * @throws JposException
     */
    public boolean getCapDiscrepancy() throws JposException {
        return false;
    }

    /**
     * If true, the Bill Acceptor can report the condition that some cash slots
     * are full.
     * @return
     * @throws JposException
     */
    public boolean getCapFullSensor() throws JposException {
        return true;
    }

    /**
     * If true, the bill acceptor can report a mechanical jam or failure
     * condition.
     * @return
     * @throws JposException
     */
    public boolean getCapJamSensor() throws JposException {
        return true;
    }

    /**
     * If true, the Bill Acceptor can report the condition that some cash slots 
     * are nearly full.
     * @return
     * @throws JposException
     */
    public boolean getCapNearFullSensor() throws JposException {
        return true;
    }

    /**
     * If true, the Bill Acceptor has the capability to suspend cash acceptance
     * processing temporarily.
     * @return
     * @throws JposException
     */
    public boolean getCapPauseDeposit() throws JposException {
        return true;
    }

    /**
     * Identifies the reporting capabilities of the Device. 
     * @return
     * @throws JposException
     */
    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_STANDARD;
    }

    /**
     * If true, the device is able to supply data as the money is being accepted
     * (“real time”).
     * @return
     * @throws JposException
     */
    public boolean getCapRealTimeData() throws JposException {
        return true;
    }

    /**
     * If true, the device accumulates and can provide various statistics
     * regarding usage; otherwise no usage statistics are accumulated.
     * @return
     * @throws JposException
     */
    public boolean getCapStatisticsReporting() throws JposException {
        return false; // TODO Make stactistic request
    }

    /**
     * If true, then the device's firmware can be updated via the updateFirmware
     * method.
     * @return
     * @throws JposException
     */
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
    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    /**
     * Contains the active currency code to be used by Bill Acceptor operations.
     * @return
     * @throws JposException
     */
    public String getCurrencyCode() throws JposException {
        return currencyCode;
    }

    /**
     * Holds the number of enqueued DataEvents.
     * @return
     * @throws JposException
     */
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
    public boolean getDataEventEnabled() throws JposException {
        return dataEventEnabled;
    }

    /**
     * The total amount of deposited cash.
     * @return
     * @throws JposException
     */
    public int getDepositAmount() throws JposException {
        return depositAmount;
    }

    /**
     * Holds the cash units supported in the Bill Acceptor for the currency 
     * represented by the CurrencyCode property. 
     * @return
     * @throws JposException
     */
    public String getDepositCashList() throws JposException {
        StringBuilder s = new StringBuilder(24 * 3);
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            for (int i = 0; i < 24; i++) {
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
    public String getDepositCodeList() throws JposException {
        StringBuilder s = new StringBuilder(24 * 3);
        HashSet<String> ks = new HashSet<String>();
        for (int i = 0; i < 24; i++) {
            String curCode = depositCodeList[i];
            if ((!curCode.equals("BAR")) && (curCode.length() > 0)
                    && (!ks.contains(curCode))) {
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
    public String getDepositCounts() throws JposException {
        StringBuilder s = new StringBuilder(240);
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            for (int i = 0; i < 24; i++) {
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
    public int getDepositStatus() throws JposException {
        switch (status & 0xFF00) {
            case BillAcceptorStatus.ACCEPTING:
            case BillAcceptorStatus.REJECTING:
            case BillAcceptorStatus.STACKING:
            case BillAcceptorStatus.RETURNING:
            case BillAcceptorStatus.HOLDING:
            case BillAcceptorStatus.EVENT_ESCROW_POSITION:
            case BillAcceptorStatus.EVENT_RETURNED:
            case BillAcceptorStatus.EVENT_STACKED:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_COUNT;
            case BillAcceptorStatus.DROP_CASSETTE_FULL:
            case BillAcceptorStatus.FAIL_GENERIC:
            case BillAcceptorStatus.DROP_CASSETTE_POSITION:
            case BillAcceptorStatus.BILL_VALIDATOR_JAMMED:
            case BillAcceptorStatus.CASSETTE_JAMMED:
            case BillAcceptorStatus.CHEATED:
            case BillAcceptorStatus.DEVICE_BUSY:
            case BillAcceptorStatus.PAUSE:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM;
            case BillAcceptorStatus.IDLING:
                return BillAcceptorConst.BACC_STATUS_DEPOSIT_START;
            case BillAcceptorStatus.UNIT_DISABLED:
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
    public int getFullStatus() throws JposException {
        if (fullStatus == BillAcceptorStatus.DROP_CASSETTE_FULL) {
            return BillAcceptorConst.BACC_STATUS_NEARFULL;
        } else if (fullStatus == BillAcceptorStatus.DROP_CASSETTE_POSITION) {
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
    public int getPowerNotify() throws JposException {
        return powerNotify;
    }

    /**
     * Identifies the current power condition of the device, if it can be
     * determined.
     * @return
     * @throws JposException
     */
    public int getPowerState() throws JposException {
        return powerState;
    }

    /**
     * If true and CapRealTimeData is true, each data event fired will update
     * the DepositAmount and DepositCounts properties.
     * @return
     * @throws JposException
     */
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
        partNumber = "";
        serialNumber = "";
        assetNumber = "";
        jamStatus = 0;
        fullStatus = 0;
        realTimeDataEnabled = false;
        currencyCode = "";
        depositAmount = 0;
        cashCountsBuffer = ";";
        for (int i = 0; i < 24; i++) {
            cashCounts[i] = 0;
            depositCounts[i] = 0;
            unacceptedCounts[i] = 0;
            depositCashList[i] = 0;
            depositCodeList[i] = "";
        }

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
    public void retrieveStatistics(String[] statisticsBuffer) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Contains the active currency code to be used by Bill Acceptor operations.
     * @param string
     * @throws JposException
     */
    public void setCurrencyCode(String string) throws JposException {
        boolean exists = false;
        for (int i = 0; i < 24; i++) {
            if ((!string.equalsIgnoreCase("BAR"))
                    && (depositCodeList[i].equalsIgnoreCase(string))) {
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
    public void updateStatistics(String statisticsBuffer) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Return an identifier for the UnifiedPOS Service and the company that
     * produced it.
     * @return identifier
     * @throws JposException
     */
    public String getDeviceServiceDescription() throws JposException {
        return "CashCode NET Bill Validator";
    }

    /**
     * Return the UnifiedPOS Service version number.
     * 
     * Three version levels are specified, as follows:
     * Major - The “millions” place.
     * Minor - The “thousands” place.
     * Build - The “units” place.
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
        return "CashCode Bill Validator " + partNumber + ". Serial number: "
                + serialNumber + ". Asset number: " + assetNumber + ".";
    }

    /**
     * Return a short name identifying the physical device. This is a short
     * version of PhysicalDeviceDescription and should be limited to 30
     * characters.
     * @return
     * @throws JposException
     */
    public String getPhysicalDeviceName() throws JposException {
        return "CashCode " + partNumber;
    }

    @Override
    protected int getAddress() {
        return BILL_VALIDATOR;
    }

    @Override
    protected int getInitStatus() {
        return BillAcceptorStatus.INITIALIZE;
    }

    @Override
    protected int getPollCommand() {
        return BillAcceptorCommand.POLL;
    }

    @Override
    protected int getResetCommand() {
        return BillAcceptorCommand.RESET;
    }

    @Override
    protected String getStatusDescription(int state) {
        return BillAcceptorStatus.getDescription(state);
    }

    @Override
    protected String getCommandDescription(int command) {
        return BillAcceptorCommand.getDescription(command);
    }

    @Override
    protected void powerStateChanged() {
        if (powerNotify == JposConst.JPOS_PN_ENABLED) {
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), powerState));
        }
    }

    /**
     * Set current peripheral status
     * @param status New peripheral status
     */
    @Override
    protected void statusChanged() {
        super.statusChanged();
        switch (status & 0xFF00) {
            case BillAcceptorStatus.ACCEPTING:
            case BillAcceptorStatus.REJECTING:
            case BillAcceptorStatus.STACKING:
            case BillAcceptorStatus.RETURNING:
                break;
            case BillAcceptorStatus.EVENT_ESCROW_POSITION:
                try {
                    // Stack or return bill from escrow possition
                    int billType = status & 0x0FF;
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
                        stackBill();
                    } else {
                        returnBill();
                    }
                } catch (JposException e) {
                    state = JposConst.JPOS_S_ERROR;
//                    fireEvent(new ErrorEvent(eventCallbacks.getEventSource(),
//                            e.getErrorCode(), 0,
//                            JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));
                }
                break;
            case BillAcceptorStatus.EVENT_RETURNED:
                break;
            case BillAcceptorStatus.EVENT_STACKED:
                int billType = status & 0x0FF;
                countBill(billType);
                if (realTimeDataEnabled) {
                    acceptDepositCounts();
                }
                fireEvent(new DataEvent(eventCallbacks.getEventSource(), 0));
                break;
            case BillAcceptorStatus.DROP_CASSETTE_POSITION:
                if (status != fullStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_FULL));
                    fullStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case BillAcceptorStatus.DROP_CASSETTE_FULL:
                if (status != fullStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_NEARFULL));
                    fullStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case BillAcceptorStatus.FAIL_GENERIC:
            case BillAcceptorStatus.BILL_VALIDATOR_JAMMED:
            case BillAcceptorStatus.CASSETTE_JAMMED:
            case BillAcceptorStatus.CHEATED:
            case BillAcceptorStatus.PAUSE:
                if (status != jamStatus) {
                    fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                            BillAcceptorConst.BACC_STATUS_JAM));
                    jamStatus = status;
                }
                state = JposConst.JPOS_S_ERROR;
                break;
            case BillAcceptorStatus.IDLING:
            case BillAcceptorStatus.UNIT_DISABLED:
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
            case BillAcceptorStatus.DEVICE_BUSY:
                state = JposConst.JPOS_S_BUSY;
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
            for (int i = 0; i < 24; i++) {
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
            for (int i = 0; i < 24; i++) {
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
            for (int i = 0; i < 24; i++) {
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
            System.arraycopy(ints, 0, cashCounts, 0, 24);
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
        waitStatus(BillAcceptorStatus.UNIT_DISABLED, DeviceTiming.NON_RESPONSE);
        // Initialize parameters
        identification();
        poll();
        requestBillTable();
        poll();
        // Set parameters
        enableSecurity();
        poll();
    }

    /**
     * Peripheral identification command
     */
    protected void identification()
            throws JposException {
        int identData[] = execute(BillAcceptorCommand.IDENTIFICATION, null);
        if (identData.length == 34) {
            // Part Number - 15 bytes, ASCII characters
            partNumber = dataToAscii(identData, 0, 15);

            // Serial Number - 12 bytes Factory assigned serial number, ASCII characters
            serialNumber = dataToAscii(identData, 15, 12);

            // Asset Number - 7 bytes, unique to every Bill Validator, binary data
            assetNumber = dataToHex(identData, 27, 7);

        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Request bill table
     */
    protected void requestBillTable() throws JposException {
        int billTableData[] = execute(BillAcceptorCommand.GET_BILL_TABLE, null);
        if (billTableData.length == 120) {
            // The 120 - byte string consists of 24 five-byte words.
            for (int i = 0; i < 24; i++) {
                // Byte 1 of word  – most significant digit of the denomination.
                int denom = billTableData[i * 5];
                // Bytes 2-4 of word – country code in ASCII characters.
                String s1 = dataToAscii(billTableData, i * 5 + 1, 3);
                String s = s1.toString().toUpperCase();
                // Correct Russian currency codes
                if (s.equals("RUS") || s.equals("RUR")) {
                    s = "RUB";
                }
                // Byte 5 of word –decimal placement or proceeding zeros.
                // If bit D7 is 0, the bits D0-D6 indicate the number of proceeding zeros.
                // If bit D7 is 1, the bits D0-D6 indicates the decimal point position
                // starting from the right and moving to the left.
                int decimal = billTableData[i * 5 + 4];
                int l = 1;
                for (int j = 0; j < (decimal & 0x7F); j++) {
                    l = l * 10;
                }
                if ((decimal & 0x80) == 0) {
                    denom = (denom * l);
                } else {
                    denom = (denom / l);
                }
                depositCodeList[i] = s;
                depositCashList[i] = denom;
                if ((s != null ? !(s.isEmpty() || s.equals("BAR")) : false)
                        && (currencyCode == null ? true : currencyCode.isEmpty())) {
                    currencyCode = s;
                }
            }
            setCashCounts(cashCountsBuffer);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Request sequence status
     * @return Sequence enabled status
     */
    protected int[] requestStatus() throws JposException {
        int billStatusData[] = execute(BillAcceptorCommand.GET_STATUS, null);
        if (billStatusData.length == 6) {
            return billStatusData;
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Enable security
     */
    protected void enableSecurity() throws JposException {
        int securityData[] = {0x7F, 0xFF, 0xFF};
        execute(BillAcceptorCommand.SET_SECURITY, securityData);
    }

    /**
     * Disable security
     */
    protected void disableSecurity() throws JposException {
        int securityData[] = {0x00, 0x00, 0x00};
        execute(BillAcceptorCommand.SET_SECURITY, securityData);
    }

    /**
     * Enable sequence
     */
    protected void enableSequence() throws JposException {
        if (status == BillAcceptorStatus.IDLING) {
            return;
        }
        int enableData[] = {0x7F, 0xFF, 0xFF, 0x7F, 0xFF, 0xFF};
        if (getDepositStatus() != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) {
            waitStatus(BillAcceptorStatus.UNIT_DISABLED, DeviceTiming.NON_RESPONSE);
            execute(BillAcceptorCommand.ENABLE_BILL_TYPES, enableData);
            waitStatus(BillAcceptorStatus.IDLING, DeviceTiming.NON_RESPONSE);
        } else {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Disable sequence
     */
    protected void disableSequence() throws JposException {
        if (status == BillAcceptorStatus.UNIT_DISABLED) {
            return;
        }
        int disableData[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        execute(BillAcceptorCommand.ENABLE_BILL_TYPES, disableData);
        if (getDepositStatus() != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) {
            waitStatus(BillAcceptorStatus.UNIT_DISABLED, DeviceTiming.DELIVERY_TIME);
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
        execute(BillAcceptorCommand.STACK, null);
    }

    /**
     * This command causes the Bill Validator to return a bill in escrow
     * position to the customer.
     * COMMAND message is returned.
     */
    protected void returnBill() throws JposException {
        execute(BillAcceptorCommand.RETURN, null);
    }

    /**
     * This command allows the Controller to hold Bill Validator in a state
     * Escrow during 10 s. After this time the Controller should send the STACK
     * or RETURN command. For continued holding in an Escrow state it is necessary
     * to resend this command. Otherwise Bill Validator will execute return
     * of a bill.
     */
    protected void holdBill() throws JposException {
        execute(BillAcceptorCommand.HOLD, null);
    }

    /**
     * Get cash counts buffer
     */
    private String getCashCounts() {
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            StringBuilder s = new StringBuilder(240);
            for (int i = 0; i < 24; i++) {
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
            int counts[] = new int[24];
            for (int i = 0; i < 24; i++) {
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
    private static final int BILL_VALIDATOR = 0x03;
    private static final int SERVICE_VERSION = 1013000;
    protected String partNumber = "";
    protected String serialNumber = "";
    protected String assetNumber = "";
    protected int jamStatus = 0;
    protected int fullStatus = 0;
    protected boolean realTimeDataEnabled = false;
    protected String currencyCode = null;
    protected int depositAmount = 0;
    protected String cashCountsBuffer = null;
    protected final int cashCounts[] = new int[24];
    protected final int depositCounts[] = new int[24];
    protected final int unacceptedCounts[] = new int[24];
    protected final int depositCashList[] = new int[24];
    protected final String depositCodeList[] = new String[24];
    protected String restrictedList[] = null;
}
