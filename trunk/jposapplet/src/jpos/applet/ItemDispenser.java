package jpos.applet;

import netscape.javascript.JSObject;

public class ItemDispenser extends jpos.ItemDispenser {

    public void readItemCount(JSObject itemCount, int slotNumber) throws jpos.JposException {
        int[] ints = new int[1];
        super.readItemCount(ints, slotNumber);
        itemCount.setSlot(0, ints[0]);
    }

    public void dispenseItem(JSObject numItem, int slotNumber) throws jpos.JposException {
        int[] ints = new int[1];
        ints[0] = Integer.parseInt(numItem.toString());
        super.readItemCount(ints, slotNumber);
        numItem.setSlot(0, ints[0]);
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
