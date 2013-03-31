package jpos.applet;

import netscape.javascript.JSObject;

public class ErrorListener implements jpos.events.ErrorListener {

    private ErrorListener(JSObject event) {
        this.event = event;
    }

    public static ErrorListener get(JSObject event) {
        ErrorListener el = (ErrorListener) event.getMember("_errorListener");
        if (el == null) {
            el = new ErrorListener(event);
            event.setMember("_errorListener", el);
        }
        return el;
    }
    
    @Override
    public void errorOccurred(jpos.events.ErrorEvent ee) {
        event.call("call", new Object[]{this, ee});
    }

    private JSObject event;
}
