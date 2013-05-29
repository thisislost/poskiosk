/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.cashcode;

/**
 * Timing Specifications
 * @author Maxim
 */
class DeviceTiming {

    /**
     * The maximum time allowed between bytes in a block transmission, ms
     */
    public static final int INTER_BYTE = 20;

    /**
     * The maximum time Peripheral will take to respond to a valid communication
     */
    public static final int RESPONSE = 100;

    /**
     * The minimum time of sending signal BUS RESET
     */
    public static final int BUS_RESET = 100;

    /**
     * The maximum non-response time
     */
    public static final int NON_RESPONSE = 5000;

    /**
     * The interval of time between two commands Poll
     */
    public static final int POLL = 200;

    /**
     * The interval of time between confirmation ACK or NAK and next command
     */
    public static final int FREE = 50;

    /**
     * The interval of time between two commands Reset when non-response Peripheral
     */
    public static final int RESET_NON_RESPONSE = 10000;

    /**
     * Pause between communication error and trying to send message again
     */
    public static final int REPEAT_PAUSE = 200;

    /**
     * Time for delivery bill from escrow position 50 sec
     */
    public static final int DELIVERY_TIME = 50000;

    /**
     * Hold timeout for escrow position 10 sec
     */
    public static final int HOLD_ESCROW = 10000;

}
