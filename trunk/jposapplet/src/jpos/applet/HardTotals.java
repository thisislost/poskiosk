package jpos.applet;

import netscape.javascript.JSObject;

public class HardTotals extends jpos.HardTotals {

    public void create(String fileName, JSObject hTotalsFile, int size, boolean errorDetection) throws jpos.JposException {
        int[] ints = new int[1];
        super.create(fileName, ints, size, errorDetection);
        hTotalsFile.setSlot(0, ints[0]);
    }

    public void find(String fileName, JSObject hTotalsFile, JSObject size) throws jpos.JposException {
        int[] ints = new int[1];
        int[] ints2 = new int[1];
        super.find(fileName, ints, ints2);
        hTotalsFile.setSlot(0, ints[0]);
        size.setSlot(0, ints2[0]);
    }

    public void findByIndex(int index, JSObject fileName) throws jpos.JposException {
        String[] strings = new String[1];
        super.findByIndex(index, strings);
        fileName.setSlot(0, strings[0]);
    }

  public void read(int hTotalsFile, JSObject data, int offset, int count)throws jpos.JposException {
        byte[] bytes = new byte[count];
        super.read(hTotalsFile, bytes, offset, count);
        for (int i = 0; i < count; i++) {
            data.setSlot(i, bytes[i]);
        }
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
}
