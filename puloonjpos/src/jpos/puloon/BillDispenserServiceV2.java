/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.puloon;

import jpos.BillDispenserConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.events.StatusUpdateEvent;
import jpos.services.EventCallbacks;

/**
 *
 * @author Maxim
 */
public class BillDispenserServiceV2 extends BillDispenserService {

    //--------------------------------------------------------------------------
    // Protected and private methods
    //
    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        // Default cassette type
        cassetteType[0] = 0;
        cassetteType[1] = 1;
        cassetteType[2] = 2;
        cassetteType[3] = 3;
        super.open(string, ec);
    }
    /**
     * Check jam status
     * @return
     */
    protected int getJamStatus() {
        // Dispenser sensors status
        if ((dispenserStatus & (DISP_DIV_L | DISP_DIV_R | DISP_EJT | DISP_EXT
                | DISP_SOL | DISP_RVST_L | DISP_RVST_R)) != 0) {
            // Mechanical fault has occurred.
            return BillDispenserConst.BDSP_STATUS_JAM;
        }
        // Cassette sensors status
        for (int i = 0; i < 4; i++) {
            if ((cassetteStatus[i] & (CASS_CHK_L | CASS_CHK_R | CASS_CB)) != 0) {
                // Mechanical fault has occurred.
                return BillDispenserConst.BDSP_STATUS_JAM;
            }
        }
        // Logical status jam
        switch (status) {
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x09:
            case 0x0A:
            case 0x0B:
            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x27:
            case 0x28:
            case 0x2A:
                return BillDispenserConst.BDSP_STATUS_JAM;
        }
        return BillDispenserConst.BDSP_STATUS_JAMOK;
    }

    /**
     * Check empty status
     */
    protected int getEmptyStatus() {
        // Cassette end status
        if ((dispenserStatus & DISP_RJT) != 0) {
            // Reject cassette is not exists
            return BillDispenserConst.BDSP_STATUS_EMPTY;
        }
        for (int i = 0; i < 4; i++) {
            if ((cassetteStatus[i] & CASS_EXISTS) == 0) {
                // Some cash slots are empty.
                return BillDispenserConst.BDSP_STATUS_EMPTY;
            }
        }
        // Cassette near end status
        for (int i = 0; i < 4; i++) {
            if ((cassetteStatus[i] & CASS_NEAR_END) != 0) {
                // Some cash slots are nearly empty.
                return BillDispenserConst.BDSP_STATUS_NEAREMPTY;
            }
        }
        // Logical status empty
        switch (status) {
            case 0x25:
                return BillDispenserConst.BDSP_STATUS_NEAREMPTY;
            case 0x24:
            case 0x26:
            case 0x2C:
                return BillDispenserConst.BDSP_STATUS_EMPTY;
        }
        return BillDispenserConst.BDSP_STATUS_EMPTYOK;
    }

    /**
     * Get ID char for parent device
     * @return
     */
    protected int getIdChar() {
        return ID_CHAR;
    }

    /**
     * Refresh current status
     * @return Peripheral state
     */
    protected void poll() throws JposException {
        char[] result = execute(CMD_STATUS, null, true, 0);
        status = result[0] - 0x20;
        dispenserStatus = ((int) result[1] & 0x3F); // | (((int) result[2] & 0x3F) << 8);
        for (int i = 0; i < 4; i++) {
            cassetteStatus[i] = (int) result[i * 4 + 2] & 0x3F;
            cassetteType[i] = result[i * 4 + 3] - 0x31;
            cassetteBillThickness[i] = (int) result[i * 4 + 4] - 0x20;
            cassetteBillLength[i] = (int) result[i * 4 + 5] - 0x20;
        }
        if (getJamStatus() != jamStatus) {
            jamStatus = getJamStatus();
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), jamStatus));
        }
        if (getEmptyStatus() != emptyStatus) {
            emptyStatus = getEmptyStatus();
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), emptyStatus));
        }
    }

    /**
     * Reset device
     * @return Peripheral state
     */
    protected void reset() throws JposException {
        execute(CMD_RESET, null, false, false, RESET_FREE_TIME);
//        char[] result = execute(CMD_RESET, null, true, RESET_FREE_TIME);
//        status = result[0] - 0x20;
//        if (status != 0) {
//            throw new JposException(JposConst.JPOS_E_FAILURE,
//                    getStatusDescription(status));
//        }
        dispenseSerialNumber = 0;
    }

    /**
     * Dispense cash
     * @param cashCounts
     * @throws JposException
     */
    protected void dispense(int counts[]) throws JposException {
        char[] para = new char[7];
        for (int i = 0; i < 4; i++) {
            if ((counts[i] > 0) && (cassetteType[i] < 0)) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL,
                        "The cash counts was illegal for the current exit");
            }
            para[i] = (char) (0x20 + counts[i]);
        }
        dispenseSerialNumber++;
        if ((dispenseSerialNumber + 0x20) > 0x7F)
            dispenseSerialNumber = 0;
        para[4] = (char)(dispenseSerialNumber + 0x20);
        for (int i = 5; i < 7; i++) {
            para[i] = 0x20;
        }
        char cmd = CMD_DISPENSE;
        if (currentExit == 2) {
            cmd = CMD_TEST_DISPENSE;
        }
        char[] result = execute(cmd, para, true, 0);
        status = result[0] - 0x20;
        for (int i = 0; i < 4; i++) {
            cashCounts[i] = cashCounts[i] - (result[i * 3 + 2] - 0x20);
            rejectCounts[i] = rejectCounts[i] + (result[i * 3 + 3] - 0x20);
        }
        if (status != 0) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getStatusDescription(status));
        }
    }

    /**
     * Purge bills from transport
     * @throws JposException
     */
    protected void purge() throws JposException {
        char[] result = execute(CMD_PURGE, null, true, RESET_FREE_TIME);
        status = result[0] - 0x20;
        for (int i = 0; i < 4; i++) {
            cashCounts[i] = cashCounts[i] - (result[i * 3 + 2] - 0x20);
            rejectCounts[i] = rejectCounts[i] + (result[i * 3 + 3] - 0x20);
        }
        if (status != 0) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getStatusDescription(status));
        }
    }

    /**
     * Get status description
     * @return State description
     */
    protected String getStatusDescription(int status) {
        switch (status) {
            case 0x00:
                return "Ok";
            case 0x01:
                return "Banknote Pick Up Error";
            case 0x02:
                return "TimeOut on the path between CHK Sensor and RVDT Start Sensor";
            case 0x03:
                return "TimeOut on the path between DIV Sensor and EJT Sensor";
            case 0x04:
                return "TimeOut on the path between EJT Sensor and EXIT Sensor";
            case 0x05:
                return "A note Staying at EXT Sensor";
            case 0x06:
                return "Ejecting the note suspected as rejected";
            case 0x07:
                return "Abnormal note management (Flow Processing Error Inside)";
            case 0x08:
                return "Abnormal note management (Flow Processing Error Inside)";
            case 0x09:
                return "Jamming on EJT Sensor";
            case 0x0A:
                return "Jamming on EXT Sensor";
            case 0x0B:
                return "Detecting notes on the path before start of pick-up";
            case 0x0C:
                return "Dispensing too many notes for one transaction"; // (Default limit: 120 notes including all the rejected)
            case 0x0D:
                return "Rejecting too many notes for one transaction";  // (Default limit: 20 notes)
            case 0x0E:
                return "Abnormal termination during purge execution";
            case 0x20:
                return "Detecting sensor trouble or abnormal material before start";
            case 0x21:
                return "Detecting sensor trouble or abnormal material before start";
            case 0x22:
                return "Detecting trouble of solenoid operation before dispense";
            case 0x23:
                return "Detecting trouble in motor or slit sensor before dispense";
            case 0x24:
                return "Detecting no cassette0 requested to dispense banknotes";
            case 0x25:
                return "Detecting Near-end status in the cassette requested to dispense"; // (When Near-end detection mode is turned on)
            case 0x26:
                return "Detecting no reject tray before start or for operation";
            case 0x27:
                return "Failed to calibrate sensors";
            case 0x28:
                return "Jamming or sensor failure in the Cash Cassette";
            case 0x29:
                return "More banknotes than the requested are dispensered.";
            case 0x2A:
                return "TimeOut on the path between RVDT Start Sensor and DIV Sensor";
            case 0x2B:
                return "Dispensing is not terminated within 90 seconds.";
            case 0x2C:
                return "Detecting no cassette1 requested to dispense banknotes";
            case 0x30:
                return "Recogniging abnormal Command";
            case 0x31:
                return "Recognizing abnormal Parameters on the command";
            case 0x32:
                return "Not to give Verify command on Reset after downloading program";
            case 0x33:
                return "Failure of writing on program area";
            case 0x34:
                return "Failure of Verify";
        }
        return "Unknown status";
    }
    //--------------------------------------------------------------------------
    // Variables
    //
    protected int dispenserStatus = 0;
    protected final int[] cassetteStatus = new int[4];
    protected final int[] cassetteBillThickness = new int[4];
    protected final int[] cassetteBillLength = new int[4];
    private int dispenseSerialNumber = 0;
    //--------------------------------------------------------------------------
    // Constants
    //
    /**
     * Dispenser status mask
     */
    private static final int DISP_DIV_L = 0x0001;
    private static final int DISP_DIV_R = 0x0002;
    private static final int DISP_EJT = 0x0004;
    private static final int DISP_EXT = 0x0008;
    private static final int DISP_RJT = 0x0010;
    private static final int DISP_SOL = 0x0020;
    private static final int DISP_RVST_L = 0x0100;
    private static final int DISP_RVST_R = 0x0200;
    /**
     * Cassette status mask
     */
    private static final int CASS_CHK_L = 0x0001;
    private static final int CASS_CHK_R = 0x0002;
    private static final int CASS_EXISTS = 0x0004;
    private static final int CASS_NEAR_END = 0x0008;
    private static final int CASS_CB = 0x0010;
    /**
     * Communication ID
     */
    private static final int ID_CHAR = 0x30;

    /*
     * Commands codes
     */
    private static final char CMD_RESET = 0x44;
    private static final char CMD_STATUS = 0x50;
    private static final char CMD_PURGE = 0x51;
    private static final char CMD_DISPENSE = 0x52;
    private static final char CMD_TEST_DISPENSE = 0x53;
    private static final char CMD_LAST_STATUS = 0x55;
    private static final char CMD_SENSOR_DIAGNOSTICS = 0x58;
    private static final char SET_BILL_OPACITIES = 0x5A;
    private static final char GET_BILL_OPACITIES = 0x5B;
    private static final char SET_BILL_DISPENSE_ORDER = 0x5C;
    private static final char GET_BILL_DISPENSE_ORDER = 0x5D;
    private static final char SET_BILL_LENGTH = 0x5E;
    private static final char GET_BILL_LENGTH = 0x5F;
    private static final char CMD_GO_LOADER = 0x72;
    private static final char CMD_PROGRAM_WRITE = 0x73;
    private static final char CMD_PROGRAM_VERIFY = 0x74;
    /**
     * Reset free time
     */
    protected static final int RESET_FREE_TIME = 2000;
}
