/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.cashcode;

/**
 * Bill Validator commands
 * @author Maxim
 */
public class BillAcceptorCommand {

    /**
     * Command for Bill Validator to self-reset
     */
    public static final int RESET = 0x30;

    /**
     * Request for Bill Validator set-up status
     */
    public static final int GET_STATUS = 0x31;

    /**
     * Sets Bill Validator Security Mode. Command is followed by set-up data.
     * See command format
     */
    public static final int SET_SECURITY = 0x32;

    /**
     * Request for Bill Validator activity Status
     */
    public static final int POLL = 0x33;

    /**
     * Indicates Bill Type enable or disable. Command is followed by set-up data.
     * See command format
     */
    public static final int ENABLE_BILL_TYPES = 0x34;

    /**
     * Sent by Controller to send a bill in escrow to the drop cassette
     */
    public static final int STACK = 0x35;

    /**
     * Sent by Controller to return a bill in escrow
     */
    public static final int RETURN = 0x36;

    /**
     * Request for Software Part Number, Serial Number,  Asset Number
     */
    public static final int IDENTIFICATION = 0x37;

    /**
     * Command for holding of Bill Validator in Escrow state
     */
    public static final int HOLD = 0x38;

    /**
     * Command for settings the barcode format and number of characters
     */
    public static final int SET_BARCODE_PARAMETERS = 0x39;

    /**
     * Command for retrieving barcode data if barcode coupon is found.
     * If this command is sent when barcode coupon is not found the Bill
     * Validator returns ILLEGAL COMMAND  response.
     */
    public static final int EXTRACT_BARCODE_DATA = 0x3A;

    /**
     * Request for bill type description
     */
    public static final int GET_BILL_TABLE = 0x41;

    /**
     * Request for Bill Validator’s firmware CRC32.
     */
    public static final int GET_CRC32_OF_THE_CODE = 0x51;

    /**
     * Command for transition to download mode.
     */
    public static final int DOWNLOAD = 0x50;

    /**
     * Command for retrieving full information about acceptance performance.
     */
    public static final int REQUEST_STATISTICS = 0x60;

    /**
     * Get command code description
     * @param code
     * @return
     */
    public static String getDescription(int code) {
        switch (code) {
            case RESET: return "Reset";
            case GET_STATUS: return "Get status";
            case SET_SECURITY: return "Set security mode";
            case POLL: return "Poll";
            case ENABLE_BILL_TYPES: return "Enable bill types";
            case STACK: return "Stack";
            case RETURN: return "Return";
            case IDENTIFICATION: return "Identification";
            case HOLD: return "Hold";
            case SET_BARCODE_PARAMETERS: return "Set barcode parameters";
            case EXTRACT_BARCODE_DATA: return "Extract barcode data";
            case GET_BILL_TABLE: return "Get bill table";
            case GET_CRC32_OF_THE_CODE: return "Get CRC32 of the code";
            case DOWNLOAD: return "Download";
            case REQUEST_STATISTICS: return "Request statistics";
            default: return "Unknown command";
        }
    }

}
