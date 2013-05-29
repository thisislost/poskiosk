/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.ccp;

import jpos.JposException;

/**
 *
 * @author Maxim
 */
public interface PortDriver {

    public void open(String portName) throws JposException;

    public void close() throws JposException;

    public String execute(String command, int replyLen) throws JposException;
    
}
