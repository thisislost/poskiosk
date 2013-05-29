/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.szzt;

import java.io.IOException;
import javax.comm.UnsupportedCommOperationException;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.EventCallbacks;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.macs.ISO9797Alg3Mac;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 *
 * @author Maxim
 */
public class ZT596PINPadService extends PINPadService {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        super.open(string, ec);
        if (jposEntry.hasPropertyWithName("UID"))
            uid = hexToData(jposEntry.getProp("UID").getValueAsString());
        if (jposEntry.hasPropertyWithName("password1"))
            password1 = hexToData(jposEntry.getProp("password1").getValueAsString());
        if (jposEntry.hasPropertyWithName("password2"))
            password2 = hexToData(jposEntry.getProp("password2").getValueAsString());
    }

    @Override
    protected void closePINPad() throws JposException {
        int[] cmd = {SET_KEYBOARD_SOUND, 0};
        execute(cmd);
    }

    @Override
    protected void openPINPad() throws JposException {
        int[] cmd = {SET_KEYBOARD_SOUND, 1};
        execute(cmd);
    }

    @Override
    protected int[] encryptPINBlock(int keyNum, char[] account) throws JposException {
        auth(1, keyNum, randomData(16), uid);
        int[] cmd = {ENCRYPT_PIN_BLOCK,
            keyNum & 0xFF, keyNum >> 8, // Key ID to encrypt PIN block
            0x00, 0x00, // Variation Index
            0x00, // Single encrypt
            keyNum & 0xFF, keyNum >> 8, // Key ID to encrypt PIN block 2nd time
            0x00, 0x00, // Variation Index for 2nd key
            0x00, // PIN Offset. First byte position to fetch
            entryPINCount, // PIN Length. Number of bytes to copy
            entryPINCount, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // Initial PIN Block Pattern
            0x02, // PIN position
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // XOR PIN Block Pattern
        };
        // Fill account number for ISO-0
        char acc[] = {
            '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int len = account.length > 12 ? 12 : account.length - 1;
        for (int i = 0; i < len; i++) {
            acc[15 - i] = account[account.length - 2 - i];
        }
        for (int i = 0; i < 8; i++) {
            cmd[21 + i] = (acc[2 * i] - 0x30) << 8 | (acc[2 * i + 1] - 0x30);
        }
        int[] ret = execute(cmd);
        int[] result = new int[8];
        for (int i = 0; i < 8; i++) {
            result[i] = ret[1 + i];
        }
        return result;
    }

    @Override
    protected char[] readClearText() throws JposException {
        int[] cmd = {READ_CLEAR_TEXT};
        int[] ret = execute(cmd);
        int len = ret[1];
        char[] result = new char[len];
        for (int i = 0; i < len; i++) {
            result[i] = (char) ret[2 + i];
        }
        return result;
    }

    @Override
    protected void removeLastPINChar() throws JposException {
        int[] cmd = {EDIT_ENTRY_BUFFER};
        execute(cmd);
    }

    @Override
    protected void setPINMode(boolean bln) throws JposException {
        if (bln) {
            int[] cmd = {SET_ENTRY_MODE,
                0x02, // PIN Keyboard entry mode
                0x46, // Secure buffer prefill value
                0x00, // Secure buffer initial position
                0x10, // Secure buffer prefill length
                0x00}; // Prefill Length’s data in buffer cann’t prifll
            execute(cmd);
        } else {
            int[] cmd = {SET_ENTRY_MODE,
                0x00, // Open Keyboard entry mode
                0x30, // Buffer prefill value
                0x00,
                0x00,
                0x00}; // Ignored value
            execute(cmd);
        }
    }

    private void auth(int stage, int keyNum, int[] data, int[] uid) throws JposException {
        int[] cmd = {AUTHENTICATE,
            keyNum & 0xFF, keyNum >> 8, // Key ID for authentication need key
            stage, // Authenticate Stage
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Authentic data
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // MAC data
        };
        System.arraycopy(data, 0, cmd, 4, 16);
        // Compute mac
        localComputeMAC(uid, cmd, 20);
        // Execute command
        int[] ret = execute(cmd);
        // Check returning MAC
        int[] comp = ret.clone();
        localComputeMAC(uid, cmd, 17);
        for (int i = 0; i < 8; i++) {
            if (ret[i + 17] != comp[i + 17]) {
                throw new JposException(JposConst.JPOS_E_FAILURE,
                        getErrorDescription(JposConst.JPOS_E_FAILURE));
            }
        }
    }

    private void deleteKey(int keyNum) throws JposException {
        int[] cmd = {DELETE_KEY, keyNum & 0xFF, keyNum >> 8};
        execute(cmd);
    }

    private void setWatchdog(int time) throws JposException {
        int[] cmd = {0xFE, time};
        execute(cmd);
    }

    private int[] randomData(int len) throws JposException {
        int[] cmd = {GENERATE_RANDOM_DATA, len};
        int[] ret = execute(cmd);
        int[] result = new int[len];
        System.arraycopy(ret, 1, result, 0, len);
        return result;
    }

    private void loadKey(int keyNum, int[] key, int writeMode, int attr) throws JposException {
        if (key.length == 8) {
            int[] cmd = {LOAD_KEY,
                keyNum & 0xFF, keyNum >> 8, // Key ID
                writeMode, // Write Mode
                attr & 0xFF, attr >> 8, // Attributes
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // Key value
            };
            System.arraycopy(key, 0, cmd, 6, 8);
            execute(cmd);
        } else {
            int[] cmd = {LOAD_KEY,
                keyNum & 0xFF, keyNum >> 8, // Key ID
                writeMode, // Write Mode
                attr & 0xFF, attr >> 8, // Attributes
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Key value
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };
            System.arraycopy(key, 0, cmd, 6, 16);
            execute(cmd);
        }
    }

    private void exchangeKey(int masterKeyNum, int keyNum, int[] key, int writeMode, int attr) throws JposException {
        if (key.length == 8) {
            int[] cmd = {EXCHANGE_KEY,
                masterKeyNum & 0xFF, masterKeyNum >> 8, // Key which will be used to decrypt the key
                0x00, 0x00, // Variation Index 1
                keyNum & 0xFF, keyNum >> 8, // Target key store location
                attr & 0xFF, attr >> 8, // Target key attributes
                writeMode, // Write mode
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // Key value
            };
            System.arraycopy(key, 0, cmd, 10, 8);
            execute(cmd);
        } else {
            int[] cmd = {EXCHANGE_KEY,
                masterKeyNum & 0xFF, masterKeyNum >> 8, // Key which will be used to decrypt the key
                0x00, 0x00, // Variation Index 1
                keyNum & 0xFF, keyNum >> 8, // Target key store location
                attr & 0xFF, attr >> 8, // Target key attributes
                writeMode, // Write mode
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Key value
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };
            System.arraycopy(key, 0, cmd, 10, 16);
            execute(cmd);
        }
    }

    @Override
    protected void reset() throws JposException {
        setPINMode(false);
    }

    @Override
    protected void updateKey(int keyNum, int[] key) throws JposException {
        if (keyNum == 0) {
            // First time initialize
            deleteKey(999);
            auth(0, 1, password1, NULL_KEY16);
            auth(0, 2, password2, NULL_KEY16);
            loadKey(888, NULL_KEY16, 0, 0);
            loadKey(888, uid, 1, 0);
            setWatchdog(255);
            auth(3, 0, randomData(16), uid);
            if (key.length == 8) {
                loadKey(0, NULL_KEY8, 0, KEYATTR_EXIST | KEYATTR_MK);
            } else {
                loadKey(0, NULL_KEY16, 0, KEYATTR_EXIST | KEYATTR_MK);
            }
            auth(3, 0, randomData(16), uid);
            loadKey(0, key, 1, KEYATTR_EXIST | KEYATTR_MK);
        } else {
            auth(1, 0, randomData(16), uid);
            exchangeKey(0, keyNum, key, 0, KEYATTR_EXIST | KEYATTR_PV | KEYATTR_PE);
        }
    }

    /**
     * Convert int[] to byte[]
     * @param data
     * @return
     */
    private byte[] getAsBytes(int[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < result.length; i++) {
            if (data[i] > 127) {
                result[i] = (byte) (data[i] - 256);
            } else {
                result[i] = (byte) data[i];
            }
        }
        return result;
    }

    /**
     * Copy byte[] into data[]
     * @param data
     * @param offset
     * @param source
     */
    private void copyBytes(int[] data, byte[] source) {
        for (int i = 0; i < data.length; i++) {
            if (i < source.length) {
                if (source[i] < 0) {
                    data[i] = 256 + source[i];
                } else {
                    data[i] = source[i];
                }
            } else {
                data[i] = 0;
            }
        }
    }

    /**
     * Compute local MAC for sending authorize commands
     * @param key
     * @param data
     * @return
     */
    private void localComputeMAC(int[] key, int[] data, int macpos) {
        ISO9797Alg3Mac mac = new ISO9797Alg3Mac(new DESEngine());
        mac.init(new KeyParameter(getAsBytes(key)));
        byte[] buffer = getAsBytes(data);
        mac.update(buffer, 0, macpos);
        mac.doFinal(buffer, macpos);
        copyBytes(data, buffer);
    }

    /**
     * Reflect 'bitnum' bits starting at lowest bit = startLSB
     * @param crc
     * @param bitnum
     * @param startLSB
     * @return
     */
    private int[] reflect(int[] crc, int bitnum, int startLSB) {
        int i, j, iw, jw, bit;
        for (int k = 0; k + startLSB < bitnum - 1 - k; k++) {
            iw = 7 - ((k + startLSB) >> 3);
            jw = 1 << ((k + startLSB) & 7);
            i = 7 - ((bitnum - 1 - k) >> 3);
            j = 1 << ((bitnum - 1 - k) & 7);
            bit = crc[iw] & jw;
            if ((crc[i] & j) != 0) {
                crc[iw] |= jw;
            } else {
                crc[iw] &= (0xff - jw);
            }
            if (bit != 0) {
                crc[i] |= j;
            } else {
                crc[i] &= (0xff - j);
            }
        }
        return crc;
    }

    /**
     * Reflect one byte
     * @param inbyte
     * @return
     */
    private int reflectByte(int inbyte) {
        int outbyte = 0;
        int i = 0x01;
        for (int j = 0x80; j > 0; j >>= 1) {
            if ((inbyte & i) != 0) {
                outbyte |= j;
            }
            i <<= 1;
        }
        return (outbyte);
    }

    /**
     * Compute any CRC
     * @param data
     * @param datalen
     * @param order
     * @param polynom
     * @param init
     * @param xor
     * @param nonDirect
     * @param reverseData
     * @param reverseResult
     * @return
     */
    private int[] computeCRC(int[] data, int datalen, int order, int[] polynom,
            int[] init, int[] xor, boolean nonDirect, boolean reverseData,
            boolean reverseResult) {
        // generate bit mask
        int counter = order;
        int[] mask = new int[8];
        for (int i = 7; i >= 0; i--) {
            if (counter >= 8) {
                mask[i] = 255;
            } else {
                mask[i] = (1 << counter) - 1;
            }
            counter -= 8;
            if (counter < 0) {
                counter = 0;
            }
        }
        // Initial vector
        int[] crc = new int[9];
        for (int i = 0; i < 8; i++) {
            crc[i] = init[i];
        }
        if (nonDirect) // nondirect -> direct
        {
            crc[8] = 0;
            for (int i = 0; i < order; i++) {
                int bit = crc[7 - ((order - 1) >> 3)] & (1 << ((order - 1) & 7));
                for (int k = 0; k < 8; k++) {
                    crc[k] = ((crc[k] << 1) | (crc[k + 1] >> 7)) & mask[k];
                    if (bit != 0) {
                        crc[k] ^= polynom[k];
                    }
                }
            }
        }
        // main loop, algorithm is fast bit by bit type
        crc[8] = 0;
        for (int i = 0; i < datalen; i++) {
            int c = data[i];
            // perform revin
            if (reverseData) {
                c = reflectByte(c);
            }
            // rotate one data byte including crcmask
            String s = "";
            for (int j = 0; j < 8; j++) {
                int bit = 0;
                if ((crc[7 - ((order - 1) >> 3)] & (1 << ((order - 1) & 7))) != 0) {
                    bit = 1;
                }
                s += bit;
                if ((c & 0x80) != 0) {
                    bit ^= 1;
                }
                c <<= 1;
                for (int k = 0; k < 8; k++) // rotate all (max.8) crc bytes
                {
                    crc[k] = ((crc[k] << 1) | (crc[k + 1] >> 7)) & mask[k];
                    if (bit != 0) {
                        crc[k] ^= polynom[k];
                    }
                }
            }
        }
        // perform revout
        if (reverseResult) {
            crc = reflect(crc, order, 0);
        }
        // perform xor value
        for (int i = 0; i < 8; i++) {
            crc[i] ^= xor[i];
        }
        return crc;
    }

    /**
     * Compute CRC-16
     * @param data
     * @param datalen
     * @return
     */
    private int computeCRC_16(int[] data, int datalen) {
        int[] polynom = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x05};
        int[] init = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        int[] xor = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        int[] crc = computeCRC(data, datalen, 16, polynom,
                init, xor, false, true, true);
        return (crc[6] << 8) | crc[7];
    }

    /**
     * Compute CRC-CCITT
     * @param data
     * @param datalen
     * @return
     */
    private int computeCRC_CCITT(int[] data, int datalen) {
        int[] polynom = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x21};
        int[] init = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF};
        int[] xor = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        int[] crc = computeCRC(data, datalen, 16, polynom,
                init, xor, false, false, false);
        return (crc[6] << 8) | crc[7];
    }

    private long computeCRC_32(int[] data, int datalen) {
        int[] polynom = {0x00, 0x00, 0x00, 0x00, 0x04, 0xC1, 0x1D, 0xB7};
        int[] init = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
        int[] xor = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
        int[] crc = computeCRC(data, datalen, 32, polynom,
                init, xor, false, true, true);
        long result = (crc[4] << 8) | crc[5];
        result = (result << 16) | (crc[6] << 8) | crc[7];
        return result;
    }

    /**
     * Execute device command
     * @param data send data
     * @return response
     */
    protected int[] execute(int[] data) throws JposException {
        Object monitor = port;
        synchronized (monitor) {
            try {
                // Create command
                int commandCount = 0;
                // Start of Transmission
                command[commandCount] = START1_CHAR;
                commandCount++;
                command[commandCount] = START2_CHAR;
                commandCount++;
                // Sequence number
                sequence++;
                if (sequence > 255) {
                    sequence = 0;
                }
                command[commandCount] = sequence;
                commandCount++;
                // Reserved byte
                command[commandCount] = 0;
                commandCount++;
                // Length of data field
                command[commandCount] = data.length & 0x00FF;
                commandCount++;
                command[commandCount] = data.length >> 8;
                commandCount++;
                // Data fieild
                for (int i = 0; i < data.length; i++) {
                    command[commandCount + i] = data[i];
                }
                commandCount = commandCount + data.length;
                // CRC-16
                int crc = computeCRC_16(command, commandCount);
                command[commandCount] = crc & 0xFF;
                commandCount++;
                command[commandCount] = crc >> 8;
                commandCount++;
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
                                // Start of Header
                                if ((response[responseCount] = is.read()) != START1_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // Start of Header
                                if ((response[responseCount] = is.read()) != START2_CHAR) {
                                    continue;
                                }
                                responseCount++;
                                // Sequence
                                if ((response[responseCount] = is.read()) != sequence) {
                                    continue;
                                }
                                responseCount++;
                                // Reserved code
                                if ((response[responseCount] = is.read()) < 0) {
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
                                int resultCount = response[responseCount - 2]
                                        + (response[responseCount - 1] << 8);
                                result = new int[resultCount];
                                int ret = -1;
                                for (int i = 0; i < resultCount; i++) {
                                    if ((ret = is.read()) < 0) {
                                        break;
                                    }
                                    response[responseCount + i] = ret;
                                    result[i] = ret;
                                }
                                if (ret < 0) {
                                    continue;
                                }
                                responseCount = responseCount + resultCount;
                                // CRC-16
                                crc = computeCRC_16(response, responseCount);
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if ((response[responseCount] = is.read()) < 0) {
                                    continue;
                                }
                                responseCount++;
                                if (crc == ((response[responseCount - 1] << 8)
                                        | response[responseCount - 2])) {
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
                status = result[0];
                if (status != STATUS_SUCCESS) {
                    throw new JposException(JposConst.JPOS_E_FAILURE,
                            getStatusDescription(status));
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

    /**
     * Get status description
     * @param status
     * @return
     */
    protected String getStatusDescription(int status) {
        switch (status) {
            case STATUS_SUCCESS:
                return "No error – status successful";
            case STATUS_INVALID_COMMAND_LENGTH:
                return "Command length not comply with definition";
            case STATUS_INVALID_KEYID:
                return "KeyID(s) is outside of valid range";
            case STATUS_INVALID_WRITE_MODE:
                return "Invalid Write Mode Specified";
            case STATUS_KEYID_NOT_EXIST:
                return "Key with specified ID doesn’t exist";
            case STATUS_KEY_LOCKED:
                return "Key with specified ID is locked";
            case STATUS_INVALID_KEY_LENGTH:
                return "The Key to be stored has invalid length (not 8 or 16 bytes)";
            case STATUS_INVALID_KEY_ATTRIBUTES:
                return "Key ID has wrong attribute";
            case STATUS_INVALID_IV_ATTRIBUTES:
                return "IV has wrong attribute";
            case STATUS_INVALID_MODE:
                return "Invalid Entry Mode specified";
            case STATUS_INVALID_OFFSET_LENGTH:
                return "Invalid Offset or Length – out of range";
            case STATUS_INVALID_LENGTH_OR_SUM:
                return "Invalid Length or Offset+Length – out of range";
            case STATUS_INVALID_DATA_SPECIFIED:
                return "Invalid data – values out of range";
            case STATUS_INVALID_PIN_LENGTH:
                return "Invalid PIN Length";
            case STATUS_PIN_VERIFICATION_FAIL:
                return "PIN verification failed";
            case STATUS_PIN_ENCRYPTION_SUSPENDED:
                return "PIN encryption/verification suspended";
            case STATUS_INVALID_USERBLOCK_ADDRESS:
                return "Invalid User Block Address specified";
            case STATUS_INVALID_MODULUS_LENGTH:
                return "Invalid modulus length specified";
            case STATUS_INVALID_EXPONENT_LENGTH:
                return "Invalid exponent length specified";
            case STATUS_INVALID_PKCS_STRUCTURE:
                return "Invalid structure received";
            case STATUS_INVALID_PKCS_PADDING:
                return "Invalid PKCS Padding";
            case STATUS_INVALID_SIGNATURE_LENGTH:
                return "Invalid Signature Length";
            case STATUS_SIGNATURE_VERIFICATION_FAIL:
                return "Signature verification failed";
            case STATUS_INVALID_SERIALNO_SPECIFIED:
                return "Invalid Serial Number specified";
            case STATUS_EPP_NOT_INITIALIZED:
                return "EPP Keypair and/or Serial# signature not loaded";
            case STATUS_EPP_ALREADY_INITIALIZED:
                return "EPP Keypair and/or Serial# signature already loaded";
            case STATUS_AUTHENTICATION_FAILED:
                return "Authenticate procedure failed";
            default:
                if (status > STATUS_AUTHENTICATION_SUSPENDED) {
                    return "Authenticate procedure locked out for "
                            + (status - STATUS_AUTHENTICATION_SUSPENDED) + " hours";
                } else {
                    return "Unknown status";
                }
        }
    }
    //--------------------------------------------------------------------------
    // Local variables
    //
    private int command[] = new int[SIZE_BUFFER];
    private int response[] = new int[SIZE_BUFFER];
    private int sequence = -1;
    private boolean executed = false;
    private int uid[] = NULL_KEY16;
    private int password1[] = PASSWORD1;
    private int password2[] = PASSWORD2;
    //--------------------------------------------------------------------------
    // Constants
    //
    /**
     * Commands
     */
    private static final int AUTHENTICATE = 0x00;
    private static final int STORE_KEY = 0x10;
    private static final int LOAD_KEY = 0x11;
    private static final int EXCHANGE_KEY = 0x12;
    private static final int READ_KEY_ATTRIBUTES = 0x13;
    private static final int DELETE_KEY = 0x14;
    private static final int DUPLICATE_KEY = 0x15;
    private static final int RSA_ENCRYPT = 0x17;
    private static final int ENCRYPT_ECB = 0x18;
    private static final int DECRYPT_ECB = 0x19;
    private static final int ENCRYPT_CBC = 0x1A;
    private static final int DECRYPT_CBC = 0x1B;
    private static final int ENCRYPT_PIN_BLOCK = 0x1C;
    private static final int FIRMWARE_AUTH = 0x1D;
    private static final int LOCAL_IBM_PIN_VERIFICATION = 0x1E;
    private static final int LOCAL_VISA_PIN_VERIFICATION = 0x1F;
    private static final int GET_DEVICE_INFO = 0x20;
    private static final int READ_CLEAR_TEXT = 0x21;
    private static final int SET_ENTRY_MODE = 0x22;
    private static final int WRITE_USER_DATA = 0x23;
    private static final int READ_USER_DATA = 0x24;
    private static final int IMPORT_HOST_PUBLIC_KEY = 0x25;
    private static final int EXPORT_EPP_PUBLIC_KEY = 0x26;
    private static final int EXP_EPP_SERIAL_NUMBER = 0x27;
    private static final int IMPORT_DES_KEY = 0x28;
    private static final int IMPORT_EPP_PUBLIC_KEY = 0x29;
    private static final int IMPORT_EPP_PRIVATE_KEY = 0x2A;
    private static final int IMPORT_SERIAL_NUMBER_SIGNATURE = 0x2B;
    private static final int VERIFY_KEY = 0x30;
    private static final int EDIT_ENTRY_BUFFER = 0x31;
    private static final int GENERATE_RANDOM_DATA = 0x32;
    private static final int SET_KEYBOARD_CODES = 0x33;
    private static final int SET_KEYBOARD_SOUND = 0x34;
    private static final int VIRTUAL_KEY_PRESS = 0xF0;
    private static final int ECHO_REPLY = 0xFF;
    /**
     * Status codes
     */
    private static final int STATUS_SUCCESS = 0x00;
    private static final int STATUS_INVALID_COMMAND_LENGTH = 0x01;
    private static final int STATUS_INVALID_KEYID = 0x02;
    private static final int STATUS_INVALID_WRITE_MODE = 0x03;
    private static final int STATUS_KEYID_NOT_EXIST = 0x04;
    private static final int STATUS_KEY_LOCKED = 0x05;
    private static final int STATUS_INVALID_KEY_LENGTH = 0x06;
    private static final int STATUS_INVALID_KEY_ATTRIBUTES = 0x07;
    private static final int STATUS_INVALID_IV_ATTRIBUTES = 0x08;
    private static final int STATUS_INVALID_MODE = 0x09;
    private static final int STATUS_INVALID_OFFSET_LENGTH = 0x0a;
    private static final int STATUS_INVALID_LENGTH_OR_SUM = 0x0b;
    private static final int STATUS_INVALID_DATA_SPECIFIED = 0x0c;
    private static final int STATUS_INVALID_PIN_LENGTH = 0x0d;
    private static final int STATUS_PIN_VERIFICATION_FAIL = 0x0e;
    private static final int STATUS_PIN_ENCRYPTION_SUSPENDED = 0x0f;
    private static final int STATUS_INVALID_USERBLOCK_ADDRESS = 0x30;
    private static final int STATUS_INVALID_MODULUS_LENGTH = 0x41;
    private static final int STATUS_INVALID_EXPONENT_LENGTH = 0x42;
    private static final int STATUS_INVALID_PKCS_STRUCTURE = 0x43;
    private static final int STATUS_INVALID_PKCS_PADDING = 0x44;
    private static final int STATUS_INVALID_SIGNATURE_LENGTH = 0x45;
    private static final int STATUS_SIGNATURE_VERIFICATION_FAIL = 0x46;
    private static final int STATUS_INVALID_SERIALNO_SPECIFIED = 0x47;
    private static final int STATUS_EPP_NOT_INITIALIZED = 0x50;
    private static final int STATUS_EPP_ALREADY_INITIALIZED = 0x51;
    private static final int STATUS_AUTHENTICATION_FAILED = 0x80;
    private static final int STATUS_AUTHENTICATION_SUSPENDED = 0x80;
    /**
     * Key attributes
     */
    private static final int KEYATTR_EXIST = 0x8000;    // key exists in the key store and can be used to perform encryption operation
    private static final int KEYATTR_IV = 0x4000;       // key is used only as Initialization Vector
    private static final int KEYATTR_MK = 0x2000;       // Master key used for secure key loading (EXCHANGE_KEY command)
    private static final int KEYATTR_PV = 0x0800;       // key can be used for PIN Verification
    private static final int KEYATTR_PE = 0x0400;       // key can be used for PIN Encryption
    private static final int KEYATTR_ECB = 0x200;       // key can be used for data encryption in ECB mode
    private static final int KEYATTR_CBC = 0x100;       // key can be used for data encryption in CBC mode
    private static final int KEYATTR_PARITY = 0x0080;    // Read Only attribute, returned by READ_KEY_ATTRIBUTES command. This attribute shows that key has good parity (odd) or bad (one of key characters has even parity). If key has good parity this attribute is clear, otherwise this attribute is set.
    private static final int KEYATTR_LOCK = 0x0040;      // Read Only attribute, returned by READ_KEY_ATTRIBUTES command.  LOCK  bit is modificand by EPP perform Authentication command. All LOCK bit is set b then power on.
    private static final int KEYATTR_DOUBLE = 0x0020;    // Read Only attribute, returned by READ_KEY_ATTRIBUTES command. This attribute
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
     * Start of Transmission
     */
    private static final int START1_CHAR = 0x55;
    private static final int START2_CHAR = 0xAA;
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
    /**
     * Null Key value
     */
    private static final int[] NULL_KEY16 = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private static final int[] NULL_KEY8 = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
}
