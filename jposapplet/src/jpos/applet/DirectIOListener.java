package jpos.applet;

import netscape.javascript.JSObject;

public class DirectIOListener implements jpos.events.DirectIOListener {

    private DirectIOListener(JSObject event) {
        this.event = event;
    }

    public static DirectIOListener get(JSObject event) {
        DirectIOListener dl = (DirectIOListener)event.getMember("_directIOListener");
        if (dl != null) {
            dl = new DirectIOListener(event);
            event.setMember("_directIOListener", dl);
        }
        return dl;
    }
    
    @Override
    public void directIOOccurred(jpos.events.DirectIOEvent dioe) {
        event.call("call", new Object[]{this, dioe});
    }
    private JSObject event;
}
