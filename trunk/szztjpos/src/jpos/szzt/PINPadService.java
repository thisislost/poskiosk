/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.szzt;

import jpos.JposConst;
import jpos.JposException;
import jpos.PINPadConst;
import jpos.events.DataEvent;
import jpos.events.DirectIOEvent;
import jpos.events.ErrorEvent;
import jpos.services.EventCallbacks;
import jpos.services.PINPadService113;

/**
 * Common PINPad service
 * @author Maxim
 */
public abstract class PINPadService extends DeviceService implements
        PINPadService113 {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        track1Data = null;
        track2Data = null;
        track3Data = null;
        track4Data = null;
        terminalID = null;
        merchantID = null;
        minimumPINLength = 4;
        maximumPINLength = 12;
        accountNumber = null;
        transactionType = 0;
        transactionHost = -1;
        transactionEnabled = false;
        entryPINEnabled = false;
        additionalSecurityInformation = null;
        encryptedPIN = null;
        super.open(string, ec);
    }

    /**
     * Set all data properties that were populated as a resault of DataEvent or
     * DataError back to their default value
     * @throws JposException
     */
    public void clearInputProperties() throws JposException {
        additionalSecurityInformation = null;
        encryptedPIN = null;
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
     * If true, then the Service/device supports comparing the version of the
     * firmware in the physical device against that of a firmware file.
     * @return
     * @throws JposException
     */
    public boolean getCapCompareFirmwareVersion() throws JposException {
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
     * If true, the device accumulates and can provide various statistics
     * regarding usage; otherwise no usage statistics are accumulated.
     * @return
     * @throws JposException
     */
    public boolean getCapStatisticsReporting() throws JposException {
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

    /**
     * Holds either the decoded track 4 (JIS-II) data from the previous card
     * swipe or an empty array. An empty array indicates that the track was not
     * physically read. The application must set this property before calling
     * the beginEFTtransaction method.
     * @return
     * @throws JposException
     */
    public byte[] getTrack4Data() throws JposException {
        return track4Data;
    }

    /**
     * Holds either the decoded track 4 (JIS-II) data from the previous card
     * swipe or an empty array. An empty array indicates that the track was not
     * physically read. The application must set this property before calling
     * the beginEFTtransactionmethod.
     * @return
     * @throws JposException
     */
    public void setTrack4Data(byte[] bytes) throws JposException {
        track4Data = bytes;
    }

    /**
     * Initialize the beginning of an EFT Transaction. The device will perform
     * initialization functions (such as computing session keys). No other PIN
     * Pad functions can be performed until this method is called.
     * @param string
     * @param i
     * @throws JposException
     */
    public void beginEFTTransaction(String string, int i) throws JposException {
        if (!string.equalsIgnoreCase("M/S")) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    "Only Master/Session transactions are supported");
        }
        if (transactionEnabled) {
            throw new JposException(JposConst.JPOS_E_BUSY,
                    "EFT transaction already started");
        }
        if (accountNumber == null) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    "Account number should be set before EFT transaction started");
        }
        transactionEnabled = true;
        entryPINEnabled = false;
        transactionHost = i;
    }

    /**
     * Clears all device input that has been buffered.
     */
    public void clearInput() throws JposException {
        eventStore.clear();
    }

    /**
     * Computes a MAC value and appends it to the designated message. Depending
     * on the selected PIN Pad management system, the PIN Pad may also insert
     * other fields into the message. Note that this method cannot be used while
     * PIN Pad input (PIN Entry) is enabled.
     * @param string
     * @param strings
     * @throws JposException
     */
    public void computeMAC(String inMsg, String[] outMsg) throws JposException {
        if (!transactionEnabled) {
            throw new JposException(JposConst.JPOS_E_DISABLED,
                    "EFT Transaction is not started");
        }
        if (entryPINEnabled) {
            throw new JposException(JposConst.JPOS_E_BUSY,
                    "The PIN Pad cannot perform a MAC calculation during PIN Entry");
        }
        int[] mac = computeMAC(transactionHost, inMsg.toCharArray());
        if (mac != null) {
            outMsg[0] = inMsg + dataToHex(mac, 0, 8);
        }
    }

    /**
     * Enable PIN Entry at the PIN Pad device. When this method is called, the
     * PINEntryEnabled property will be changed to true. If the PIN Pad uses
     * pre-defined prompts for PIN Entry, then the Prompt property will be
     * changed to PPAD_MSG_ENTERPIN.
     * @throws JposException
     */
    public void enablePINEntry() throws JposException {
        if (!transactionEnabled) {
            throw new JposException(JposConst.JPOS_E_DISABLED,
                    "EFT Transaction is not started");
        }
        entryPINEnabled = true;
        entryPINCount = 0;
        setPINMode(true);
    }

    /**
     * Ends an EFT Transaction. The Device will perform termination functions
     * (such as computing next transaction keys).
     * @param i
     * @throws JposException
     */
    public void endEFTTransaction(int completionCode) throws JposException {
        if (entryPINEnabled) {
            entryPINEnabled = false;
            setPINMode(false);
            fireEvent(new DataEvent(eventCallbacks.getEventSource(),
                    PINPadConst.PPAD_CANCEL));
        }
        clearInputProperties();
        transactionEnabled = false;
        transactionHost = -1;
    }

    /**
     * Holds the account number to be used for the current EFT transaction. The
     * application must set this property before calling the beginEFTTransaction
     * method.
     * @return
     * @throws JposException
     */
    public String getAccountNumber() throws JposException {
        return accountNumber;
    }

    /**
     * Holds additional security/encryption information when a DataEvent is
     * delivered. This property will be formatted as a HEX-ASCII string.
     * The information content and internal format of this string will vary
     * among PIN Pad Management Systems. For example, if the PIN Pad Management
     * System is DUKPT, then this property will contain the “PIN Pad sequence
     * number”. If the PIN Entry was cancelled, this property will contain the
     * empty string.
     * @return
     * @throws JposException
     */
    public String getAdditionalSecurityInformation() throws JposException {
        return additionalSecurityInformation;
    }

    /**
     * Holds the amount of the current EFT transaction. The application must set
     * this property before calling the beginEFTTransaction method. This
     * property is a monetary value stored using an implied four decimal places.
     * For example, an actual value of 12345 represents 1.2345.
     * @return
     * @throws JposException
     */
    public long getAmount() throws JposException {
        return amount;
    }

    /**
     * Holds a semi-colon separated list of a set of a “language definitions”
     * that are supported by the pre-defined prompts in the PIN Pad. A “language
     * definition” consists of an ISO-639 language code and an ISO-3166 country
     * code. The two codes are comma separated.
     * @return
     * @throws JposException
     */
    public String getAvailableLanguagesList() throws JposException {
        return null;
    }

    /**
     * Holds a comma-separated string representation of the supported values for
     * the Prompt property.
     * @return
     * @throws JposException
     */
    public String getAvailablePromptsList() throws JposException {
        return null;
    }

    /**
     * Defines the operations that the application may perform on the PIN Pad
     * display.
     * @return
     * @throws JposException
     */
    public int getCapDisplay() throws JposException {
        return PINPadConst.PPAD_DISP_NONE;
    }

    /**
     * If true, the application can use the PIN Pad to obtain input. The
     * application will use an associated POS Keyboard Device Control object as
     * the interface to the PIN Pad keyboard. Note that the associated POS
     * Keyboard Control is effectively disabled while PINEntryEnabled is true.
     * If false, the application cannot obtain input directly from the PIN Pad
     * keyboard. This property is initialized by the open method.
     * @return
     * @throws JposException
     */
    public boolean getCapKeyboard() throws JposException {
        return true;
    }

    /**
     * Defines the capabilities that the application has to select the language
     * of pre-defined messages (e.g., English, French, Arabic etc.).
     * @return
     * @throws JposException
     */
    public int getCapLanguage() throws JposException {
        return PINPadConst.PPAD_LANG_NONE;
    }

    /**
     * If true, the PIN Pad supports MAC calculation.
     * @return
     * @throws JposException
     */
    public boolean getCapMACCalculation() throws JposException {
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
     * If true, the PIN Pad has a Tone Indicator. The Tone Indicator may be
     * accessed by use of an associated Tone Indicator Control. If false, there
     * is no Tone Indicator.
     * @return
     * @throws JposException
     */
    public boolean getCapTone() throws JposException {
        return false;
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
     * Holds the value of the Encrypted PIN after a DataEvent. This property
     * will be formatted as a hexadecimalASCII string. Each character is in the
     * ranges ‘0’ through ‘9’ or ‘A’ through ‘F’. Each pair of characters is the
     * hexadecimal representation for a byte.For example, if the first four
     * characters are “12FA”, then the first two bytes of the PIN are 12
     * hexadecimal (18) and FA hexadecimal (250).
     * If the PIN Entry was cancelled, this property will contain the empty string.
     * @return
     * @throws JposException
     */
    public String getEncryptedPIN() throws JposException {
        if (entryPINEnabled) {
            entryPINEnabled = false;
            setPINMode(false);
            encryptedPIN = dataToHex(encryptPINBlock(
                    transactionHost, accountNumber.toCharArray()), 0, 8);
        }
        return encryptedPIN;
    }

    /**
     * Holds the maximum acceptable number of digits in a PIN. This property
     * must be set to a default value by the open method. If the application
     * wishes to change this property, it should be set before the
     * enablePINEntry method is called.
     * @return
     * @throws JposException
     */
    public int getMaximumPINLength() throws JposException {
        return maximumPINLength;
    }

    /**
     * Holds the Merchant ID, as it is known to the EFT Transaction Host
     * @return
     * @throws JposException
     */
    public String getMerchantID() throws JposException {
        return merchantID;
    }

    /**
     * Holds the minimum acceptable number of digits in a PIN. This property
     * will be set to a default value by the open method. If the application
     * wishes to change this property, it should be set before the
     * enablePINEntry method is called.
     * @return
     * @throws JposException
     */
    public int getMinimumPINLength() throws JposException {
        return minimumPINLength;
    }

    /**
     * If true, the PIN entry operation is enabled. It is set when the
     * enablePINEntry method is called. It will be set to false when the user
     * has completed the PIN Entry operation or when the endEFTTransaction
     * method has completed.
     * @return
     * @throws JposException
     */
    public boolean getPINEntryEnabled() throws JposException {
        return entryPINEnabled;
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
     * Holds the identifies a pre-defined message to be displayed on the PIN Pad. 
     * @return
     * @throws JposException
     */
    public int getPrompt() throws JposException {
        return PINPadConst.PPAD_MSG_IDLE;
    }

    /**
     * Holds the “language definition” for the message to be displayed
     * @return
     * @throws JposException
     */
    public String getPromptLanguage() throws JposException {
        return null;
    }

    /**
     * Holds the terminal ID, as it is known to the EFT Transaction Host.
     * @return
     * @throws JposException
     */
    public String getTerminalID() throws JposException {
        return terminalID;
    }

    /**
     * Holds either the decoded track 1 data from the previous card swipe or an
     * empty array.
     * @return
     * @throws JposException
     */
    public byte[] getTrack1Data() throws JposException {
        return track1Data;
    }

    /**
     * Holds either the decoded track 2 data from the previous card swipe or an
     * empty array.
     * @return
     * @throws JposException
     */
    public byte[] getTrack2Data() throws JposException {
        return track2Data;
    }

    /**
     * Holds either the decoded track 3 data from the previous card swipe or an
     * empty array.
     * @return
     * @throws JposException
     */
    public byte[] getTrack3Data() throws JposException {
        return track3Data;
    }

    /**
     * Holds the type of the current EFT Transaction.
     * @return
     * @throws JposException
     */
    public int getTransactionType() throws JposException {
        return transactionType;
    }

    /**
     * Holds the account number to be used for the current EFT transaction. The
     * application must set this property before calling the beginEFTTransaction
     * method.
     * @param string
     * @throws JposException
     */
    public void setAccountNumber(String string) throws JposException {
        if (validateAccountNumber(string)) {
            accountNumber = string;
        }
    }

    /**
     * Holds the amount of the current EFT transaction. The application must set
     * this property before calling the beginEFTTransaction method. This
     * property is a monetary value stored using an implied four decimal places.
     * For example, an actual value of 12345 represents 1.2345.
     * @param l
     * @throws JposException
     */
    public void setAmount(long l) throws JposException {
        amount = l;
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
     * Holds the maximum acceptable number of digits in a PIN. This property
     * must be set to a default value by the open method. If the application
     * wishes to change this property, it should be set before the
     * enablePINEntry method is called.

     * @param i
     * @throws JposException
     */
    public void setMaximumPINLength(int i) throws JposException {
        maximumPINLength = i;
    }

    /**
     * Holds the Merchant ID, as it is known to the EFT Transaction Host
     * @param string
     * @throws JposException
     */
    public void setMerchantID(String string) throws JposException {
        merchantID = string;
    }

    /**
     * Holds the minimum acceptable number of digits in a PIN. This property
     * will be set to a default value by the open method. If the application
     * wishes to change this property, it should be set before the
     * enablePINEntry method is called.
     * @param i
     * @throws JposException
     */
    public void setMinimumPINLength(int i) throws JposException {
        minimumPINLength = i;
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
     * Holds the identifies a pre-defined message to be displayed on the PIN Pad.
     * @param i
     * @throws JposException
     */
    public void setPrompt(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Holds the “language definition” for the message to be displayed
     * @param string
     * @throws JposException
     */
    public void setPromptLanguage(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    /**
     * Holds the terminal ID, as it is known to the EFT Transaction Host.
     * @param string
     * @throws JposException
     */
    public void setTerminalID(String string) throws JposException {
        terminalID = string;
    }

    /**
     * Holds either the decoded track 1 data from the previous card swipe or an
     * @param bytes
     * @throws JposException
     */
    public void setTrack1Data(byte[] bytes) throws JposException {
        track1Data = bytes;
    }

    /**
     * Holds either the decoded track 2 data from the previous card swipe or an
     * @param bytes
     * @throws JposException
     */
    public void setTrack2Data(byte[] bytes) throws JposException {
        track2Data = bytes;
    }

    /**
     * Holds either the decoded track 3 data from the previous card swipe or an
     * @param bytes
     * @throws JposException
     */
    public void setTrack3Data(byte[] bytes) throws JposException {
        track3Data = bytes;
    }

    /**
     * Holds the type of the current EFT Transaction.
     * @param i
     * @throws JposException
     */
    public void setTransactionType(int i) throws JposException {
        transactionType = i;
    }

    /**
     * Provides a new encryption key to the PIN Pad. It is used only for those
     * PIN Pad Management Systems in which new key values are sent to the
     * terminal as a field in standard messages from the EFT Transaction Host.
     * @param i
     * @param string
     * @throws JposException
     */
    public void updateKey(int keyNum, String key) throws JposException {
        updateKey(keyNum, hexToData(key));
    }

    public void verifyMAC(String message) throws JposException {
        if (!transactionEnabled) {
            throw new JposException(JposConst.JPOS_E_DISABLED,
                    "EFT Transaction is not started");
        }
        if (entryPINEnabled) {
            throw new JposException(JposConst.JPOS_E_BUSY,
                    "The PIN Pad cannot perform a MAC verificatin during PIN Entry");
        }
        if (message.length() < 16) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    "Service failed to verify MAC value in message");
        }

        char[] data = new char[message.length() - 16];
        for (int i = 0; i < data.length; i++) {
            data[i] = message.charAt(i);
        }
        int[] mac = hexToData(message.substring(message.length() - 16));
        if (!verifyMAC(transactionHost, data, mac)) {
            throw new JposException(JposConst.JPOS_E_FAILURE,
                    "Service failed to verify MAC value in message");
        }
    }

    @Override
    public void setDeviceEnabled(boolean bln) throws JposException {
        super.setDeviceEnabled(bln);
        if (deviceEnabled) {
            openPINPad();
        } else {
            closePINPad();
        }
    }

    //--------------------------------------------------------------------------
    // Protected methods
    //
    /**
     * Open PINPad for read keyboard to buffer
     * @throws JposException
     */
    protected abstract void openPINPad() throws JposException;

    /**
     * Close PINPad for read keyboard to buffer
     * @throws JposException
     */
    protected abstract void closePINPad() throws JposException;

    /**
     * Read clear text from PINPad
     * @return
     */
    protected abstract char[] readClearText() throws JposException;

    /**
     * Remove last entry key in PIN buffer
     * @return
     */
    protected abstract void removeLastPINChar() throws JposException;

    /**
     * Set input mode
     * @throws JposException
     */
    protected abstract void setPINMode(boolean bln) throws JposException;

    /**
     * Set input mode
     * @param keyNum
     * @return
     * @throws JposException
     */
    protected abstract int[] encryptPINBlock(int keyNum, char[] account) throws JposException;

    /**
     * Verify mac method
     * @param keyNum
     * @param data
     * @return
     */
    protected boolean verifyMAC(int keyNum, char[] data, int[] mac) throws JposException {
        throw new JposException(JposConst.JPOS_E_FAILURE,
                "Service failed to verify MAC value in message");
    }

    /**
     * Compute mac method
     * @param keyNum
     * @param data
     * @return
     * @throws JposException
     */
    protected int[] computeMAC(int keyNum, char[] data) throws JposException {
        return null;
    }

    /**
     * Update key
     * @param keyNum
     * @param key
     * @return
     * @throws JposException
     */
    protected void updateKey(int keyNum, int[] key) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                "PIN Pad Management System does support update key");
    }

    /**
     * Poll
     * @throws JposException
     */
    protected void poll() throws JposException {
        if (!deviceEnabled) {
            return;
        }
        try {
            char[] clearText = readClearText();
            if (clearText != null ? clearText.length > 0 : false) {
                for (int i = 0; i < clearText.length; i++) {
                    char ch = clearText[i];
                    if (ch == 0x00) {
                        ch = '*';
                    }
                    // Char input event
                    byte abyte[] = new byte[1];
                    abyte[0] = (byte) ch;
                    fireEvent(new DirectIOEvent(eventCallbacks.getEventSource(),
                            0, (byte) ch, abyte));
                    // Check PIN Entry mode
                    if (entryPINEnabled) {
                        if (ch == '*') {
                            entryPINCount++;
                        }
                        // Back space
                        if ((ch == 0x08) && (entryPINCount > 0)) {
                            removeLastPINChar();
                            entryPINCount--;
                        }
                        // Enter complited
                        if (((ch == 0x0D) && (entryPINCount >= minimumPINLength))
                                || (entryPINCount >= maximumPINLength)) {
                            setPINMode(false);
                            encryptedPIN = dataToHex(encryptPINBlock(
                                    transactionHost, accountNumber.toCharArray()), 0, 8);
                            entryPINEnabled = false;
                            fireEvent(new DataEvent(eventCallbacks.getEventSource(),
                                    PINPadConst.PPAD_SUCCESS));
                        }
                        // Enter canceled
                        if (ch == 0x1B) {
                            setPINMode(false);
                            encryptedPIN = null;
                            entryPINEnabled = false;
                            fireEvent(new DataEvent(eventCallbacks.getEventSource(),
                                    PINPadConst.PPAD_CANCEL));
                        }
                    }
                }
            }
        } catch (JposException jpe) {
            fireEvent(new ErrorEvent(eventCallbacks.getEventSource(),
                    jpe.getErrorCode(),
                    jpe.getErrorCodeExtended(),
                    JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));
        }
    }

    private static boolean validateAccountNumber(String arg) {
        char[] pan = arg.toCharArray();
        int n = 0;
        for (int i = 1; i < pan.length; i++) {
            char ch = pan[pan.length - 1 - i];
            if (ch > '9' || ch < '0') {
                return false;
            }
            int m = ch - '0';
            if (i % 2 == 1) {
                m = m * 2;
                m = m / 10 + m % 10;
            }
            n = (n + m) % 10;
        }
        n = (n + (pan[pan.length - 1] - '0')) % 10;
        return n == 0;
    }
    //--------------------------------------------------------------------------
    // Local variables
    //
    protected byte[] track1Data = null;
    protected byte[] track2Data = null;
    protected byte[] track3Data = null;
    protected byte[] track4Data = null;
    protected String terminalID = null;
    protected String merchantID = null;
    protected int minimumPINLength = 4;
    protected int workKeyNum = -1; // -1 - master key, 0,1,2... - session keys
    protected int maximumPINLength = 12;
    protected int entryPINCount = 0;
    protected String accountNumber = null;
    protected int transactionType = 0;
    protected int transactionHost = -1;
    protected long amount = 0;
    protected boolean transactionEnabled = false;
    protected boolean entryPINEnabled = false;
    protected String additionalSecurityInformation = null;
    protected String encryptedPIN = null;
}
