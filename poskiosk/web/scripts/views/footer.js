/* 
 * Footer view
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
    'text!templates/footer.html'
], function(app, $, _, Backbone, text) {

    return Backbone.View.extend({
        className: 'footer',
        initialize: function() {
            this.template = _.template(text);
        },
        render: function() {
            this.$el.html(this.template());
            this.$back = this.$el.find('#back-btn');
            this.$forward = this.$el.find('#forward-btn');
            return this;
        },
        // Set href for footer buttons
        setButtons: function(back, forward) {
            if (back != undefined) {
                this.$back.show().attr('href', back);
            } else {
                this.$back.hide();
            }
            if (forward != undefined) {
                this.$forward.show().attr('href', forward);
            } else {
                this.$forward.hide();
            }
        }

    });
});