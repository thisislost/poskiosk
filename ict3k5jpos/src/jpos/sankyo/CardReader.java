/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.sankyo;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import jpos.JposConst;
import jpos.JposException;

/**
 * Card reader driver
 * @author Maxim
 */
public class CardReader {

    public void open(String portName) throws JposException {
        // Open port
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            CommPort newPort = portIdentifier.open("ICT3K5", NON_RESPONSE);
            if (newPort instanceof SerialPort) {
                port = (SerialPort) newPort;
                port.setSerialPortParams(BAUDRATE,
                        SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                port.setDTR(true);
                port.setRTS(true);
                port.setInputBufferSize(SIZE_BUFFER);
                port.setOutputBufferSize(SIZE_BUFFER);
            } else {
                throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Port is not serial");
            }
            is = port.getInputStream();
            os = port.getOutputStream();
            reset();
            startPolling();
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Device is not found", e);
        }
    }

    public void close() {
        stopPolling();
        port.setDTR(false);
        port.setRTS(false);
        port.close();
    }

    public int getSensor() {
        return sensor;
    }

    public int getStatus() {
        return status;
    }

    public void addListener(CardReaderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CardReaderListener listener) {
        listeners.remove(listener);
    }

    /**
     * Check polling mode
     * @return
     */
    protected boolean isPolling() {
        return pollingTimer != null;
    }

    /**
     * Start polling process
     */
    protected void startPolling() {
        if (!isPolling()) {
            pollingTimer = new Timer();
            pollingTimer.scheduleAtFixedRate(new PollingTask(), POLL_INTERVAL,
                    POLL_INTERVAL);
        }
    }

    /**
     * Stop polling process
     */
    protected void stopPolling() {
        if (isPolling()) {
            pollingTimer.cancel();
            pollingTimer.purge();
            pollingTimer = null;
        }
    }

    /**
     * Refresh current status
     * @return Peripheral state
     */
    public void poll() throws JposException {
        byte[] response = execute("C11");
        if (response.length > 5) {
            if (sensor != response[5]) {
                sensor = response[5];
                for (CardReaderListener listener : listeners) {
                    listener.onChangeSensor();
                }
            }
        }
    }

    /**
     * Update LED indicator on status report
     */
    public void updateLED() throws JposException {
        switch (status) {
            case 0x00:
                if (enabled) {
                    execute("C51"); // Green LED
                } else {
                    execute("C50"); // LED off
                }
                break;
            case 0x01:
                execute("C52"); // Red LED
                break;
            case 0x02:
                execute("C53"); // Yellow LED
                break;
            default:
                execute("C50"); // LED off
            }
    }

    /**
     * Update status
     * @param response 
     */
    protected void updateStatus(byte[] response) throws JposException {
        String s = new String(response);
        int code = Integer.parseInt(s.substring(3, 5), 16);
        int newStatus;
        if (response[0] == 'P') {
            newStatus = code;
        } else {
            newStatus = 0x100 | code;
        }
        if (status != newStatus) {
            if (!statusinupdate) {
                statusinupdate = true;
                try {
                    status = newStatus;
                    updateLED();
                    for (CardReaderListener listener : listeners) {
                        listener.onChangeStatus();
                    }
                } finally {
                    statusinupdate = false;
                }
            }
        }
    }

    /**
     * Reset device
     * @return Peripheral state
     */
    public void reset() throws JposException {
        execute("C0032410000010");
        updateLED();
    }

    /**
     * Enable entry card mode
     * @throws JposException 
     */
    public void enable() throws JposException {
        execute("C:01");
        enabled = true;
        updateLED();
    }

    /**
     * Disable entry card mode
     * @throws JposException 
     */
    public void disable() throws JposException {
        execute("C:1");
        enabled = false;
        updateLED();
    }

    /**
     * Return enable mode
     * @return 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Wait for entry card 
     * @param timeout
     * @throws JposException 
     */
    public void entry(int timeout) throws JposException {
        enabled = true;
        updateLED();
        try {
            execute("C201", timeout);
        } finally {
            enabled = false;
            updateLED();
        }
    }

    /**
     * Eject card
     * @throws JposException 
     */
    public void eject() throws JposException {
        execute("C30", 7000);
    }

    /**
     * Capture card
     * @throws JposException 
     */
    public void capture() throws JposException {
        execute("C31", 7000);
    }

    /**
     * Retrieve card from gate after eject
     * @throws JposException 
     */
    public void retrive() throws JposException {
        execute("C40");
    }

    /**
     * Read track from magnetic strip
     */
    public String readTrack(int track) throws JposException {
        byte[] response = execute("C6" + Integer.toString(track));
        byte[] data = new byte[response.length - 5];
        System.arraycopy(response, 5, data, 0, response.length - 5);
        return new String(data);
    }

    /**
     * Read all tracks from magnetic strip
     */
    public String[] readAllTracks() throws JposException {
        byte[] response = execute("C65");
        byte[] data = new byte[response.length - 5];
        System.arraycopy(response, 5, data, 0, response.length - 5);
        return new String(data).split("~");
    }

    /**
     * Get error description
     * @param error
     * @return 
     */
    protected String getStatusDescription(int status) {
        switch (status) {
            case 0x00:
                return "No card detected within ICRW (including card gate)";
            case 0x01:
                return "Card locates at card Gate";
            case 0x02:
                return "Card locates inside ICRW (Transport)";
            case 0x100:
                return "A given command code is unidentified";
            case 0x101:
                return "Parameter is not correct";
            case 0x102:
                return "Command execution is impossible";
            case 0x103:
                return "Function is not implemented";
            case 0x104:
                return "Command data error";
            case 0x106:
                return "Key for decrypting is not received";
            case 0x110:
                return "Card jam";
            case 0x111:
                return "Shutter error";
            case 0x113:
                return "Irregular card length (LONG)";
            case 0x114:
                return "Irregular card length (SHORT)";
            case 0x115:
                return "Flash Memory Parameter Area CRC error";
            case 0x116:
                return "Card position Move (and Pull out error)";
            case 0x117:
                return "Jam error at retrieve";
            case 0x118:
                return "Two card error";
            case 0x120:
                return "Read Error (Parity error (VRC error))";
            case 0x121:
                return "Read Error (Start sentinel error, end sentinel error or LRC error)";
            case 0x123:
                return "Read Error (No data contents, only start sentinel, end sentinel and LRC)";
            case 0x124:
                return "Read Error (No magnetic stripe or not encoded)";
            case 0x130:
                return "Power Down";
            case 0x131:
                return "DSR signal was turned to OFF";
            case 0x139:
                return "Electric fan breaks down";
            case 0x140:
                return "Pull Out Error";
            case 0x143:
                return "IC Positioning Error";
            case 0x150:
                return "Capture Counter Overflow Error";
            case 0x160:
                return "Abnormal Vcc condition error of IC card or SAM";
            case 0x161:
                return "ATR communication error of IC card or SAM";
            case 0x162:
                return "Invalid ATR error to the selected activation for IC card or SAM";
            case 0x163:
                return "No response error on communication from IC card or SAM";
            case 0x164:
                return "Communication error to IC card or SAM (except for no response)";
            case 0x165:
                return "Not activated error of IC card or SAM";
            case 0x166:
                return "Not supported IC card or SAM error by ICRW (only for non EMV activation)";
            case 0x169:
                return "Not supported IC card or SAM error by EMV2000 (only for EMV activation)";
            case 0x173:
                return "EEPROM error";
            case 0x1B0:
                return "Not received Initialize command";
            case 0x1FF:
                return "Communication error";
        }
        return "Unknown status error";
    }

    /**
     * Task that continue executing each poll interval
     */
    private class PollingTask extends TimerTask {

        /**
         * Implements run task
         */
        public void run() {
            Object monitor = port;
            synchronized (monitor) {
                if (!executed) {
                    try {
                        poll();
                    } catch (Exception e) {
                        // Communication error
                        if (status != 0x1FF) {
                            status = 0x1FF;
                            for (CardReaderListener listener : listeners) {
                                listener.onChangeStatus();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Cancel executed command
     * @throws JposException 
     */
    protected void cancel() throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                os.write(DLE);
                os.write(EOT);
                os.flush();
                port.enableReceiveTimeout(NON_RESPONSE);
                try {
                    int result = NAK;
                    while (result >= 0) {
                        // Ignore if result not DLE and EOT
                        result = is.read();
                        if (result == DLE) {
                            result = is.read();
                            if (result == EOT) {
                                break;
                            }
                        }
                    }
                } finally {
                    port.disableReceiveTimeout();
                }
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported com operation error", e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "I/O operation error", e);
            }
        }
    }

    /**
     * Error detection CRC method
     * CRC - CCITT using whole byte shifting into a two-byte frame
     * P(X) = X16 + X12 + X5 + l
     * @param bufData Object section to be from and including SYNC to end of DATA
     * @param sizeData Length from and including SYNC to end of DATA
     * @return Calculated CRC
     */
    private static int crc16(byte[] data) {
        int crc = 0;

        for (int i = 0; i < data.length; i++) {
            int ch = (data[i] & 0xFF) << 8;
            for (int j = 8; j > 0; j--) {
                if (((crc ^ ch) & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                ch <<= 1;
            }
        }
        return crc & 0xffff;
    }

    /**
     * Send command w/o data
     * @param out 
     */
    protected void command(String out) throws JposException {
        command(out, null);
    }

    /**
     * Senf command to device
     * @param out
     * @param data
     * @throws JposException 
     */
    protected void command(String out, byte[] data) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                // Make a command
                ByteArrayOutputStream baos = new ByteArrayOutputStream(out.length() + 5);
                DataOutputStream outputStream = new DataOutputStream(baos);
                outputStream.writeByte(STX);
                int len = out.length();
                if (data != null) {
                    len += data.length;
                }
                outputStream.writeShort(len);
                outputStream.writeBytes(out);
                if (data != null) {
                    outputStream.write(data, 0, data.length);
                }
                int crc = crc16(baos.toByteArray());
                outputStream.writeShort(crc);
                byte[] command = baos.toByteArray();
                int repeatCount = 0;
                int result = NAK;
                while (repeatCount < MAX_RETRY && result != ACK) {
                    // Clear input stream
                    port.enableReceiveTimeout(INTER_BYTE);
                    try {
                        while (is.read() > -1) {
                        }
                    } finally {
                        port.disableReceiveTimeout();
                    }
                    // Transmit data to output stream
                    os.write(command);
                    os.flush();
                    // (1) Waiting ACK after command
                    port.enableReceiveTimeout(ACK_TIMEOUT);
                    try {
                        while (result >= 0) {
                            result = is.read();
                            // Ignore if result not ACK nor NAK
                            if (result == ACK || result == NAK) {
                                break;
                            }
                        }
                    } finally {
                        port.disableReceiveTimeout();
                    }
                    // Resend command and Goto (1)
                    if (result != ACK) {
                        Thread.sleep(RETRY_INTERVAL);
                        repeatCount++;
                    }

                }
                // If ACK coto (2) Waiting for response
                if (result != ACK) {
                    throw new JposException(JposConst.JPOS_E_TIMEOUT, "Response timeout or error");
                }
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported com operation error", e);
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Response timeout or error", e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "I/O operation error", e);
            }
        }
    }

    protected byte[] response() throws JposException {
        return response(WAIT_RESPONSE);
    }

    /**
     * Read response from device
     * @return
     * @throws JposException 
     */
    protected byte[] response(int timeout) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                byte[] response = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(baos);
                int repeatCount = 0;
                int result = NAK;
                while (repeatCount < MAX_RETRY && result != ACK) {
                    baos.reset();
                    // (2) Waiting for response after ACK
                    port.enableReceiveTimeout(timeout);
                    try {
                        while (result >= 0) {
                            result = is.read();
                            // Ignore if result not STX
                            if (result == STX) {
                                break;
                            }
                        }
                    } finally {
                        port.disableReceiveTimeout();
                    }
                    // If STX goto (3) Waitinf for LEN
                    if (result == STX) {
                        outputStream.writeByte(STX);
                        port.enableReceiveTimeout(NON_RESPONSE);
                        try {
                            // (3) Waiting for LEN
                            DataInputStream dis = new DataInputStream(is);
                            int len = dis.readShort();
                            outputStream.writeShort(len);
                            // (4) Waiting for text
                            response = new byte[len];
                            if (dis.read(response, 0, len) != len) {
                                throw new EOFException();
                            }
                            outputStream.write(response);
                            // (5) Wait for CRC
                            int crc = dis.readShort() & 0xffff;
                            if (crc != crc16(baos.toByteArray())) {
                                throw new EOFException();
                            }
                            os.write(ACK);
                            os.flush();
                            result = ACK;
                        } catch (EOFException e) {
                            // Time out or wrong crc
                            result = NAK;
                        } finally {
                            port.disableReceiveTimeout();
                        }
                    }
                    // Send NAK and Goto (2)
                    if (result != ACK) {
                        os.write(NAK);
                        os.flush();
                        repeatCount++;
                    }
                }
                // Wait for free time
                Thread.sleep(FREE_TIME);
                // Timeout error
                if (result != ACK) {
                    throw new JposException(JposConst.JPOS_E_TIMEOUT, "Response timeout or error");
                }
                return response;
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported com operation error", e);
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Response timeout or error", e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "I/O operation error", e);
            }
        }
    }

    /**
     * Send command w/o data and wait response with default timeout
     * @param out
     * @return
     * @throws JposException 
     */
    protected byte[] execute(String out) throws JposException {
        return execute(out, null, WAIT_RESPONSE);
    }

    /**
     * Send command with data and wait response with default timeout
     * @param out
     * @return
     * @throws JposException 
     */
    protected byte[] execute(String out, byte[] data) throws JposException {
        return execute(out, data, WAIT_RESPONSE);
    }

    /**
     * Send command w/o data and wait response
     * @param out
     * @return
     * @throws JposException 
     */
    protected byte[] execute(String out, int timeout) throws JposException {
        return execute(out, null, timeout);
    }

    /**
     * Send command with data and wait response
     * @param out
     * @param data
     * @return
     * @throws JposException 
     */
    protected byte[] execute(String out, byte[] data, int timeout) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                while (executed) {
                    monitor.wait();
                }
                byte[] response;
                executed = true;
                try {
                    // Send command
                    command(out, data);
                    try {
                        // Get result
                        response = response(timeout);
                    } catch (JposException e) {
                        // Cancel command
                        cancel();
                        throw e;
                    }
                } finally {
                    executed = false;
                    monitor.notify();
                }
                updateStatus(response);
                return response;
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Response timeout or error");
            }
        }
    }
    //--------------------------------------------------------------------------
    // Variables
    //
    protected int status = 0;
    protected int sensor = 0;
    private boolean executed = false;
    private boolean statusinupdate = false;
    private Timer pollingTimer = null;
    private SerialPort port = null;
    private InputStream is;
    private OutputStream os;
    private boolean enabled = false;
    private static final int BAUDRATE = 38400;
    private static final int SIZE_BUFFER = 1024;
    private static final int NON_RESPONSE = 250;
    private static final int WAIT_RESPONSE = 20000;
    private static final int ACK_TIMEOUT = 300;
    private static final int INTER_BYTE = 50;
    private static final int RETRY_INTERVAL = 20;
    private static final int POLL_INTERVAL = 1000;
    private static final int FREE_TIME = 10;
    private static final int MAX_RETRY = 3;
    private static final int STX = 0xF2;
    private static final int ACK = 0x06;
    private static final int NAK = 0x15;
    private static final int DLE = 0x10;
    private static final int EOT = 0x04;
    private final ArrayList<CardReaderListener> listeners = new ArrayList();
}
