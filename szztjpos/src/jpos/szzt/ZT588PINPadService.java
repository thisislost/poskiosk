/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.szzt;

import java.io.IOException;
import javax.comm.UnsupportedCommOperationException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.EventCallbacks;

/**
 *
 * @author Maxim
 */
public class ZT588PINPadService extends PINPadService {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        super.open(string, ec);
        if (jposEntry.hasPropertyWithName("UID"))
            uid = hexToData(jposEntry.getProp("UID").getValueAsString());
        if (jposEntry.hasPropertyWithName("password1"))
            password1 = hexToData(jposEntry.getProp("password1").getValueAsString());
        if (jposEntry.hasPropertyWithName("password2"))
            password2 = hexToData(jposEntry.getProp("password2").getValueAsString());
        if (jposEntry.hasPropertyWithName("password3"))
            password3 = hexToData(jposEntry.getProp("password3").getValueAsString());
    }


    @Override
    protected int[] encryptPINBlock(int keyNum, char[] account) throws JposException {
        int[] result = getHexData(8, getCipherPIN(keyNum, 0x20, account, null), 4);
        return result;
    }

    @Override
    protected char[] readClearText() throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                // Execute command
                while (executed) {
                    monitor.wait();
                }
                executed = true;
                try {
                    // Clear input stream
                    port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                    try {
                        int responseCount = 0;
                        while ((response[responseCount] = is.read()) > -1) {
                            responseCount++;
                        }
                        if (responseCount > 0) {
                            char[] result = new char[responseCount];
                            for (int i = 0; i < responseCount; i++) {
                                result[i] = (char) response[i];
                            }
                            return result;
                        } else {
                            return null;
                        }
                    } finally {
                        port.disableReceiveTimeout();
                    }
                } finally {
                    executed = false;
                    monitor.notify();
                }
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        }
    }

    @Override
    protected void closePINPad() throws JposException {
        // Close the EPP, press key value will be communicate directly by the RS-232
        controlCode(0x30, 0x32);
        // Close beep then press key
        controlCode(0x30, 0x34);
    }

    @Override
    protected void openPINPad() throws JposException {
        // Open the EPP, press key value will be communicate directly by the RS-232
        controlCode(0x30, 0x33);
        // Open beep then press key
        controlCode(0x30, 0x35);
    }

    @Override
    protected void removeLastPINChar() throws JposException {
    }

    @Override
    protected void setPINMode(boolean bln) throws JposException {
        if (bln) {
            // PIN fill code
            controlCode(0x33, 0xFF); 
            // PIN min length 
            controlCode(0x34, (minimumPINLength / 10) * 16 + minimumPINLength % 10);
            // PIN max length
            controlCode(0x35, (maximumPINLength / 10) * 16 + maximumPINLength % 10);
            // Auth EPP Key
            authKey(0x00, null);
            // Start input PIN
            startPINInput();
        }
    }

    @Override
    protected String getStatusDescription(int status) {
        switch (status) {
            case 0xE0:
                return "Low battery, Need to replace the battery ";
            case 0xE1:
                return "Not IMK which can't download the master key ";
            case 0xE2:
                return "Not TMK which can't download the working key";
            case 0xE3:
                return "Wrong Key length";
            case 0xE4:
                return "Key index don't exist";
            case 0xE5:
                return "Unmatchable label or no such key";
            case 0xE6:
                return "Wrong verification odd and even of Key";
            case 0xE7:
                return "Wrong Key check";
            case 0xE8:
                return "Wrong command length";
            case 0xE9:
                return "Illegal data";
            case 0xEA:
                return "Information Code don't exist ";
            case 0xEB:
                return "Wrong Parameter type";
            case 0xEC:
                return "Certifcation failure";
            case 0xED:
                return "Certification locked";
            case 0xEE:
                return "Overtime input on EPP";
            case 0xEF:
                return "Other error";
            default:
                return "Other error";
        }
    }

    @Override
    protected void reset() throws JposException {
        // Soft reset
        controlCode(0x30, 0x30);
    }

    @Override
    protected void updateKey(int keyNum, int[] key) throws JposException {
        if (keyNum == 0x00) {
            // First time initialize
            // Reset erase all keys
            controlCode(0x30, 0x31);
            // Load UID
            clearLoadKey(0x80, uid);
            // Initial password
            auth(0x81, password1);
            auth(0x82, password2);
            auth(0x83, password3);
            // Auth EPP for load key
            authEPP();
            clearLoadKey(0x00, key);
        } else {
            // Auth EPP for load key
            authKey(0x00, null);
            cipherLoadKey(0x00, keyNum, 0x33, key);
        }
    }

    @Override
    protected boolean verifyMAC(int keyNum, char[] data, int[] mac) throws JposException {
        return super.verifyMAC(keyNum, data, mac);
    }

    @Override
    protected int[] computeMAC(int keyNum, char[] data) throws JposException {
        return super.computeMAC(keyNum, data);
    }

    /**
     * Make TDES key
     * @param data
     * @return
     */
    private int[] toTDESKey(int[] key) {
        if (key.length == 16) {
            int[] desKey = new int[24];
            System.arraycopy(key, 0, desKey, 0, 16);
            System.arraycopy(key, 0, desKey, 16, 8);
            return desKey;
        } else
            return key;
    }


    /**
     * Convert int array to byte array
     * @param data
     * @return
     */
    private byte[] toByte(int[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) data[i];
        }
        return result;
    }

    /**
     * Convert byte array to int array
     * @param value
     * @return
     */
    private int[] fromByte(byte[] value) {
        int[] result = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = value[i];
        }
        return result;
    }

    private boolean compareData(int[] data1, int[] data2) {
        if (data1.length != data2.length)
            return false;
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i])
                return false;
        }
        return true;
    }

    /**
     * TDES crypt
     * @param data
     * @param key
     * @return
     */
    private int[] crypt(int mode, int[] data, int[] key) throws JposException {
        SecretKeySpec secretkeyspec;
        Cipher cipher;
        try {
            secretkeyspec = new SecretKeySpec(toByte(toTDESKey(key)), "DESede");
            cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(mode, secretkeyspec);
            return fromByte(cipher.doFinal(toByte(data)));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
    }

    /**
     * Auth key
     * @param keyNum
     */
    protected void authKey(int keyNum, int[] key) throws JposException {
        int[] random = auth(0xA0, NULL_KEY16);
        int[] data1 = auth(keyNum, random);
        if (key != null) {
            int[] data2 = crypt(Cipher.ENCRYPT_MODE, random, key);
            if (!compareData(data1, data2))
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
        }
        int[] crypted = crypt(Cipher.ENCRYPT_MODE, data1, uid);
        int[] data2 = auth(0x90, crypted);
        if (!compareData(data1, data2))
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
    }

    /**
     * Auth EPP device
     */
    protected void authEPP() throws JposException {
        int[] data1 = crypt(Cipher.ENCRYPT_MODE, auth(0xA0, NULL_KEY16), uid);
        int[] data2 = crypt(Cipher.DECRYPT_MODE, auth(0x91, data1), uid);
        if (!compareData(data1, data2))
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    getErrorDescription(JposConst.JPOS_E_FAILURE));
    }

    /**
     * Auth function
     * @param authCode
     * @param data
     * @return
     */
    protected int[] auth(int authCode, int[] input) throws JposException {
        int[] data = new int[2 + 32];
        if (input.length == 16) {
            fillNumber(data, 0, 2, Integer.toHexString(authCode).toUpperCase());
            fillHexData(data, 2, input);
            int[] result = execute(0x3A, data);
            int[] output = new int[16];
            for (int i = 0; i < 16; i++) {
                output[i] = Integer.parseInt(getNumber(result, 2 * i, 2), 16);
            }
            return output;
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
    }

    /**
     * Control code command Send
     * @param location
     * @param value
     */
    protected void controlCode(int location, int value) throws JposException {
        int[] data = new int[3];
        data[0] = location;
        fillNumber(data, 1, 2, Integer.toHexString(value).toUpperCase());
        if (location == 0x30 && (value == 0x30 || value == 0x31)) {
            execute(0x35, data, 5000); // Execute with delay for reset
        } else {
            execute(0x35, data); // Execute
        }
    }

    /**
     * Load cleartext key
     * @param keyNum
     * @param key
     * @throws JposException
     */
    protected void clearLoadKey(int keyNum, int[] key) throws JposException {
        int[] data = null;
        if (key.length == 8) {
            data = new int[4 + 3 * 16];
            data[3] = 0x31; // Key length
        } else if (key.length == 16) {
            data = new int[4 + 3 * 32];
            data[3] = 0x32; // Key length
        } else if (key.length == 24) {
            data = new int[4 + 3 * 48];
            data[3] = 0x33; // Key length
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        fillNumber(data, 0, 2, Integer.toHexString(keyNum).toUpperCase()); // Index for save
        data[2] = 0x31; // Combination code
        fillHexData(data, 4, key);
        for (int i = 4 + 2 * key.length; i < data.length; i++) {
            data[i] = 0x30;
        }
        execute(0x31, data);
    }

    /**
     * Load ciphertext key
     * @param masterKeyNum
     * @param keyNum
     * @param key
     * @throws JposException
     */
    protected void cipherLoadKey(int masterKeyNum, int keyNum, int keyTag, int[] key) throws JposException {
        int[] data = null;
        if (key.length == 8) {
            data = new int[6 + 16];
            data[5] = 0x31; // Key length
        } else if (key.length == 16) {
            data = new int[6 + 32];
            data[5] = 0x32; // Key length
        } else if (key.length == 24) {
            data = new int[6 + 48];
            data[5] = 0x33; // Key length
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        fillNumber(data, 0, 2, Integer.toHexString(masterKeyNum).toUpperCase()); // Index for decryption
        fillNumber(data, 2, 2, Integer.toHexString(keyNum).toUpperCase()); // Index for save
        data[4] = keyTag; // Key Tag code
        fillHexData(data, 6, key);
        execute(0x32, data);
    }

    /**
     * Start input PIN
     */
    private void startPINInput() throws JposException {
        int[] data = new int[5];
        data[0] = 0x30; // No prompt
        data[1] = 0x30; // PIN length depends on control code
        data[2] = 0x30;
        data[3] = 0x32; // Stop PIN entry till length reaches, futhermore enter automatically
        data[4] = 0x31; // Delete one of PIN, send one 0x08, allow expand function key input
        execute(0x36, data);
    }

    private int[] getCipherPIN(int keyNum, int type, char[] acc, char[] acc2) throws JposException {
        int[] data;
        if (type == 0x20) {
            data = new int[3 + acc.length];
            for (int i = 0; i < acc.length; i++) {
                data[3 + i] = acc[i];
            }
        } else if (type == 0x62) {
            data = new int[3 + acc.length + acc2.length];
            for (int i = 0; i < acc.length; i++) {
                data[3 + i] = acc[i];
            }
            for (int i = 0; i < acc2.length; i++) {
                data[3 + acc.length + i] = acc2[i];
            }
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        fillNumber(data, 0, 2, Integer.toHexString(keyNum).toUpperCase());
        data[2] = type;
        return execute(0x37, data);
    }

    /**
     * Compute xor check sum
     * @param data
     * @param start
     * @param len
     * @return
     */
    private int computeCheckSum(int[] data, int start, int len) {
        int result = 0;
        for (int i = start; i < len; i++) {
            result = result ^ data[i];
//            System.out.println("i = " + i + " data = " + data[i] + " result = " + result);
        }
        return result;
    }

    /**
     * Fill number string (hex, octal or bcd) into data
     * @param data
     * @param start
     * @param value
     */
    private void fillNumber(int[] data, int start, int len, String value) {
        int offset = len - value.length();
        for (int i = 0; i < len; i++) {
            if (i < offset) {
                data[start + i] = 0x30;
            } else {
                data[start + i] = value.charAt(i - offset);
            }
        }
    }

    /**
     * Fill hex data array
     * @param data
     * @param start
     * @param len
     * @param in
     */
    private void fillHexData(int[] data, int start, int[] in) {
        for (int i = 0; i < in.length; i++) {
            fillNumber(data, start + i * 2, 2, Integer.toHexString(in[i]).toUpperCase());
        }
    }

    /**
     * Get number string (hex, octal or bcd) from data
     * @param data
     * @param start
     * @param value
     */
    private String getNumber(int[] data, int start, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) data[start + i]);
        }
        return sb.toString();
    }

    /**
     * Get hex data array
     * @param outlen
     * @param data
     * @param start
     * @return
     */
    private int[] getHexData(int outlen, int[] data, int start) {
        int[] out = new int[outlen];
        for (int i = 0; i < outlen; i++) {
            out[i] = Integer.parseInt(getNumber(data, start + i * 2, 2), 16);
//            System.out.println("out" + i + ": " + Integer.toHexString(out[i]).toUpperCase());
        }
        return out;
    }

    /**
     * Execute device command
     * @param cmd command
     * @param data send data
     * @return response
     */
    protected int[] execute(int cmd, int[] data) throws JposException {
        return execute(cmd, data, 0);
    }

    /**
     * Execute device command
     * @param cmd command
     * @param data send data
     * @param freeTime pause after executing
     * @return response
     */
    protected int[] execute(int cmd, int[] data, int freeTime) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                // Create command
                int commandCount = 0;
                // Start of Transmission
                command[commandCount] = STX_CHAR;
                commandCount++;
                // Length of data field
                fillNumber(command, commandCount, 3, Integer.toString(data.length + 1));
                commandCount = commandCount + 3;
                // Command code
                command[commandCount] = cmd;
                commandCount++;
                // Data fieild
                if (data != null) {
                    for (int i = 0; i < data.length; i++) {
                        command[commandCount + i] = data[i];
                    }
                }
                commandCount = commandCount + data.length;
                // End of Transmission
                command[commandCount] = ETX_CHAR;
                commandCount++;

                // CRC
                int crc = computeCheckSum(command, 1, commandCount);
                fillNumber(command, commandCount, 2, Integer.toHexString(crc).toUpperCase());
                commandCount = commandCount + 2;
                int[] result = null;
                boolean done = false;
                // Execute command
                while (executed) {
                    monitor.wait();
                }
                executed = true;
                try {
                    int repeatCount = 0;
                    while ((!done) && (repeatCount < MAX_RETRY)) {
                        repeatCount++;
                        // Clear input stream
                        port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                        try {
                            while (is.read() > -1) {
                            }
                        } finally {
                            port.disableReceiveTimeout();
                        }
                        // Transmit data to output stream
                        for (int i = 0; i < commandCount; i++) {
                            os.write(command[i]);
                        }
                        os.flush();
                        // Receive response
                        long start = System.currentTimeMillis();
                        port.enableReceiveTimeout(INTER_BYTE_TIMEOUT);
                        try {
                            while ((!done)
                                    && ((System.currentTimeMillis() - start) < RESPONSE_TIMEOUT)) {
                                // monitor.wait(INTER_BYTE_TIMEOUT);
                                int responseCount = 0;
                                // Start of Text
                                if ((response[responseCount] = is.read()) != STX_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // Data field length
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                int resultCount = Integer.parseInt(getNumber(response, responseCount - 3, 3)) - 1;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if (resultCount > 0) {
                                    result = new int[resultCount];
                                    int ret = -1;
                                    for (int i = 0; i < result.length; i++) {
                                        if ((ret = is.read()) < 0) {
                                            break;
                                        }
                                        response[responseCount + i] = ret;
                                        result[i] = ret;
                                    }
                                    if (ret < 0) {
                                        continue;
                                    }
                                    responseCount = responseCount + result.length;
                                }
                                // End of Text
                                if ((response[responseCount] = is.read()) != ETX_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // CRC-16
                                crc = computeCheckSum(response, 1, responseCount);
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if (crc == Integer.parseInt(getNumber(response, responseCount - 2, 2), 16)) {
                                    done = true;
                                }
                            }
                        } finally {
                            port.disableReceiveTimeout();
                        }
                    }
                } finally {
                    executed = false;
                    monitor.notify();
                }
                if (!done) {
                    throw new JposException(JposConst.JPOS_E_TIMEOUT,
                            getErrorDescription(JposConst.JPOS_E_TIMEOUT));
                }
                // Check result status
                if ((cmd + 0x30) == response[4]) {
                    status = 0;
                } else {
                    status = response[4];
                    throw new JposException(JposConst.JPOS_E_FAILURE,
                            getStatusDescription(status));
                }
                // Wait for free time
                if (freeTime > 0) {
                    Thread.sleep(freeTime);
                }
                // Return result
                return result;
            } catch (UnsupportedCommOperationException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            } catch (InterruptedException e) {
                throw new JposException(JposConst.JPOS_E_TIMEOUT,
                        getErrorDescription(JposConst.JPOS_E_TIMEOUT), e);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        }
    }
    //--------------------------------------------------------------------------
    // Local variables
    //
    private int command[] = new int[SIZE_BUFFER];
    private int response[] = new int[SIZE_BUFFER];
    private boolean executed = false;
    private int uid[] = NULL_KEY16;
    private int password1[] = PASSWORD1;
    private int password2[] = PASSWORD2;
    private int password3[] = PASSWORD3;

    /**
     * Start of Text
     */
    private static final int STX_CHAR = 0x02;
    /**
     * End of Text
     */
    private static final int ETX_CHAR = 0x03;
    /**
     * Retrive count for executing
     */
    private static final int MAX_RETRY = 3;
    /**
     * Response timeout
     */
    private static final int RESPONSE_TIMEOUT = 10000;
    /**
     * Max interval between two bytes transmition
     */
    private static final int INTER_BYTE_TIMEOUT = 50;
    /**
     * Passwords
     */
    private static final int[] PASSWORD1 = {
        0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31,
        0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31
    };
    private static final int[] PASSWORD2 = {
        0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32,
        0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32, 0x32
    };
    private static final int[] PASSWORD3 = {
        0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33,
        0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33
    };

    /**
     * Null Key value
     */
    private static final int[] NULL_KEY24 = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final int[] NULL_KEY16 = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final int[] NULL_KEY8 = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
}
