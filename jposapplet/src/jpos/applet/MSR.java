
package jpos.applet;

import netscape.javascript.JSObject;

public class MSR extends jpos.MSR {

    public void retrieveCardProperty(String name, JSObject value) throws jpos.JposException {
        String[] strings = new String[1];
        super.retrieveCardProperty(name, strings);
        value.setSlot(0, strings[0]);
    }

    public void retrieveDeviceAuthenticationData(JSObject challenge) throws jpos.JposException {
        int len = Integer.parseInt(challenge.getMember("length").toString());
        byte[] bytes = new byte[len];
        super.retrieveDeviceAuthenticationData(bytes);
        for (int i = 0; i < len; i++) {
            challenge.setSlot(i, bytes[i]);
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

    public void addErrorListener(JSObject event) {
        super.addErrorListener(ErrorListener.get(event));
    }

    public void removeErrorListener(JSObject event) {
        super.removeErrorListener(ErrorListener.get(event));
    }
   
}
