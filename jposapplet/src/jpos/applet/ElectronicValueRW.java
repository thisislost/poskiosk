package jpos.applet;

import netscape.javascript.JSObject;

public class ElectronicValueRW extends jpos.ElectronicValueRW {

    public void activateService(JSObject data, JSObject obj) throws jpos.JposException {
        int[] ints = new int[1];
        Object[] objects = new Object[1];
        super.activateService(ints, objects);
        data.setSlot(0, ints[0]);
        obj.setSlot(0, objects[0]);
    }

    public void updateKey(JSObject data, JSObject obj) throws jpos.JposException {
        int[] ints = new int[1];
        Object[] objects = new Object[1];
        super.updateKey(ints, objects);
        data.setSlot(0, ints[0]);
        obj.setSlot(0, objects[0]);
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

    public void addDataListener(JSObject event) {
        super.addDataListener(DataListener.get(event));
    }

    public void removeDataListener(JSObject event) {
        super.removeDataListener(DataListener.get(event));
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
