package jpos.applet;

import netscape.javascript.JSObject;

public class OutputCompleteListener implements jpos.events.OutputCompleteListener {

    private OutputCompleteListener(JSObject event) {
        this.event = event;
    }

    public static OutputCompleteListener get(JSObject event) {
        OutputCompleteListener ol = (OutputCompleteListener) event.getMember("_outputCompleteListener");
        if (ol == null) {
            ol = new OutputCompleteListener(event);
            event.setMember("_outputCompleteListener", ol);
        }
        return ol;
    }
    
    @Override
    public void outputCompleteOccurred(jpos.events.OutputCompleteEvent oce) {
        event.call("call", new Object[]{this, oce});
    }
    private JSObject event;
}