/**
 * Bill Dispenser device
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app         
 * @param {jpos.applet.JposAppet} jpos        
 * @param {Constants} jposConst   
 * @returns {jpos.applet.BillDispenser}                      
 * 
 * Changes:
 * 
 * 28.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */
define(['app',
    'applet!applet/jposapplet',
    'devices/jposconst'], function(app, jpos, jposConst) {

    var dev = jpos.createDevice('BillDispenser');
    
    return dev;
});