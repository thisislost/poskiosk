/* 
 * Sample informer controller. 
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {Backbone.View.class} Info
 * @returns {Backbone.Router}
 *
 * Changes:
 * 
 * 29.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([
    'app',
    'jquery',
    'underscore',
    'backbone',
    'views/info'
    ], function(app, $, _, Backbone, Info){

        var Router = Backbone.Router.extend({
        
            // Router states
            routes: {
                'info': 'info'
            },
            
            // Show info form
            info: function () {
               app.setContent(Info);
            }

        });
        
        return new Router();
    });



