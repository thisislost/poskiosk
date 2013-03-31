package jpos.applet;

import netscape.javascript.JSObject;

public class FiscalPrinter extends jpos.FiscalPrinter {

    public void getData(int dataItem, JSObject optArgs, JSObject data) throws jpos.JposException {
        int[] ints = new int[1];
        String[] strings = new String[1];
        super.getData(dataItem, ints, strings);
        optArgs.setSlot(0, ints[0]);
        data.setSlot(0, strings[0]);
    }

    public void getData(JSObject date) throws jpos.JposException {
        String[] strings = new String[1];
        super.getDate(strings);
        date.setSlot(0, strings[0]);
    }

    public void getTotalizer(int vatID, int optArgs, JSObject data) throws jpos.JposException {
        String[] strings = new String[1];
        super.getTotalizer(vatID, optArgs, strings);
        data.setSlot(0, strings[0]);
    }

    public void getVatEntry(int vatID, int optArgs, JSObject vatRate) throws jpos.JposException {
        int[] ints = new int[1];
        super.getVatEntry(vatID, optArgs, ints);
        vatRate.setSlot(0, ints[0]);
    }

    public void compareFirmwareVersion(String firmwareFileName, JSObject result) throws jpos.JposException {
        int[] ints = new int[1];
        super.compareFirmwareVersion(firmwareFileName, ints);
        result.setSlot(0, ints[0]);
    }

    public void retrieveStatistics(JSObject statisticsBuffer) throws jpos.JposException {
        String[] strings = new String[1];
        super.retrieveStatistics(strings);
        statisticsBuffer.setSlot(0, strings[0]);
    }

    public void addDirectIOListener(JSObject event) {
        super.addDirectIOListener(DirectIOListener.get(event));
    }

    public void removeDirectIOListener(JSObject event) {
        super.removeDirectIOListener(DirectIOListener.get(event));
    }

    public void addStatusUpdateListener(JSObject event) {
        super.addStatusUpdateListener(StatusUpdateListener.get(event));
    }

    public void removeStatusUpdateListener(JSObject event) {
        super.removeStatusUpdateListener(StatusUpdateListener.get(event));
    }

    public void addErrorListener(JSObject event) {
        super.addErrorListener(ErrorListener.get(event));
    }

    public void removeErrorListener(JSObject event) {
        super.removeErrorListener(ErrorListener.get(event));
    }

    public void addOutputCompleteListener(JSObject event) {
        super.addOutputCompleteListener(OutputCompleteListener.get(event));
    }

    public void removeOutputCompleteListener(JSObject event) {
        super.removeOutputCompleteListener(OutputCompleteListener.get(event));
    }
}
