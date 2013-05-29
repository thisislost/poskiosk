/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.sankyo;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSRConst;
import jpos.events.DataEvent;
import jpos.events.ErrorEvent;
import jpos.services.EventCallbacks;
import jpos.services.MSRService113;

/**
 *
 * @author 99088
 */
public class MSRService extends DeviceService implements MSRService113 {

    @Override
    public void setDeviceEnabled(boolean arg0) throws JposException {
        super.setDeviceEnabled(arg0);
        if (arg0) {
            // Enable input card
            reader.enable();
        } else {
            // Disable input card
            reader.disable();
            // Return card if it in the reader
            if (reader.getStatus() == 0x02) {
                reader.eject();
            }
        }
    }

    public void authenticateDevice(byte[] arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void deauthenticateDevice(byte[] arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public byte[] getAdditionalSecurityInformation() throws JposException {
        return null;
    }

    public String getCapCardAuthentication() throws JposException {
        return "";
    }

    public int getCapDataEncryption() throws JposException {
        return MSRConst.MSR_DE_NONE;

    }

    public int getCapDeviceAuthentication() throws JposException {
        return MSRConst.MSR_DA_NOT_SUPPORTED;
    }

    public boolean getCapTrackDataMasking() throws JposException {
        return false;
    }

    public byte[] getCardAuthenticationData() throws JposException {
        return null;
    }

    public int getCardAuthenticationDataLength() throws JposException {
        return 0;
    }

    public String getCardPropertyList() throws JposException {
        if (parseDecodeData && reader.getStatus() == 0x02 && (track1 != null ? track1.startsWith("B") : false)) {
            return "AccountNumber,ExpirationDate,FirstName,MiddleInitial,ServiceCode,Surname,Title";
        } else {
            return "";
        }
    }

    public String getCardType() throws JposException {
        if (reader.getStatus() == 0x02 && (track1 != null ? track1.startsWith("B") : false)) {
            return "BANK";
        } else {
            return "";
        }
    }

    public String getCardTypeList() throws JposException {
        return "BANK";
    }

    public int getDataEncryptionAlgorithm() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getDeviceAuthenticated() throws JposException {
        return true;
    }

    public int getDeviceAuthenticationProtocol() throws JposException {
        return MSRConst.MSR_AP_NONE;
    }

    public byte[] getTrack1EncryptedData() throws JposException {
        return null;
    }

    public int getTrack1EncryptedDataLength() throws JposException {
        return 0;
    }

    public byte[] getTrack2EncryptedData() throws JposException {
        return null;
    }

    public int getTrack2EncryptedDataLength() throws JposException {
        return 0;
    }

    public byte[] getTrack3EncryptedData() throws JposException {
        return null;
    }

    public int getTrack3EncryptedDataLength() throws JposException {
        return 0;
    }

    public byte[] getTrack4EncryptedData() throws JposException {
        return null;
    }

    public int getTrack4EncryptedDataLength() throws JposException {
        return 0;
    }

    public String getWriteCardType() throws JposException {
        return "";
    }

    private boolean validateAccountNumber(String arg) {
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

    public void retrieveCardProperty(String arg0, String[] arg1) throws JposException {
        if (!(parseDecodeData && (track1 != null ? track1.startsWith("B") : false) && track2 != null)) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        String s = "";
        if (arg0.equalsIgnoreCase("AccountNumber")) {
            s = track2;
            int p = s.indexOf("=");
            if (p >= 0) {
                s = s.substring(0, p);
                if (!validateAccountNumber(s)) {
                    s = "";
                }
            }
            if (s.isEmpty()) {
                String[] ss = track1.split("^");
                if (ss.length > 0) {
                    s = ss[0].substring(1);
                    if (!validateAccountNumber(s)) {
                        s = "";
                    }
                }
            }
        } else if (arg0.equalsIgnoreCase("ExpirationDate")) {
            s = track2;
            int p = s.indexOf("=");
            if (p >= 0 && p + 5 <= s.length()) {
                s = s.substring(p + 1, p + 5);
            } else {
                s = "";
            }
            if (s.isEmpty()) {
                String[] ss = track1.split("^");
                if (ss.length > 2) {
                    if (ss[2].length() >= 4) {
                        s = ss[2].substring(0, 4);
                    }
                }
            }
        } else if (arg0.equalsIgnoreCase("FirstName")
                || arg0.equalsIgnoreCase("MiddleInitial")
                || arg0.equalsIgnoreCase("Surname")
                || arg0.equalsIgnoreCase("Title")) {
            String[] ss = track1.split("\\x5e");
            if (ss.length > 1) {
                s = ss[1];
                if (ss[0].startsWith("B59")) // Country code for Master Card
                {
                    s = s.substring(3);
                }
                ss = s.split("/");
                if (arg0.equalsIgnoreCase("Surname")) {
                    if (ss.length > 0) {
                        s = ss[0];
                    } else {
                        s = "";
                    }
                } else {
                    if (ss.length > 1) {
                        s = ss[1];
                        ss = s.split(".");
                        if (arg0.equalsIgnoreCase("Title")) {
                            if (ss.length > 1) {
                                s = ss[1];
                            } else {
                                s = "";
                            }
                        } else {
                            ss = s.split(" ");
                            if (arg0.equalsIgnoreCase("FirstName")) {
                                if (ss.length > 0) {
                                    s = ss[0];
                                } else {
                                    s = "";
                                }
                            } else if (arg0.equalsIgnoreCase("MiddleInitial")) {
                                if (ss.length > 1) {
                                    s = ss[1];
                                } else {
                                    s = "";
                                }
                            }
                        }
                    } else {
                        s = "";
                    }
                }
            } else {
                s = "";
            }
        } else if (arg0.equalsIgnoreCase("ServiceCode")) {
            s = track2;
            int p = s.indexOf("=");
            if (p >= 0 && p + 8 <= s.length()) {
                s = s.substring(p + 5, p + 8);
            } else {
                s = "";
            }
            if (s.isEmpty()) {
                String[] ss = track1.split("^");
                if (ss.length > 2) {
                    if (ss[2].length() >= 7) {
                        s = ss[2].substring(4, 7);
                    }
                }
            }
        } else {
            throw new JposException(JposConst.JPOS_E_ILLEGAL,
                    getErrorDescription(JposConst.JPOS_E_ILLEGAL));
        }
        arg1[0] = s;
    }

    public void retrieveDeviceAuthenticationData(byte[] arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setDataEncryptionAlgorithm(int arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setWriteCardType(String arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void updateKey(String arg0, String arg1) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void clearInputProperties() throws JposException {
        track1 = null;
        track2 = null;
        track3 = null;
        track4 = null;
    }

    public int getCapWritableTracks() throws JposException {
        return MSRConst.MSR_TR_NONE;
    }

    public int getEncodingMaxLength() throws JposException {
        return 0;
    }

    public int getTracksToWrite() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setTracksToWrite(int arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void writeTracks(byte[][] arg0, int arg1) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void compareFirmwareVersion(String arg0, int[] arg1) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    public boolean getCapUpdateFirmware() throws JposException {
        return false;
    }

    public void updateFirmware(String arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    public void resetStatistics(String arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void retrieveStatistics(String[] arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void updateStatistics(String arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public boolean getCapTransmitSentinels() throws JposException {
        return false;
    }

    public byte[] getTrack4Data() throws JposException {
        return track4 != null ? track4.getBytes() : null;
    }

    public boolean getTransmitSentinels() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void setTransmitSentinels(boolean arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_NONE;
    }

    public int getPowerNotify() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public int getPowerState() throws JposException {
        return JposConst.JPOS_PS_UNKNOWN;
    }

    public void setPowerNotify(int arg0) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL,
                getErrorDescription(JposConst.JPOS_E_ILLEGAL));
    }

    public void clearInput() throws JposException {
        eventStore.clear();
    }

    public String getAccountNumber() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("AccountNumber", arg);
        return arg[0];
    }

    public boolean getAutoDisable() throws JposException {
        return autoDisable;
    }

    public boolean getCapISO() throws JposException {
        return true;
    }

    public boolean getCapJISOne() throws JposException {
        return false;
    }

    public boolean getCapJISTwo() throws JposException {
        return false;
    }

    public int getDataCount() throws JposException {
        return eventQueue.size() + eventStore.size();
    }

    public boolean getDataEventEnabled() throws JposException {
        return dataEventEnabled;
    }

    public boolean getDecodeData() throws JposException {
        return true;
    }

    public int getErrorReportingType() throws JposException {
        return errorReportingType;
    }

    public String getExpirationDate() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("ExpirationDate", arg);
        return arg[0];
    }

    public String getFirstName() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("FirstName", arg);
        return arg[0];
    }

    public String getMiddleInitial() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("MiddleInitial", arg);
        return arg[0];
    }

    public boolean getParseDecodeData() throws JposException {
        return parseDecodeData;
    }

    public String getServiceCode() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("ServiceCode", arg);
        return arg[0];
    }

    public String getSuffix() throws JposException {
        return null;
    }

    public String getSurname() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("Surname", arg);
        return arg[0];
    }

    public String getTitle() throws JposException {
        String[] arg = new String[1];
        retrieveCardProperty("Title", arg);
        return arg[0];
    }

    public byte[] getTrack1Data() throws JposException {
        return track1 != null ? track1.getBytes() : null;
    }

    public byte[] getTrack1DiscretionaryData() throws JposException {
        return null;
    }

    public byte[] getTrack2Data() throws JposException {
        return track2 != null ? track2.getBytes() : null;
    }

    public byte[] getTrack2DiscretionaryData() throws JposException {
        return null;
    }

    public byte[] getTrack3Data() throws JposException {
        return track3 != null ? track3.getBytes() : null;
    }

    public int getTracksToRead() throws JposException {
        return tracksToRead;
    }

    public void setAutoDisable(boolean arg0) throws JposException {
        autoDisable = arg0;
    }

    public void setDataEventEnabled(boolean arg0) throws JposException {
        synchronized (eventQueue) {
            dataEventEnabled = arg0;
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

    public void setDecodeData(boolean arg0) throws JposException {
    }

    public void setErrorReportingType(int arg0) throws JposException {
        errorReportingType = arg0;
    }

    public void setParseDecodeData(boolean arg0) throws JposException {
        parseDecodeData = arg0;
    }

    public void setTracksToRead(int arg0) throws JposException {
        tracksToRead = arg0;
    }

    @Override
    public void onChangeStatus() {
        super.onChangeStatus();
        switch (reader.status) {
            case 0x02:
                // Read tracks
                int status = 0;
                int trackerror = 0;
                if ((tracksToRead & MSRConst.MSR_TR_1) > 0) {
                    try {
                        track1 = reader.readTrack(1);
                        status = track1.length();
                    } catch (JposException e) {
                        track1 = null;
                        trackerror = JposConst.JPOS_E_FAILURE;
                    }
                }
                if ((tracksToRead & MSRConst.MSR_TR_2) > 0) {
                    try {
                        track2 = reader.readTrack(2);
                        status = status | (track2.length() << 8);
                    } catch (JposException e) {
                        track2 = null;
                        trackerror = trackerror | (JposConst.JPOS_E_FAILURE << 8);
                    }
                }
                if ((tracksToRead & MSRConst.MSR_TR_3) > 0) {
                    try {
                        track3 = reader.readTrack(3);
                        status = status | (track3.length() << 16);
                    } catch (JposException e) {
                        track3 = null;
                        trackerror = trackerror | (JposConst.JPOS_E_FAILURE << 16);
                    }
                }
                if ((tracksToRead & MSRConst.MSR_TR_4) > 0) {
                    try {
                        track4 = reader.readTrack(4);
                        status = status | track4.length() << 24;
                    } catch (JposException e) {
                        track4 = null;
                        trackerror = trackerror | (JposConst.JPOS_E_FAILURE << 24);
                    }
                }
                if (trackerror == 0) {
                    fireEvent(new DataEvent(eventCallbacks.getEventSource(), status));
                } else {
                    // Some error occured
                    if (errorReportingType == MSRConst.MSR_ERT_TRACK) {
                        // Return error tracks
                        fireEvent(new ErrorEvent(eventCallbacks.getEventSource(),
                                JposConst.JPOS_E_EXTENDED, trackerror,
                                JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));
                    } else {
                        track1 = null;
                        track2 = null;
                        track3 = null;
                        track4 = null;
                        fireEvent(new ErrorEvent(eventCallbacks.getEventSource(),
                                JposConst.JPOS_E_FAILURE, 0,
                                JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));
                    }
                }
                break;
            case 0x01:
            case 0x00:
                track1 = null;
                track2 = null;
                track3 = null;
                track4 = null;
                break;
        }
        if (reader.getStatus() >= 0x100) {
            fireEvent(new ErrorEvent(eventCallbacks.getEventSource(),
                    JposConst.JPOS_E_FAILURE, 0,
                    JposConst.JPOS_EL_INPUT, JposConst.JPOS_ER_CLEAR));

        }
    }

    @Override
    public void open(String logicalDeviceName, EventCallbacks ec) throws JposException {
        super.open(logicalDeviceName, ec);
        errorReportingType = MSRConst.MSR_ERT_TRACK;
        parseDecodeData = true;
        tracksToRead = MSRConst.MSR_TR_1_2;
    }
    private int errorReportingType;
    private boolean parseDecodeData;
    private int tracksToRead;
    private String track1 = null;
    private String track2 = null;
    private String track3 = null;
    private String track4 = null;
}
