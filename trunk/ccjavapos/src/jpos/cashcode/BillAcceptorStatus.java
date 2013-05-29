/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.cashcode;

/**
 * Bill Validator status codes
 * @author Maxim
 */
class BillAcceptorStatus {

    /**
     * Power Up	The state of Bill Validator after power up
     */
    public final static int POWER_UP  = 0x1000;

    /**
     * Power Up with Bill in Validator  Power up with bill in the Bill Validator.
     * After a RESET command from the Controller Bill Validator returns the bill
     * and continues initializing.
     */
    public final static int POWER_BILL_VALIDATOR = 0x1100;

    /**
     * Power Up with Bill in Stacker    Power up with bill in Stacker (Bill was
     * transported too far to be returned). After the Bill Validator is reset
     * and INITIALIZING is complete, status will immediately change to STACKED(81H)
     * (Credit Recovery feature).
     */
    public final static int POWER_BILL_STACKER = 0x1200;

    /**
     * Initialize	The state, in which Bill Validator executes initialization after
     * RESET command from the Controller.
     */
    public final static int INITIALIZE = 0x1300;

    /**
     * Idling	In this state Bill Validator waits for bill insertion.
     */
    public final static int IDLING = 0x1400;

    /**
     * Accepting	In this state Bill Validator executes scanning of a bill and
     * determines its denomination.
     */
    public final static int ACCEPTING = 0x1500;

    /**
     * Stacking	In this state, the Bill Validator transports a bill from
     * Escrow position to the recycling cassette or to the drop box and remains in
     * this state until the bill is stacked or returned if jammed.
     */
    public final static int STACKING = 0x1700;

    /**
     * Returning	In this state Bill Validator transports a bill from Escrow
     * position to entry bezel and remains in this state until the bill is removed
     * by customer.
     */

    public final static int RETURNING = 0x1800;

    /**
     * Unit Disabled	The Bill Validator has been disabled by the Controller
     * and also the state in which Bill Validator is after initialization
     */
    public final static int UNIT_DISABLED = 0x1900;

    /**
     * Holding	The state, in which the bill is held in Escrow position after
     * the HOLD command from the Controller.
     */
    public final static int HOLDING = 0x1A00;

    /**
     * Device Busy	The state, in which Bill Validator cannot answer a detailed
     * command right now. On expiration of time YH, peripheral is accessible for
     * polling. YH is expressed as multiple of 100 milliseconds.
     */
    public final static int DEVICE_BUSY = 0x1B00;

    /**
     * Generic rejecting code. Always followed by rejection reason int (see below).
     */
    public final static int REJECTING = 0x1C00;

    /**
     * Rejecting due to Insertion	Insertion error
     */
    public final static int REJECTING_INSERTION = 0x1C60;

    /**
     * Rejecting due to Magnetic	Magnetic error
     */
    public final static int REJECTING_MAGNETIC = 0x1C61;

    /**
     * Rejecting due to bill
     * Remaining in the head	Bill remains in the head, and new bill is rejected.
     */
    public final static int REJECTING_BILL = 0x1C62;

    /**
     * Rejecting due to Multiplying	Compensation error/multiplying factor error
     */
    public final static int REJECTING_MULTIPLYING = 0x1C63;

    /**
     * Rejecting due to Conveying	Conveying error
     */
    public final static int REJECTING_CONVEYING = 0x1C64;

    /**
     * Rejecting due to Identification	Identification error
     */
    public final static int REJECTING_IDENTIFICATION = 0x1C65;

    /**
     * Rejecting due to Verification	Verification error
     */
    public final static int REJECTING_VERIFICATION = 0x1C66;

    /**
     * Rejecting due to Optic	Optic error
     */
    public final static int REJECTING_OPTIC = 0x1C67;

    /**
     * Rejecting due to Inhibit	Return by “inhibited denomination” error
     */
    public final static int REJECTING_INHIBIT = 0x1C68;

    /**
     * Rejecting due to Capacity	Capacitance error
     */
    public final static int REJECTING_CAPACITY = 0x1C69;

    /**
     * Rejecting due to Operation	Operation error
     */
    public final static int REJECTING_OPERATION = 0x1C6A;

    /**
     * Rejecting due to Length	Length error
     */
    public final static int REJECTING_LENGTH = 0x1C6C;

    /**
     * Invalid command	Command from the Controller is not valid.
     */
    public final static int INVALID_COMMAND = 0x3000;

    /**
     * Drop Cassette Full	Drop Cassette full condition
     */
    public final static int DROP_CASSETTE_FULL = 0x4100;

    /**
     * Drop Cassette out of position	The Bill Validator has detected the
     * drop cassette to be open or removed.
     */
    public final static int DROP_CASSETTE_POSITION = 0x4200;

    /**
     * Bill Validator Jammed	Bill(s) are jammed in the acceptance path.
     */
    public final static int BILL_VALIDATOR_JAMMED = 0x4300;

    /**
     * Cassette Jammed	A bill are jammed in drop cassette.
     */
    public final static int CASSETTE_JAMMED = 0x4400;

    /**
     * Cheated	The Bill Validator sends this event if the intentions of the
     * user to deceive the Bill Validator are detected.
     */
    public final static int CHEATED = 0x4500;

    /**
     * Pause	The Bill Validator reaches this state when the user tries to
     * insert a bill before the previous bill is stacked. Bill Validator stops
     * motion of the bill until the entry channel is cleared.
     */
    public final static int PAUSE = 0x4600;

    /**
     * Generic Failure codes. Always followed by failure description int (see below).
     */
    public final static int FAIL_GENERIC = 0x4700;

    /**
     * Stack Motor Failure	Drop Cassette Motor failure
     */
    public final static int FAIL_STACK_MOTOR = 0x4750;

    /**
     * Transport Motor Speed Failure	Transport Motor Speed out of range
     */
    public final static int FAIL_TRANSPORT_SPEED = 0x4751;

    /**
     * Transport Motor Failure	Transport Motor failure
     */
    public final static int FAIL_TRANSPORT_MOTOR = 0x4752;

    /**
     * Aligning Motor Failure	Aligning Motor failure
     */
    public final static int FAIL_ALIGNING_MOTOR = 0x4753;

    /**
     *Initial Box Status Failure	Initial cassette Status failure
     */
    public final static int FAIL_INITAL_BOX = 0x4754;

    /**
     * Optic Canal Failure	One of the optic sensors has failed to provide its response.
     */
    public final static int FAIL_OPTIC_CANAL = 0x4755;

    /**
     * Magnetic Canal Failure	Inductive Sensor failure
     */
    public final static int FAIL_MAGNETIC_CANAL = 0x4756;

    /**
    /**
     * Capacitance Canal Failure	Capacitance sensor failed to respond
     */
    public final static int FAIL_CAPACITANCE_CANAL = 0x475F;

    /**
     * Escrow position	Y = bill type (0 to 23)
     */
    public final static int EVENT_ESCROW_POSITION = 0x8000;

    /**
     * Bill stacked	Y = bill type (0 to 23)
     */
    public final static int EVENT_STACKED = 0x8100;

    /**
     * Bill returned	Y = bill type (0 to 23)
     */
    public final static int EVENT_RETURNED = 0x8200;

    /**
     * Get state code description
     * @param code
     * @return
     */
    public static String getDescription(int code) {
        switch (code & 0xFF00) {
            case POWER_UP:
                return "Power Up";
            case POWER_BILL_VALIDATOR:
                return "Power Up with Bill in Validator";
            case POWER_BILL_STACKER:
                return "Power up with bill in Stacker";
            case INITIALIZE:
                return "Initialize";
            case IDLING:
                return "Idling";
            case ACCEPTING:
                return "Accepting";
            case STACKING:
                return "Stacking";
            case RETURNING:
                return "Returning";
            case UNIT_DISABLED:
                return "Unit Disabled";
            case HOLDING:
                return "Holding";
            case DEVICE_BUSY:
                return "Device Busy: " + String.valueOf((code & 0xFF) * 100) + " ms";
            case REJECTING:
                switch (code) {
                    case REJECTING_INSERTION:
                        return "Rejecting due to Insertion";
                    case REJECTING_MAGNETIC:
                        return "Rejecting due to Magnetic";
                    case REJECTING_BILL:
                        return "Rejecting due to bill Remaining in the head";
                    case REJECTING_MULTIPLYING:
                        return "Rejecting due to Multiplying";
                    case REJECTING_CONVEYING:
                        return "Rejecting due to Conveying";
                    case REJECTING_IDENTIFICATION:
                        return "Rejecting due to Identification";
                    case REJECTING_VERIFICATION:
                        return "Rejecting due to Verification";
                    case REJECTING_OPTIC:
                        return "Rejecting due to Optic";
                    case REJECTING_INHIBIT:
                        return "Rejecting due to Inhibit";
                    case REJECTING_CAPACITY:
                        return "Rejecting due to Capacity";
                    case REJECTING_OPERATION:
                        return "Rejecting due to Operation";
                    case REJECTING_LENGTH:
                        return "Rejecting due to Length";
                    default:
                        return "Generic rejecting";
                }
            case INVALID_COMMAND:
                return "Invalid command";
            case DROP_CASSETTE_FULL:
                return "Drop Cassette Full";
            case DROP_CASSETTE_POSITION:
                return "Drop Cassette out of position";
            case BILL_VALIDATOR_JAMMED:
                return "Bill Validator Jammed";
            case CASSETTE_JAMMED:
                return "Cassette Jammed";
            case CHEATED:
                return "Cheated";
            case PAUSE:
                return "Pause";
            case FAIL_GENERIC:
                switch (code) {
                    case FAIL_STACK_MOTOR:
                        return "Stack Motor Failure";
                    case FAIL_TRANSPORT_SPEED:
                        return "Transport Motor Speed Failure";
                    case FAIL_TRANSPORT_MOTOR:
                        return "Transport Motor Failure";
                    case FAIL_ALIGNING_MOTOR:
                        return "Aligning Motor Failure";
                    case FAIL_INITAL_BOX:
                        return "Initial Box Status Failure";
                    case FAIL_OPTIC_CANAL:
                        return "Optic Canal Failure";
                    case FAIL_MAGNETIC_CANAL:
                        return "Magnetic Canal Failure";
                    case FAIL_CAPACITANCE_CANAL:
                        return "Capacitance Canal Failure";
                    default:
                        return "Generic Failure";
                }
            case EVENT_ESCROW_POSITION:
                return "Escrow position";
            case EVENT_STACKED:
                return "Bill stacked";
            case EVENT_RETURNED:
                return "Bill returned";
        }
        return "Unknown state";
    }

}
