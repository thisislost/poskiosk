package jpos.applet;

import netscape.javascript.JSObject;

public class DataListener implements jpos.events.DataListener {

    private DataListener(JSObject event) {
        this.event = event;
    }

    public static DataListener get(JSObject event) {
        DataListener dl = (DataListener)event.getMember("_dataListener");
        if (dl == null) {
            dl = new DataListener(event);
            event.setMember("_dataListener", dl);
        }
        return dl;
    }
    
    @Override
    public void dataOccurred(jpos.events.DataEvent de) {
        event.call("call", new Object[]{this, de});
    }
    private JSObject event;
}
