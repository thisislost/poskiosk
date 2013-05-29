/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.puloon;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import jpos.BillDispenserConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.events.StatusUpdateEvent;
import jpos.services.BillDispenserService113;
import jpos.services.EventCallbacks;

/**
 * Puloon Bill Dispenser Service
 * @author Maxim
 */
public abstract class BillDispenserService extends DeviceService implements BillDispenserService113 {

    //--------------------------------------------------------------------------
    // Public Bill Dispenser methods
    //
    /**
     * Opens a device for subsequent I/O.
     * @param logicalDeviceName Parameter specifies the device name to open
     * @param ec Callback event handler
     * @throws JposException
     */
    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        // Initialize vriables
        asyncMode = false;
        currentExit = 1;
        jamStatus = BillDispenserConst.BDSP_STATUS_JAMOK;
        emptyStatus = BillDispenserConst.BDSP_STATUS_EMPTYOK;

        // Reject count influence on cash counts estimation
        try {
            rejectInfluence = Double.valueOf(jposEntry.getProp("rejectInfluence").getValueAsString());
        } catch (Exception e) {
        }
        // Bill denominations and currencies
        currencyCode = "";
        for (int i = 0; i < 4; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("cassette");
            sb.append((char) ('0' + i));
            try {
                String s = jposEntry.getProp(sb.toString()).getValueAsString();
                String[] sa = s.split(" ");
                cassetteTypeDenom[i] = Integer.valueOf(sa[0]);
                if (sa.length > 1) {
                    cassetteTypeCurrency[i] = sa[1].trim().toUpperCase();
                } else {
                    cassetteTypeCurrency[i] = "RUB";
                }
                if (currencyCode.length() == 0) {
                    currencyCode = cassetteTypeCurrency[i];
                }
            } catch (Exception e) {
                cassetteTypeDenom[i] = 0;
                cassetteTypeCurrency[i] = "";
            }
        }
        super.open(string, ec);
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

    /**
     * This method is called to set the initial amounts in the Bill Dispenser
     * after initial setup, or to adjust cash counts after replenishment or
     * removal, such as a paid in or paid out operation. This method is called
     * when needed for devices which cannot determine the exact amount of cash
     * in them automatically. If the device can determine the exact amount, then
     * this method call is ignored. The application would first call
     * readCashCounts to get the current counts, and adjust them to the amount
     * being replenished. Then the application will call this method to set the
     * amount currently in the changer.
     * after initial setup
     * @param string
     * @throws JposException
     */
    public void adjustCashCounts(String string) throws JposException {
        String[] as = null;
        if (string != null ? (string.length() > 1
                ? string.charAt(0) == ';' : false) : false) {
            as = string.substring(1).split(",");
        }
        for (int i = 0; i < 4; i++) {
            if (cassetteType[i] < 0 ? false
                    : currencyCode.equals(cassetteTypeCurrency[cassetteType[i]])) {
                cashCounts[i] = 0;
                rejectCounts[i] = 0;
                if (as != null) {
                    for (int j = 0; j < as.length; j++) {
                        if (as[j] != null) {
                            String ar[] = as[j].split(":");
                            if ((ar.length == 2)
                                    && (Integer.valueOf(ar[0]) == cassetteTypeDenom[cassetteType[i]])) {
                                cashCounts[i] = Integer.valueOf(ar[1]);
                                as[j] = null;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears all buffered output data, including all asynchronous output. Also,
     * when possible, halts output that are in progress
     * @throws JposException
     */
    public void clearOutput() throws JposException {
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
     * Dispenses the cash from the Bill Dispenser into the exit specified by CurrentExit
     * The cash dispensed specified by pairs of cash units and counts
     * @param string
     * @throws JposException
     */
    public void dispenseCash(String string) throws JposException {
        // Split cash counts
        String[] as = null;
        if (string != null ? (string.length() > 1
                ? string.charAt(0) == ';' : false) : false) {
            as = string.substring(1).split(",");
        }
        // Cash counts is not specified
        if (as == null) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    "The cash counts was not specified");
        }
        // Fill counts array
        int[] counts = new int[4];
        for (int i = 0; i < 4; i++) {
            counts[i] = 0;
            if (cassetteType[i] < 0 ? false
                    : currencyCode.equals(cassetteTypeCurrency[cassetteType[i]])) {
                for (int j = 0; j < as.length; j++) {
                    if (as[j] != null) {
                        String ar[] = as[j].split(":");
                        if ((ar.length == 2)
                                && (Integer.valueOf(ar[0]) == cassetteTypeDenom[cassetteType[i]])) {
                            counts[i] = Integer.valueOf(ar[1]);
                            as[j] = null;
                            break;
                        }
                    }
                }
                if ((cashCounts[i] - rejectInfluence * rejectCounts[i]) < counts[i]) {
                    throw new JposException(JposConst.JPOS_E_EXTENDED,
                            BillDispenserConst.JPOS_EBDSP_OVERDISPENSE,
                            "The specified cash cannot be dispensed because of a cash shortage");
                }
            }
        }
        // Cash counts is illegal for this exit
        for (int j = 0; j < as.length; j++) {
            if (as[j] != null) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL,
                        "The cash counts was illegal for the current exit");
            }
        }
        // Check for already thread executing
        if (dispenseThread == null ? false : dispenseThread.isAlive()) {
            throw new JposException(JposConst.JPOS_E_BUSY,
                    "Cash cannot be dispensed because asynchronous method is in progress");
        }
        // Dispense cash now or initiate task if AsyncMode is true.
        if (asyncMode) {
            dispenseThread = new Thread(new DispenseTask(counts));
            dispenseThread.start();
        } else {
            trace("Execute sync dispense");
            dispense(counts);
            trace("Exit sync dispense");
        }
    }

    /**
     * If true, the dispenseCash method will be performed asynchronously.
     * If false, this method will be performed synchronously.
     * @return
     * @throws JposException
     */
    public boolean getAsyncMode() throws JposException {
        return asyncMode;
    }

    /**
     * Holds the completion status of the last asynchronous dispense request
     * (i.e., when dispenseCash was called with AsyncMode true).
     * @return
     * @throws JposException
     */
    public int getAsyncResultCode() throws JposException {
        return asyncResultCode;
    }

    /**
     * Holds the completion status of the last asynchronous dispense request
     * (i.e., when dispenseCash was called with AsyncMode true).
     * @return
     * @throws JposException
     */
    public int getAsyncResultCodeExtended() throws JposException {
        return asyncResultCodeExtended;
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
        return true;
    }

    /**
     * If true, the Bill Dispenser can report the condition that some cash slots
     * are empty.
     * @return
     * @throws JposException
     */
    public boolean getCapEmptySensor() throws JposException {
        return false;
    }

    /**
     * If true, the Bill Dispenser can report the occurrence of a mechanical
     * fault in the Bill Dispenser.
     * @return
     * @throws JposException
     */
    public boolean getCapJamSensor() throws JposException {
        return true;
    }

    /**
     * If true, the Bill Dispenser can report the condition that some cash slots
     * are nearly empty.
     * @return
     * @throws JposException
     */
    public boolean getCapNearEmptySensor() throws JposException {
        return true;
    }

    /**
     * Identifies the reporting capabilities of the Device.
     * @return
     * @throws JposException
     */
    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_NONE;
    }

    /**
     * If true, the device accumulates and can provide various statistics
     * regarding usage; otherwise no usage statistics are accumulated.
     * @return
     * @throws JposException
     */
    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    /**
     * If true, then the device's firmware can be updated via the updateFirmware
     * method.
     * @return
     * @throws JposException
     */
    public boolean getCapUpdateFirmware() throws JposException {
        return false;
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
     * Holds the cash units supported in the Bill Dispenser for the currency
     * represented by the CurrencyCode property.
     * @return
     * @throws JposException
     */
    public String getCurrencyCashList() throws JposException {
        StringBuilder s = new StringBuilder(24 * 4);
        if (currencyCode != null ? !currencyCode.isEmpty() : false) {
            for (int i = 0; i < 4; i++) {
                if (cassetteType[i] < 0 ? false
                        : currencyCode.equals(cassetteTypeCurrency[cassetteType[i]])) {
                    if (s.length() > 0) {
                        s.append(",");
                    }
                    s.append(cassetteTypeDenom[cassetteType[i]]);
                }
            }
        }
        return ";" + s.toString();
    }

    /**
     * Contains the active currency code to be used by Bill Dispenser operations.
     * This value is guaranteed to be one of the set of currencies specified by
     * the CurrencyCodeList property.
     * @return
     * @throws JposException
     */
    public String getCurrencyCode() throws JposException {
        return currencyCode;
    }

    /**
     * Holds a list of ASCII three-character ISO 4217 currency codes separated
     * by commas. For example, if the string is “JPY,USD”, then the Bill Dispenser
     * supports both Japanese and U.S. monetary units.
     * @return
     * @throws JposException
     */
    public String getCurrencyCodeList() throws JposException {
        StringBuilder s = new StringBuilder(24 * 4);
        HashSet<String> ks = new HashSet<String>();
        for (int i = 0; i < 4; i++) {
            if (cassetteType[i] >= 0) {
                String curCode = cassetteTypeCurrency[cassetteType[i]];
                if ((curCode.length() > 0) && (!ks.contains(curCode))) {
                    if (s.length() > 0) {
                        s.append(",");
                    }
                    s.append(curCode);
                    ks.add(curCode);
                }
            }
        }
        return s.toString();
    }

    /**
     * Holds the current cash dispensing exit. The value 1 represents the
     * primary exit (or normal exit), while values greater than 1 are considered
     * auxiliary exits. Legal values range from 1 to DeviceExits.
     * @return
     * @throws JposException
     */
    public int getCurrentExit() throws JposException {
        return currentExit;
    }

    /**
     * The number of exits for dispensing cash.
     * @return
     * @throws JposException
     */
    public int getDeviceExits() throws JposException {
        return 2;
    }

    /**
     * Holds the current status of the Bill Dispenser.
     * This property is initialized and kept current while the device is enabled. 
     * If more than one condition is present, then the order of precedence 
     * starting at the highest is: fault, empty, and near empty.
     * @return
     * @throws JposException
     */
    public int getDeviceStatus() throws JposException {
        // Dispenser & cassette sensors status
        if (getJamStatus() != BillDispenserConst.BDSP_STATUS_JAMOK) // Mechanical fault has occurred.
        {
            return getJamStatus();
        }
        if (getEmptyStatus() != BillDispenserConst.BDSP_STATUS_EMPTYOK) // Some cash slots are nearly empty.
        {
            return getEmptyStatus();
        }
        // The current condition of the Bill Dispenser is satisfactory.
        return BillDispenserConst.BDSP_STATUS_OK;
    }

    /**
     * Holds the cash units which may be dispensed to the exit which is denoted
     * by CurrentExit property. The supported cash units are either the same as
     * CurrencyCashList, or a subset of it
     * @return
     * @throws JposException
     */
    public String getExitCashList() throws JposException {
        return getCurrencyCashList();
    }

    /**
     * Contains the type of power notification selection made by the Application.
     * @return
     * @throws JposException
     */
    public int getPowerNotify() throws JposException {
        return JposConst.JPOS_PN_DISABLED;
    }

    /**
     * Identifies the current power condition of the device, if it can be
     * determined.
     * @return
     * @throws JposException
     */
    public int getPowerState() throws JposException {
        return JposConst.JPOS_PS_UNKNOWN;
    }

    /**
     * The format of the string cashCountsis the same ascashCountsin the
     * dispenseCash method. Each unit in cashCounts matches a unit in the
     * CurrencyCashList property, and is in the same order.
     * @param strings
     * @param discrepancy
     * @throws JposException
     */
    public void readCashCounts(String[] strings, boolean[] discrepancy) throws JposException {
        discrepancy[0] = false;
        StringBuilder s = new StringBuilder(240);
        for (int i = 0; i < 4; i++) {
            if (cassetteType[i] < 0 ? false
                    : currencyCode.equals(cassetteTypeCurrency[cassetteType[i]])) {
                if (s.length() > 0) {
                    s.append(",");
                }
                s.append(cassetteTypeDenom[cassetteType[i]]);
                s.append(":");
                s.append(cashCounts[i]);
                if (rejectCounts[i] > 0) {
                    discrepancy[0] = true;
                }
            }
        }
        strings[0] = ";" + s.toString();
    }

    /**
     * Resets the defined resettable statistics in a device to zero.
     * @param statisticsBuffer The data buffer defining the statistics that are
     * to be reset.
     * @param string
     * @throws JposException
     */
    public void resetStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Retrieves the requested statistics from a device.
     * @param statisticsBuffer The data buffer defining the statistics to be
     * retrieved and in which the retrieved statistics are placed.
     * @param strings
     * @throws JposException
     */
    public void retrieveStatistics(String[] strings) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * If true, the dispenseCash method will be performed asynchronously. 
     * If false, this method will be performed synchronously
     * @param bln
     * @throws JposException
     */
    public void setAsyncMode(boolean bln) throws JposException {
        asyncMode = bln;
    }

    /**
     * Contains the active currency code to be used by Bill Dispenser operations.
     * This property is initialized to an appropriate value by the open method.
     * This value is guaranteed to be one of the set of currencies specified by
     * the CurrencyCodeList property.
     * @param string
     * @throws JposException
     */
    public void setCurrencyCode(String string) throws JposException {
        boolean exists = false;
        for (int i = 0; i < 4; i++) {
            if (cassetteType[i] >= 0) {
                String curCode = cassetteTypeCurrency[cassetteType[i]];
                if ((curCode.length() > 0) && (string.equalsIgnoreCase(curCode))) {
                    exists = true;
                }
            }
        }
        if (exists) {
            currencyCode = string.toUpperCase();
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
    }

    /**
     * Holds the current cash dispensing exit. The value 1 represents the
     * primary exit (or normal exit), while values greater than 1 are considered
     * auxiliary exits. Legal values range from 1 to DeviceExits.
     * @param i
     * @throws JposException
     */
    public void setCurrentExit(int i) throws JposException {
        if ((i < 1) || (i > 2)) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        currentExit = i;
    }

    /**
     * Contains the type of power notification selection made by the Application.
     * @param i
     * @throws JposException
     */
    public void setPowerNotify(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * This method updates the firmware of a device with the version of the
     * firmware contained or defined in the file specified by the firmware.
     * @param firmwareFileName Specifies either the name of the file containing
     * the firmware or a file containing a set of firmware files that are to be
     * downloaded into the device.
     * @param string
     * @throws JposException
     */
    public void updateFirmware(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Updates the defined resettable statistics in a device.
     * @param statisticsBuffer The data buffer defining the statistics with
     * values that are to be updated.
     * @param string
     * @throws JposException
     */
    public void updateStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    //--------------------------------------------------------------------------
    // Protected and private methods
    //
    /**
     * Check jam status
     * @return
     */
    protected abstract int getJamStatus();

    /**
     * Check empty status
     */
    protected abstract int getEmptyStatus();

    /**
     * Dispense cash
     * @param cashCounts
     * @throws JposException
     */
    protected abstract void dispense(int counts[]) throws JposException;

    /**
     * Purge bills from transport
     * @throws JposException
     */
    protected abstract void purge() throws JposException;
    //--------------------------------------------------------------------------
    // Local classes
    //
    private boolean trace = true;

    private void trace(String s) {
        if (trace) {
            System.out.println(
                    DateFormat.getDateTimeInstance().format(System.currentTimeMillis())
                    + " " + s);
        }
    }

    /**
     * Thread runner for async cash dispense
     */
    private class DispenseTask implements Runnable {

        int[] counts;

        public DispenseTask(int[] counts) {
            this.counts = counts;
            asyncResultCode = 0;
            asyncResultCodeExtended = 0;
        }

        public void run() {
            try {
                trace("Run async dispense");
                dispense(counts);
                trace("Send async success event");
                asyncResultCode = 0;
                asyncResultCodeExtended = 0;
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                        BillDispenserConst.BDSP_STATUS_ASYNC));
            } catch (JposException jpe) {
                trace("Send async error event");
                asyncResultCode = jpe.getErrorCode();
                asyncResultCode = jpe.getErrorCodeExtended();
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(),
                        BillDispenserConst.BDSP_STATUS_ASYNC));
            }
            trace("Exit async dispense");
        }
    }
    //--------------------------------------------------------------------------
    // Local variables
    //
    protected boolean asyncMode = false;
    protected int currentExit = 1;
    protected String currencyCode = "";
    protected int asyncResultCode = 0;
    protected int asyncResultCodeExtended = 0;
    protected int jamStatus = BillDispenserConst.BDSP_STATUS_JAMOK;
    protected int emptyStatus = BillDispenserConst.BDSP_STATUS_EMPTYOK;
    protected Thread dispenseThread = null;
    protected final int[] cassetteType = new int[4];
    protected final int[] cashCounts = new int[4];
    protected final int[] rejectCounts = new int[4];
    protected final String[] cassetteTypeCurrency = new String[4];
    protected final int[] cassetteTypeDenom = new int[4];
    protected final SynchronousQueue<String> dispenseTaskQueue = new SynchronousQueue<String>();
    protected double rejectInfluence = 0;
    //--------------------------------------------------------------------------
    // Constants
    //
    protected static final int SERVICE_VERSION = 1013000;
}
