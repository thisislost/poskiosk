/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.simulator;

import java.util.HashMap;
import jpos.POSPrinterConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.events.OutputCompleteEvent;
import jpos.events.StatusUpdateEvent;
import jpos.services.EventCallbacks;
import jpos.services.POSPrinterService113;

/**
 *
 * @author ryabochkin_mr
 */
public class POSPrinterService extends DeviceService implements POSPrinterService113 {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        super.open(string, ec);
        greeting.registerResource("output", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String t = params.get("station");
                int station;
                if ("JOURNAL".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_JOURNAL;
                } else if ("SLIP".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_SLIP;
                } else {
                    station = POSPrinterConst.PTR_S_RECEIPT;
                }
                String s = content[stationIndex(station)];
                tracer.println("Output " + t + " is " + s);
                return s;
            }
        });
        greeting.registerResource("cover", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String s = params.get("state");
                String t = params.get("station");
                int state;
                int station;
                if ("JOURNAL".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_JOURNAL;
                    if ("OK".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_JRN_COVER_OK;
                        s = "JOURNAL COVER OK";
                    } else {
                        state = POSPrinterConst.PTR_SUE_JRN_COVER_OPEN;
                        s = "JOURNAL COVER OPEN";
                    }
                } else if ("SLIP".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_SLIP;
                    if ("OK".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_SLP_COVER_OK;
                        s = "SLIP COVER OK";
                    } else {
                        state = POSPrinterConst.PTR_SUE_SLP_COVER_OPEN;
                        s = "SLIP COVER OPEN";
                    }
                } else if ("RECEIPT".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_RECEIPT;
                    if ("OK".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_REC_COVER_OK;
                        s = "RECEIPT COVER OK";
                    } else {
                        state = POSPrinterConst.PTR_SUE_REC_COVER_OPEN;
                        s = "RECEIPT COVER OPEN";
                    }
                } else {
                    station = 0;
                    if ("OK".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_COVER_OK;
                        s = "COVER OK";
                    } else {
                        state = POSPrinterConst.PTR_SUE_COVER_OPEN;
                        s = "COVER OPEN";
                    }
                }
                tracer.println("Cover status is " + s);
                setCoverState(station, state);
                return s;
            }
        });
        greeting.registerResource("cartridge", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String s = params.get("state");
                String t = params.get("station");
                int state;
                int station;
                if ("JOURNAL".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_JOURNAL;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_NEAREMPTY;
                        s = "JOURNAL CARTRIDGE NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_EMPTY;
                        s = "JOURNAL CARTRIDGE EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_OK;
                        s = "JOURNAL CARTRIDGE OK";
                    }
                } else if ("SLIP".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_SLIP;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_NEAREMPTY;
                        s = "SLIP CARTRIDGE NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_EMPTY;
                        s = "SLIP CARTRIDGE EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_OK;
                        s = "SLIP CARTRIDGE OK";
                    }
                } else {
                    station = POSPrinterConst.PTR_S_RECEIPT;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_REC_CARTRIDGE_NEAREMPTY;
                        s = "RECEIPT CARTRIDGE NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_REC_CARTRIDGE_EMPTY;
                        s = "RECEIPT CARTRIDGE EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_REC_CARTRIDGE_OK;
                        s = "RECEIPT CARTRIDGE OK";
                    }
                }
                tracer.println("Cartridge status is " + s);
                setCartridgeState(station, state);
                return s;
            }
        });
        greeting.registerResource("paper", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String s = params.get("state");
                String t = params.get("station");
                int state;
                int station;
                if ("JOURNAL".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_JOURNAL;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_JRN_NEAREMPTY;
                        s = "JOURNAL PAPER NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_JRN_EMPTY;
                        s = "JOURNAL PAPER EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_JRN_PAPEROK;
                        s = "JOURNAL PAPER OK";
                    }
                } else if ("SLIP".equalsIgnoreCase(t)) {
                    station = POSPrinterConst.PTR_S_SLIP;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_SLP_NEAREMPTY;
                        s = "SLIP PAPER NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_SLP_EMPTY;
                        s = "SLIP PAPER EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_SLP_PAPEROK;
                        s = "SLIP PAPER OK";
                    }
                } else {
                    station = POSPrinterConst.PTR_S_RECEIPT;
                    if ("NEAREMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_REC_NEAREMPTY;
                        s = "RECEIPT PAPER NEAREMPTY";
                    } else if ("EMPTY".equalsIgnoreCase(s)) {
                        state = POSPrinterConst.PTR_SUE_REC_EMPTY;
                        s = "RECEIPT PAPER EMPTY";
                    } else {
                        state = POSPrinterConst.PTR_SUE_REC_PAPEROK;
                        s = "RECEIPT PAPER OK";
                    }
                }
                tracer.println("Paper status is " + s);
                setPaperState(station, state);
                return s;
            }
        });
    }

    @Override
    public int getCapRecRuledLine() throws JposException {
        return 0;
    }

    @Override
    public int getCapSlpRuledLine() throws JposException {
        return 0;
    }

    @Override
    public void drawRuledLine(int i, String string, int i1, int i2, int i3, int i4) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void printMemoryBitmap(int i, byte[] bytes, int i1, int i2, int i3) throws JposException {
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        return false;
    }

    @Override
    public boolean getCapConcurrentPageMode() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecPageMode() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpPageMode() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        return false;
    }

    @Override
    public String getPageModeArea() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getPageModeDescriptor() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getPageModeHorizontalPosition() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setPageModeHorizontalPosition(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public String getPageModePrintArea() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setPageModePrintArea(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getPageModePrintDirection() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setPageModePrintDirection(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getPageModeStation() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setPageModeStation(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getPageModeVerticalPosition() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setPageModeVerticalPosition(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void clearPrintArea() throws JposException {
        content = new String[]{"", "", ""};
    }

    @Override
    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void pageModePrint(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void updateFirmware(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        return false;
    }

    @Override
    public void resetStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void retrieveStatistics(String[] strings) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void updateStatistics(String string) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        return true;
    }

    @Override
    public boolean getMapCharacterSet() throws JposException {
        return mapCharset;
    }

    @Override
    public void setMapCharacterSet(boolean bln) throws JposException {
        mapCharset = bln;
    }

    @Override
    public String getRecBitmapRotationList() throws JposException {
        return bitmapRotationList[stationIndex(POSPrinterConst.PTR_S_RECEIPT)];
    }

    @Override
    public String getSlpBitmapRotationList() throws JposException {
        return bitmapRotationList[stationIndex(POSPrinterConst.PTR_S_SLIP)];
    }

    @Override
    public int getCapJrnCartridgeSensor() throws JposException {
        return POSPrinterConst.PTR_CART_CLEANING + POSPrinterConst.PTR_CART_EMPTY
                + POSPrinterConst.PTR_CART_REMOVED + POSPrinterConst.PTR_CART_NEAREND;
    }

    @Override
    public int getCapJrnColor() throws JposException {
        return POSPrinterConst.PTR_COLOR_PRIMARY;
    }

    @Override
    public int getCapRecCartridgeSensor() throws JposException {
        return POSPrinterConst.PTR_CART_CLEANING + POSPrinterConst.PTR_CART_EMPTY
                + POSPrinterConst.PTR_CART_REMOVED + POSPrinterConst.PTR_CART_NEAREND;
    }

    @Override
    public int getCapRecColor() throws JposException {
        return POSPrinterConst.PTR_COLOR_PRIMARY;
    }

    @Override
    public int getCapRecMarkFeed() throws JposException {
        return POSPrinterConst.PTR_MF_TO_CUTTER + POSPrinterConst.PTR_MF_TO_TAKEUP;
    }

    @Override
    public boolean getCapSlpBothSidesPrint() throws JposException {
        return false;
    }

    @Override
    public int getCapSlpCartridgeSensor() throws JposException {
        return POSPrinterConst.PTR_CART_CLEANING + POSPrinterConst.PTR_CART_EMPTY
                + POSPrinterConst.PTR_CART_REMOVED + POSPrinterConst.PTR_CART_NEAREND;
    }

    @Override
    public int getCapSlpColor() throws JposException {
        return POSPrinterConst.PTR_COLOR_PRIMARY;
    }

    @Override
    public int getCartridgeNotify() throws JposException {
        return cartridgeNotify;
    }

    @Override
    public void setCartridgeNotify(int i) throws JposException {
        cartridgeNotify = i;
    }

    @Override
    public int getJrnCartridgeState() throws JposException {
        return cartridgeState[stationIndex(POSPrinterConst.PTR_S_JOURNAL)];
    }

    @Override
    public int getJrnCurrentCartridge() throws JposException {
        return currentCartridge[stationIndex(POSPrinterConst.PTR_S_JOURNAL)];
    }

    @Override
    public void setJrnCurrentCartridge(int i) throws JposException {
        currentCartridge[stationIndex(POSPrinterConst.PTR_S_JOURNAL)] = i;
    }

    @Override
    public int getRecCartridgeState() throws JposException {
        return cartridgeState[stationIndex(POSPrinterConst.PTR_S_RECEIPT)];
    }

    @Override
    public int getRecCurrentCartridge() throws JposException {
        return currentCartridge[stationIndex(POSPrinterConst.PTR_S_RECEIPT)];
    }

    @Override
    public void setRecCurrentCartridge(int i) throws JposException {
        currentCartridge[stationIndex(POSPrinterConst.PTR_S_RECEIPT)] = i;
    }

    @Override
    public int getSlpCartridgeState() throws JposException {
        return cartridgeState[stationIndex(POSPrinterConst.PTR_S_SLIP)];
    }

    @Override
    public int getSlpCurrentCartridge() throws JposException {
        return currentCartridge[stationIndex(POSPrinterConst.PTR_S_SLIP)];
    }

    @Override
    public void setSlpCurrentCartridge(int i) throws JposException {
        currentCartridge[stationIndex(POSPrinterConst.PTR_S_SLIP)] = i;
    }

    @Override
    public int getSlpPrintSide() throws JposException {
        return POSPrinterConst.PTR_PS_SIDE1;
    }

    @Override
    public void changePrintSide(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void markFeed(int i) throws JposException {
    }

    @Override
    public int getCapPowerReporting() throws JposException {
        return JposConst.JPOS_PR_ADVANCED;
    }

    @Override
    public int getPowerNotify() throws JposException {
        return powerNotify;
    }

    @Override
    public void setPowerNotify(int i) throws JposException {
        powerNotify = i;
    }

    @Override
    public int getPowerState() throws JposException {
        return powerState;
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        return POSPrinterConst.PTR_CCS_ALPHA + POSPrinterConst.PTR_CCS_ASCII
                + POSPrinterConst.PTR_CCS_UNICODE;
    }

    @Override
    public boolean getCapConcurrentJrnRec() throws JposException {
        return false;
    }

    @Override
    public boolean getCapConcurrentJrnSlp() throws JposException {
        return false;
    }

    @Override
    public boolean getCapConcurrentRecSlp() throws JposException {
        return false;
    }

    @Override
    public boolean getCapCoverSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrn2Color() throws JposException {
        return false;
    }

    @Override
    public boolean getCapJrnBold() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrnDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapJrnDwide() throws JposException {
        return false;
    }

    @Override
    public boolean getCapJrnDwideDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapJrnEmptySensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrnItalic() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrnNearEndSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrnPresent() throws JposException {
        return true;
    }

    @Override
    public boolean getCapJrnUnderline() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRec2Color() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecBarCode() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecBitmap() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecBold() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecDwide() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecDwideDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecEmptySensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecItalic() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecLeft90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecNearEndSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecPapercut() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecPresent() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecRight90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecRotate180() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecStamp() throws JposException {
        return true;
    }

    @Override
    public boolean getCapRecUnderline() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlp2Color() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpBarCode() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpBitmap() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpBold() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpDwide() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpDwideDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpEmptySensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpFullslip() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpItalic() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpLeft90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpNearEndSensor() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpPresent() throws JposException {
        return true;
    }

    @Override
    public boolean getCapSlpRight90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpRotate180() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSlpUnderline() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        return true;
    }

    @Override
    public boolean getAsyncMode() throws JposException {
        return asyncMode;
    }

    @Override
    public void setAsyncMode(boolean bln) throws JposException {
        asyncMode = bln;
    }

    @Override
    public int getCharacterSet() throws JposException {
        return charset;
    }

    @Override
    public void setCharacterSet(int i) throws JposException {
        charset = i;
    }

    @Override
    public String getCharacterSetList() throws JposException {
        return "101,850,866,997,998,999";
    }

    @Override
    public boolean getCoverOpen() throws JposException {
        return coverOpen;
    }

    @Override
    public int getErrorLevel() throws JposException {
        return lastErrorLevel;
    }

    @Override
    public int getErrorStation() throws JposException {
        return lastErrorStation;
    }

    @Override
    public String getErrorString() throws JposException {
        return lastErrorString;
    }

    @Override
    public boolean getFlagWhenIdle() throws JposException {
        return flagWhenIdle;
    }

    @Override
    public void setFlagWhenIdle(boolean bln) throws JposException {
        flagWhenIdle = bln;
    }

    @Override
    public String getFontTypefaceList() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getJrnEmpty() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_JOURNAL)] == POSPrinterConst.PTR_SUE_JRN_EMPTY;
    }

    @Override
    public boolean getJrnLetterQuality() throws JposException {
        return false;
    }

    @Override
    public void setJrnLetterQuality(boolean bln) throws JposException {
    }

    @Override
    public int getJrnLineChars() throws JposException {
        return 80;
    }

    @Override
    public void setJrnLineChars(int i) throws JposException {
    }

    @Override
    public String getJrnLineCharsList() throws JposException {
        return "80";
    }

    @Override
    public int getJrnLineHeight() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setJrnLineHeight(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getJrnLineSpacing() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setJrnLineSpacing(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getJrnLineWidth() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getJrnNearEnd() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_JOURNAL)] == POSPrinterConst.PTR_SUE_JRN_NEAREMPTY;
    }

    @Override
    public int getMapMode() throws JposException {
        return mapMode;
    }

    @Override
    public void setMapMode(int i) throws JposException {
        mapMode = i;
    }

    @Override
    public int getOutputID() throws JposException {
        return outputID;
    }

    @Override
    public String getRecBarCodeRotationList() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getRecEmpty() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_RECEIPT)] == POSPrinterConst.PTR_SUE_REC_EMPTY;
    }

    @Override
    public boolean getRecLetterQuality() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setRecLetterQuality(boolean bln) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getRecLineChars() throws JposException {
        return 80;
    }

    @Override
    public void setRecLineChars(int i) throws JposException {
    }

    @Override
    public String getRecLineCharsList() throws JposException {
        return "80";
    }

    @Override
    public int getRecLineHeight() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setRecLineHeight(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getRecLineSpacing() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setRecLineSpacing(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getRecLinesToPaperCut() throws JposException {
        return 50;
    }

    @Override
    public int getRecLineWidth() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getRecNearEnd() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_RECEIPT)] == POSPrinterConst.PTR_SUE_REC_NEAREMPTY;
    }

    @Override
    public int getRecSidewaysMaxChars() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getRecSidewaysMaxLines() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getRotateSpecial() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setRotateSpecial(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public String getSlpBarCodeRotationList() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public boolean getSlpEmpty() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_SLIP)] == POSPrinterConst.PTR_SUE_SLP_EMPTY;
    }

    @Override
    public boolean getSlpLetterQuality() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setSlpLetterQuality(boolean bln) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getSlpLineChars() throws JposException {
        return 80;
    }

    @Override
    public void setSlpLineChars(int i) throws JposException {
    }

    @Override
    public String getSlpLineCharsList() throws JposException {
        return "80";
    }

    @Override
    public int getSlpLineHeight() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setSlpLineHeight(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getSlpLinesNearEndToEnd() throws JposException {
        return 50;
    }

    @Override
    public int getSlpLineSpacing() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setSlpLineSpacing(int i) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getSlpLineWidth() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getSlpMaxLines() throws JposException {
        return 50;
    }

    @Override
    public boolean getSlpNearEnd() throws JposException {
        return paperState[stationIndex(POSPrinterConst.PTR_S_SLIP)] == POSPrinterConst.PTR_SUE_SLP_NEAREMPTY;
    }

    @Override
    public int getSlpSidewaysMaxChars() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public int getSlpSidewaysMaxLines() throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void beginInsertion(int i) throws JposException {
        content[stationIndex(POSPrinterConst.PTR_S_SLIP)] = "";
    }

    @Override
    public void beginRemoval(int i) throws JposException {
    }

    @Override
    public void clearOutput() throws JposException {
    }

    @Override
    public void cutPaper(int i) throws JposException {
        content[stationIndex(POSPrinterConst.PTR_S_RECEIPT)] = "";
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endRemoval() throws JposException {
    }

    @Override
    public void printBarCode(int i, String string, int i1, int i2, int i3, int i4, int i5) throws JposException {
        tracer.print(stationName(i) + " printBarcode:\n" + string);
    }

    @Override
    public void printBitmap(int i, String string, int i1, int i2) throws JposException {
        tracer.print(stationName(i) + " printBitmap:\n" + string);
    }

    @Override
    public void printImmediate(int i, String string) throws JposException {
        String[] out;
        if (tranState[stationIndex(i)]) {
            out = tranContent;
        } else {
            out = content;
        }
        tracer.print(stationName(i) + " printImmediate:\n" + string);
        out[stationIndex(i)] = out[stationIndex(i)] + string;
    }

    @Override
    public void printNormal(int i, String string) throws JposException {
        String[] out;
        int j = stationIndex(i);
        if (tranState[j]) {
            out = tranContent;
        } else {
            out = content;
        }
        out[j] = out[j] + string;
        tracer.print(stationName(i) + " printNormal:\n" + string);
        if (asyncMode) {
            outputID++;
            fireEvent(new OutputCompleteEvent(eventCallbacks.getEventSource(), outputID));
        }
    }

    @Override
    public void printTwoNormal(int i, String string, String string1) throws JposException {
        String[] out;
        int j1, j2, i1, i2;
        if (i == POSPrinterConst.PTR_TWO_RECEIPT_JOURNAL) {
            i1 = POSPrinterConst.PTR_S_RECEIPT;
            i2 = POSPrinterConst.PTR_S_JOURNAL;
        } else if (i == POSPrinterConst.PTR_TWO_SLIP_JOURNAL) {
            i1 = POSPrinterConst.PTR_S_SLIP;
            i2 = POSPrinterConst.PTR_S_JOURNAL;
        } else {
            i1 = POSPrinterConst.PTR_S_SLIP;
            i2 = POSPrinterConst.PTR_S_RECEIPT;
        }
        j1 = stationIndex(i1);
        j2 = stationIndex(i2);
        if (tranState[j1]) {
            out = tranContent;
        } else {
            out = content;
        }
        out[j1] = out[j1] + string;
        if (tranState[j2]) {
            out = tranContent;
        } else {
            out = content;
        }
        out[j2] = out[j2] + string1;
        tracer.print(stationName(i1) + " printTwoNormal1:\n" + string + 
                "\n" + stationName(i2) + " printTwoNormal2:\n" + string1);
        if (asyncMode) {
            outputID++;
            fireEvent(new OutputCompleteEvent(this, outputID));
        }
    }

    @Override
    public void rotatePrint(int i, int i1) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL);
    }

    @Override
    public void setBitmap(int i, int i1, String string, int i2, int i3) throws JposException {
    }

    @Override
    public void setLogo(int i, String string) throws JposException {
    }

    @Override
    public void transactionPrint(int i, int i1) throws JposException {
        int j = stationIndex(i);
        if (i1 == POSPrinterConst.PTR_TP_TRANSACTION) {
            tranState[j] = true;
            tranContent[j] = content[j];
        } else {
            tranState[j] = false;
            content[j] = tranContent[j];
        }
    }

    @Override
    public void validateData(int i, String string) throws JposException {
    }

    private int stationIndex(int i) {
        if (i == POSPrinterConst.PTR_S_JOURNAL) {
            return 0;
        } else if (i == POSPrinterConst.PTR_S_SLIP) {
            return 2;
        } else {
            return 1;
        }
    }
    
    private String stationName(int i) {
        if (i == POSPrinterConst.PTR_S_JOURNAL) {
            return "Journal";
        } else if (i == POSPrinterConst.PTR_S_SLIP) {
            return "Slip";
        } else {
            return "Receipt";
        }
    }

    private void setPaperState(int station, int state) {
        int j = stationIndex(station);
        if (paperState[j] != state) {
            paperState[j] = state;
            fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), state));
        }
    }

    private void setCoverState(int station, int state) {
        if (station == 0) {
            boolean open = state == POSPrinterConst.PTR_SUE_COVER_OPEN;
            if (coverOpen != open) {
                coverOpen = open;
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), state));
            }
        } else {
            int j = stationIndex(station);
            if (coverState[j] != state) {
                coverState[j] = state;
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), state));
            }
        }
    }

    private void setCartridgeState(int station, int state) {
        int j = stationIndex(station);
        if (cartridgeState[j] != state) {
            cartridgeState[j] = state;
            if (cartridgeNotify == POSPrinterConst.PTR_CN_ENABLED) {
                fireEvent(new StatusUpdateEvent(eventCallbacks.getEventSource(), state));
            }
        }
    }
    private int[] paperState = {
        POSPrinterConst.PTR_SUE_JRN_PAPEROK,
        POSPrinterConst.PTR_SUE_REC_PAPEROK,
        POSPrinterConst.PTR_SUE_SLP_PAPEROK
    };
    private boolean coverOpen = false;
    private int[] coverState = {
        POSPrinterConst.PTR_SUE_JRN_COVER_OK,
        POSPrinterConst.PTR_SUE_REC_COVER_OK,
        POSPrinterConst.PTR_SUE_SLP_COVER_OK
    };
    private int[] cartridgeState = {
        POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_OK,
        POSPrinterConst.PTR_SUE_REC_CARTRIDGE_OK,
        POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_OK
    };
    private String[] content = {"", "", ""};
    private String[] tranContent = {"", "", ""};
    private boolean[] tranState = {false, false, false};
    private String[] bitmapRotationList = {
        "0", "0", "0"
    };
    private int[] currentCartridge = {
        0, 0, 0
    };
    private boolean mapCharset = false;
    private int cartridgeNotify = POSPrinterConst.PTR_CN_DISABLED;
    private boolean asyncMode = false;
    private int charset = POSPrinterConst.PTR_CCS_UNICODE;
    private int lastErrorLevel = 0;
    private int lastErrorStation = 0;
    private String lastErrorString = "";
    private boolean flagWhenIdle = false;
    private int mapMode = POSPrinterConst.PTR_MM_DOTS;
    private int outputID = 0;
}
