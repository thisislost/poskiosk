/* 
 * Routers initialization and run application
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Backbone} Backbone
 * @returns {Application}
 *
 * Changes:
 * 
 * 23.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([
    'backbone'
    ], function(Backbone){
        var app = {
            // Run application
            run: function () {
                // Handle exit event
                this.handleExit();
                // Before start applitacation
                this.trigger('beforestart', this);                
                // Start history
                Backbone.history.start();
                // After start applitacation
                this.trigger('afterstart', this);                
            },
            
            // Stop application
            exit: function () {
                // Before start applitacation
                this.trigger('beforestop', this);                
                // Start history
                Backbone.history.stop();
                // After start applitacation
                this.trigger('afterstop', this);                
            },

            // Handle to stop application
            handleExit: function () {
                if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
                    $(window).bind('beforeunload', function () {
                        app.exit();
                    });
                } else {
                    $(window).unload(function () {
                        app.exit();
                    });
                }
            }
            
        }
        _.extend(app, Backbone.Events);
        
        return app;
    });
