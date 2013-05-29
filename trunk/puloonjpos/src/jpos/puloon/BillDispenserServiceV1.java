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
public class BillDispenserServiceV1 extends BillDispenserService {

    //--------------------------------------------------------------------------
    // Protected and private methods
    //
    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        // Default cassette type
        cassetteType[0] = 0;
        cassetteType[1] = 1;
        cassetteType[2] = -1;
        cassetteType[3] = -1;
        super.open(string, ec);
    }
    /**
     * Check jam status
     * @return
     */
    protected int getJamStatus() {
        // Dispenser sensors status
        if ((dispenserStatus & (CHK_SENSOR_1 | CHK_SENSOR_2 | DIV_SENSOR_1
                | DIV_SENSOR_2 | EJT_SENSOR | EXIT_SENSOR | SOL_SENSOR
                | CHK_SENSOR_3 | CHK_SENSOR_4 )) != 0) {
            // Mechanical fault has occurred.
            return BillDispenserConst.BDSP_STATUS_JAM;
        }
        return BillDispenserConst.BDSP_STATUS_JAMOK;
    }

    /**
     * Check empty status
     */
    protected int getEmptyStatus() {
        // Cassette near end status
        if ((dispenserStatus & (UPPER_EXISTS | LOWER_EXISTS | REJECT_TRAY)) != 0) {
            // Some cash slots are nearly empty.
            return BillDispenserConst.BDSP_STATUS_EMPTY;
        }
        // Cassette near end status
        if (((dispenserStatus & UPPER_NEAR_END) != 0)
                || ((dispenserStatus & LOWER_NEAR_END) != 0)) {
            // Some cash slots are nearly empty.
            return BillDispenserConst.BDSP_STATUS_NEAREMPTY;
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
        char[] result = execute((char) CMD_STATUS, null, false, 0);
        status = result[1] - 0x30;
        dispenserStatus = ((int) result[2] & 0x7F) | (((int) result[3] & 0x7F) << 8);
        if (getJamStatus() != jamStatus) {
            jamStatus = getJamStatus();
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), jamStatus));
        }
        if (getEmptyStatus() != emptyStatus) {
            emptyStatus = getEmptyStatus();
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), emptyStatus));
        }
        cassetteType[0] = (dispenserStatus & UPPER_EXISTS) != 0 ? -1 : 0;
        cassetteType[1] = (dispenserStatus & LOWER_EXISTS) != 0 ? -1 : 1;
    }

    /**
     * Reset device
     * @return Peripheral state
     */
    protected void reset() throws JposException {
        char result[] = execute(CMD_RESET, null, false, RESET_FREE_TIME);
        if (result == null ? false : result.length > 0) {
            status = result[0] - 0x30;
            if ((status != 0) && (status != 1)) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getStatusDescription(status));
            }
        }
    }

    /**
     * Convert counts to chars
     * @param chars
     * @param i
     * @param count
     */
    private void charsFromCount(char[] chars, int i, int count) {
        int mod = count % 10;
        chars[i] = (char) ((count - mod)/10 + 0x30);
        chars[i + 1] = (char) (mod + 0x30);
    }

    /**
     * Convert chars to counts
     * @param chars
     * @param i
     * @return
     */
    private int charsToCount(char[] chars, int i) {
        return (chars[i] - 0x30) * 10 + (chars[i + 1] - 0x30);
    }

    /**
     * Dispense cash
     * @param cashCounts
     * @throws JposException
     */
    protected void dispense(int counts[]) throws JposException {
        // Define count digits
        for (int i = 0; i < 4; i++) {
            if ((counts[i] > 0) && (cassetteType[i] < 0)) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL,
                        "The cash counts was illegal for the current exit");
            }
        }
        char para[] = null;
        char cmd = 0x00;
        if (currentExit == 2) {
            cmd = CMD_TEST_DISPENSE;
        } else {
            if ((counts[0] > 0) && (counts[1] > 0)) {
                cmd = CMD_DISPENSE;
                para = new char[4];
                charsFromCount(para, 0, counts[0]);
                charsFromCount(para, 2, counts[1]);
            } else if (counts[0] > 0) {
                cmd = CMD_DISPENSE_UPPER;
                para = new char[2];
                charsFromCount(para, 0, counts[0]);
            } else if (counts[1] > 0) {
                cmd = CMD_DISPENSE_LOWER;
                para = new char[2];
                charsFromCount(para, 0, counts[1]);
            }
        }
        if (cmd == 0) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    "The cash counts was illegal for the current exit");
        }
        char[] result = execute(cmd, para, false, 0);
        int exits[] = new int[4];
        int rejects[] = new int[4];
        boolean nearEnd = false;
        for (int i = 0; i < 4; i++) {
            exits[i] = 0;
            rejects[i] = 0;
        }
        switch (cmd) {
            case CMD_DISPENSE_UPPER:
                if (result.length == 8) {
                    exits[0] = charsToCount(result, 2);
                    rejects[0] = charsToCount(result, 6);
                } else if (result.length == 6) {
                    exits[0] = charsToCount(result, 0);
                    rejects[0] = charsToCount(result, 2);
                }
                status = result[4] - 0x30;
                if (result[5] > 0) {
                    nearEnd = true;
                }
                break;
            case CMD_DISPENSE_LOWER:
                if (result.length == 8) {
                    exits[1] = charsToCount(result, 2);
                    rejects[1] = charsToCount(result, 6);
                } else if (result.length == 6) {
                    exits[1] = charsToCount(result, 0);
                    rejects[1] = charsToCount(result, 2);
                }
                status = result[4] - 0x30;
                if (result[5] > 0) {
                    nearEnd = true;
                }
                break;
            case CMD_DISPENSE:
                if (result.length == 15) {
                    exits[0] = charsToCount(result, 2);
                    exits[1] = charsToCount(result, 6);
                    rejects[0] = charsToCount(result, 11);
                    rejects[1] = charsToCount(result, 13);
                } else if (result.length == 11) {
                    exits[0] = charsToCount(result, 0);
                    exits[1] = charsToCount(result, 4);
                    rejects[0] = charsToCount(result, 2);
                    rejects[1] = charsToCount(result, 6);
                }
                status = result[8] - 0x30;
                if (((result[9] - 0x30) > 0) ||
                    ((result[10] - 0x30) > 0)) {
                    nearEnd = true;
                }
                break;
        }
        for (int i = 0; i < 4; i++) {
            cashCounts[i] = cashCounts[i] - exits[i];
            rejectCounts[i] = rejectCounts[i] + rejects[i];
        }
        if ((status != 0) && (status != 1)) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getStatusDescription(status));
        }
    }

    /**
     * Purge bills from transport
     * @throws JposException
     */
    protected void purge() throws JposException {
        char[] result = execute(CMD_RESET, null, false, RESET_FREE_TIME);
        if (result == null ? false : result.length > 0) {
            status = result[0] - 0x30;
            if ((status != 0) && (status != 1)) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getStatusDescription(status));
            }
        }
    }

    /**
     * Get status description
     * @return State description
     */
    protected String getStatusDescription(int status) {
        switch (status) {
            case 0x00:
                return "Good";
            case 0x01:
                return "Normal stop";
            case 0x02:
                return "Pickup error";
            case 0x03:
                return "Cassette0 check sensor jam";
            case 0x04:
                return "Overflow bill";
            case 0x05:
                return "Exit or Eject sensor jam";
            case 0x06:
                return "Divert sensor jam";
            case 0x07:
                return "Undefined command";
            case 0x08:
                return "Cassette0 Bill End";
            case 0x0A:
                return "Check sensor or Eject sensor count is mistmached";
            case 0x0B:
                return "Dispensing too many notes for one transaction";
            case 0x0C:
                return "Counting Error (between DIV Sensor and EJT Sensor)";
            case 0x0D:
                return "Counting Error(between EJT Sensor and EXIT Sensor)";
            case 0x0E:
                return "Sensor error";
            case 0x0F:
                return "Reject Tray is not recognized";
            case 0x10:
                return "Cassette1 Bill End ";
            case 0x11:
                return "Motor Stop";
            case 0x12:
                return "Timeout occurs between Check sensors and Eject sensor";
            case 0x13:
                return "Timeout occurs between Divert sensors and Eject sensor";
            case 0x14:
                return "Over Reject";
            case 0x15:
                return "Cassette0 is not recognized";
            case 0x16:
                return "Cassette1 is not recognized";
            case 0x17:
                return "Dispensing timeout";
            case 0x18:
                return "Eject sensor jam";
            case 0x19:
                return "Diverter is not operated or solenoid sensor error";
            case 0x1A:
                return "Solinoid Sensor error (diverter is abnormal)";
            case 0x1B:
                return "The counting bills on Divert and Check sensor are mistmatched";
            case 0x1C:
                return "Cassette1 check sensor jam";
            case 0x1D:
                return "The counting bills on Eject and Check sensor are mistmatched";
            case 0x1E:
                return "Purge error (reverse jam)";
            case 0x1F:
                return "The bill is dispensed from the wrong cassette";
            case 0x20:
                return "Timeout occurs between Check and Divert sensor";
        }
        return "Unknown status";
    }
    //--------------------------------------------------------------------------
    // Variables
    //
    protected int dispenserStatus = 0;
    //--------------------------------------------------------------------------
    // Constants
    //
    /**
     * Dispenser status mask
     */
    private static final int CHK_SENSOR_1 = 0x0001;
    private static final int CHK_SENSOR_2 = 0x0002;
    private static final int DIV_SENSOR_1 = 0x0004;
    private static final int DIV_SENSOR_2 = 0x0008;
    private static final int EJT_SENSOR = 0x0010;
    private static final int EXIT_SENSOR = 0x0020;
    private static final int UPPER_NEAR_END = 0x0040;
    private static final int SOL_SENSOR = 0x0100;
    private static final int UPPER_EXISTS = 0x0200;
    private static final int LOWER_EXISTS = 0x0400;
    private static final int CHK_SENSOR_3 = 0x0800;
    private static final int CHK_SENSOR_4 = 0x1000;
    private static final int LOWER_NEAR_END = 0x2000;
    private static final int REJECT_TRAY = 0x4000;
    /**
     * Communication ID
     */
    private static final char ID_CHAR = 0x50;

    /*
     * Commands codes
     */
    private static final char CMD_RESET = 0x44;
    private static final char CMD_DISPENSE_UPPER = 0x45;
    private static final char CMD_DISPENSE_LOWER = 0x55;
    private static final char CMD_DISPENSE = 0x56;
    private static final char CMD_STATUS = 0x46;
    private static final char CMD_ROM_VERSION = 0x47;
    private static final char CMD_TEST_DISPENSE = 0x76;
    /**
     * Reset free time
     */
    protected static final int RESET_FREE_TIME = 2000;
}
