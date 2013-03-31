/* 
 * Sample information view
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {Constants} labels
 * @returns {Backbone.View.class}
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
    'i18n!nls/labels'
    ], function(app, $, _, Backbone, labels){
       
        return Backbone.View.extend({
            
            render: function() {
                this.$el.html(labels.about);
                return this;
            }
        });
    });