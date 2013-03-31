/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.simulator;

import jpos.JposException;
import jpos.services.ScannerService113;

/**
 *
 * @author ryabochkin_mr
 */
public class ScannerService extends DeviceService implements ScannerService113 {

    @Override
    public void clearInputProperties() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void compareFirmwareVersion(String string, int[] ints) throws JposException {
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
    public boolean getAutoDisable() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAutoDisable(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getDataCount() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getDataEventEnabled() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDataEventEnabled(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getDecodeData() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDecodeData(boolean bln) throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getScanData() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getScanDataLabel() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getScanDataType() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearInput() throws JposException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
