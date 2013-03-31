/* 
 * Header view
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {string} text
 * @returns {Backbone.View.class}
 *
 * Changes:
 * 
 * 23.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([
    'app',
    'jquery',
    'underscore',
    'backbone',
    'text!templates/header.html'
], function(app, $, _, Backbone, text) {

    return Backbone.View.extend({
        className: 'header',
        initialize: function() {
            this.template = _.template(text);
        },
        render: function() {
            this.$el.html(this.template());
            return this;
        }
    });

});