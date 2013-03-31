/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.simulator;

import jpos.JposException;
import jpos.services.POSPrinterService113;

/**
 *
 * @author ryabochkin_mr
 */
public class POSPrinterService extends DeviceService implements POSPrinterService113 {

    @Override
    public int getCapRecRuledLine() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapSlpRuledLine() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawRuledLine(int i, String string, int i1, int i2, int i3, int i4) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printMemoryBitmap(int i, byte[] bytes, int i1, int i2, int i3) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapConcurrentPageMode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecPageMode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpPageMode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPageModeArea() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPageModeDescriptor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPageModeHorizontalPosition() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPageModeHorizontalPosition(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPageModePrintArea() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPageModePrintArea(String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPageModePrintDirection() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPageModePrintDirection(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPageModeStation() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPageModeStation(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPageModeVerticalPosition() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPageModeVerticalPosition(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearPrintArea() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pageModePrint(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateFirmware(String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetStatistics(String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void retrieveStatistics(String[] strings) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateStatistics(String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getMapCharacterSet() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMapCharacterSet(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRecBitmapRotationList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSlpBitmapRotationList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapJrnCartridgeSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapJrnColor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapRecCartridgeSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapRecColor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapRecMarkFeed() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpBothSidesPrint() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapSlpCartridgeSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapSlpColor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCartridgeNotify() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCartridgeNotify(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnCartridgeState() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnCurrentCartridge() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setJrnCurrentCartridge(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecCartridgeState() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecCurrentCartridge() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecCurrentCartridge(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpCartridgeState() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpCurrentCartridge() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlpCurrentCartridge(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpPrintSide() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void changePrintSide(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void markFeed(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapPowerReporting() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPowerNotify() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPowerNotify(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPowerState() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapConcurrentJrnRec() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapConcurrentJrnSlp() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapConcurrentRecSlp() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapCoverSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrn2Color() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnBold() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnDwide() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnDwideDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnEmptySensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnItalic() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnNearEndSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnPresent() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapJrnUnderline() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRec2Color() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecBarCode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecBitmap() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecBold() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecDwide() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecDwideDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecEmptySensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecItalic() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecLeft90() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecNearEndSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecPapercut() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecPresent() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecRight90() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecRotate180() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecStamp() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapRecUnderline() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlp2Color() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpBarCode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpBitmap() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpBold() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpDwide() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpDwideDhigh() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpEmptySensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpFullslip() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpItalic() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpLeft90() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpNearEndSensor() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpPresent() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpRight90() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpRotate180() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapSlpUnderline() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getAsyncMode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAsyncMode(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCharacterSet() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCharacterSet(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCharacterSetList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCoverOpen() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getErrorLevel() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getErrorStation() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getErrorString() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getFlagWhenIdle() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlagWhenIdle(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFontTypefaceList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getJrnEmpty() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getJrnLetterQuality() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setJrnLetterQuality(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnLineChars() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setJrnLineChars(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getJrnLineCharsList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnLineHeight() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setJrnLineHeight(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnLineSpacing() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setJrnLineSpacing(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getJrnLineWidth() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getJrnNearEnd() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMapMode() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMapMode(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getOutputID() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRecBarCodeRotationList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getRecEmpty() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getRecLetterQuality() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecLetterQuality(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecLineChars() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecLineChars(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRecLineCharsList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecLineHeight() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecLineHeight(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecLineSpacing() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecLineSpacing(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecLinesToPaperCut() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecLineWidth() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getRecNearEnd() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecSidewaysMaxChars() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRecSidewaysMaxLines() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRotateSpecial() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRotateSpecial(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSlpBarCodeRotationList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getSlpEmpty() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getSlpLetterQuality() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlpLetterQuality(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpLineChars() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlpLineChars(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSlpLineCharsList() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpLineHeight() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlpLineHeight(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpLinesNearEndToEnd() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpLineSpacing() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlpLineSpacing(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpLineWidth() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpMaxLines() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getSlpNearEnd() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpSidewaysMaxChars() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSlpSidewaysMaxLines() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beginInsertion(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beginRemoval(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearOutput() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cutPaper(int i) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endInsertion() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endRemoval() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printBarCode(int i, String string, int i1, int i2, int i3, int i4, int i5) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printBitmap(int i, String string, int i1, int i2) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printImmediate(int i, String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printNormal(int i, String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printTwoNormal(int i, String string, String string1) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rotatePrint(int i, int i1) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setBitmap(int i, int i1, String string, int i2, int i3) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLogo(int i, String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void transactionPrint(int i, int i1) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void validateData(int i, String string) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
