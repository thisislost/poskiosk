/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.applet;

import netscape.javascript.JSObject;

/**
 *
 * @author ryabochkin_mr
 */
public class StatusUpdateListener implements jpos.events.StatusUpdateListener {

    private StatusUpdateListener(JSObject event) {
        this.event = event;
    }

    public static StatusUpdateListener get(JSObject event) {
        StatusUpdateListener sl = (StatusUpdateListener) event.getMember("_statusUpdateListener");
        if (sl == null) {
            sl = new StatusUpdateListener(event);
            event.setMember("_statusUpdateListener", sl);
        }
        return sl;
    }
    
    @Override
    public void statusUpdateOccurred(jpos.events.StatusUpdateEvent sue) {
        event.call("call", new Object[]{this, sue});
    }
    private JSObject event;
}
