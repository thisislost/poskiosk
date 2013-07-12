/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.simulator;

import java.util.HashMap;
import jpos.JposException;
import jpos.ScannerConst;
import jpos.events.DataEvent;
import jpos.services.EventCallbacks;
import jpos.services.ScannerService113;

/**
 *
 * @author ryabochkin_mr
 */
public class ScannerService extends DeviceService implements ScannerService113 {

    @Override
    public void open(String string, EventCallbacks ec) throws JposException {
        super.open(string, ec);
        decodeData = false;
        greeting.registerResource("input", new GreetingServer.ServerResource() {
            @Override
            public String getResource(HashMap<String, String> params) {
                String d = params.get("data");
                String t = params.get("type");
                int dt = ScannerConst.SCAN_SDT_UNKNOWN;
                if ("UPCA".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_UPCA;
                } else if ("Code39".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_Code39;
                } else if ("EAN128".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_EAN128;
                } else if ("Codabar".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_Codabar;
                } else if ("DATAMATRIX".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_DATAMATRIX;
                } else if ("EAN13".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_EAN13;
                } else if ("QRCODE".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_QRCODE;
                } else if ("Code128".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_Code128;
                } else if ("Other".equalsIgnoreCase(t)) {
                    dt = ScannerConst.SCAN_SDT_OTHER;
                }
                inputData(d.getBytes(), dt);
                tracer.println("Input data: " + t + " " + d);
                return d;
            }
        });
    }

    @Override
    public boolean getDecodeData() throws JposException {
        return decodeData;
    }

    @Override
    public void setDecodeData(boolean bln) throws JposException {
        decodeData = bln;
    }

    @Override
    public byte[] getScanData() throws JposException {
        return data;
    }

    @Override
    public byte[] getScanDataLabel() throws JposException {
        return data;
    }

    @Override
    public int getScanDataType() throws JposException {
        return dataType;
    }

    private void inputData(byte[] data, int dataType) {
        this.data = data;
        this.dataType = dataType;
        fireEvent(new DataEvent(eventCallbacks.getEventSource(), 0));
    }
    
    private byte[] data;
    private int dataType;
    private boolean decodeData;
}
