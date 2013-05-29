/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpos.sankyo;

/**
 * Card reader listener interface
 * @author Maxim
 */
public interface CardReaderListener {
    
    /**
     * On change status event
     */
    public void onChangeStatus();
    
    /**
     * On change sensor event
     */
    public void onChangeSensor();

}
