/* 
 * Payment screen
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {string} text
 * @param {Backbone.Collection} vendors
 * @returns {Backbone.View.class}
 *
 * Changes:
 * 
 * 11.04.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([
    'app',
    'jquery',
    'underscore',
    'backbone',
    'text!templates/payment.html',
    'models/vendors'
    ], function(app, $, _, Backbone, text, vendors) {

    return Backbone.View.extend({
        initialize: function(options) {
            this.id = options.id;
            this.model = vendors.get(this.id);
            this.template = _.template(text);
            this.model.on('change', function() {
                this.render();
            }, this);
        },
        render: function() {
            var html = this.template(this.model.toJSON());
            this.$el.html(html);
            return this;
        },
        close: function() {
            vendors.off('change', undefined, this);
            return this;
        }
    });
});

