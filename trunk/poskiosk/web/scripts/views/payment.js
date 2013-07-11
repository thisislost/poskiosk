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
    'models/vendors',
    'views/keyboard'
], function(app, $, _, Backbone, text, vendors, Keyboard) {

    return Backbone.View.extend({
        initialize: function(options) {
            this.id = options.id;
            this.fieldno = options.fieldno;
            this.vendor = vendors.get(this.id);
            this.fields = this.vendor.getFields();
            this.template = _.template(text);
            this.keyboard = new Keyboard();
            this.vendor.on('change', function() {
                this.render();
            }, this);
        },
        render: function() {
            var field = this.fields[this.fieldno].toJSON(),
                    data = _.extend(this.vendor.toJSON(), {field: field}),
            html = this.template(data);
            this.$el.html(html);
            var input = this.$el.find('.input');
            if (field.inputmask) {
                input.inputmask(field.inputmask);
            }
            this.keyboard.setElement(this.$el.find('.keyboard'))
                    .setInput(input)
                    .setKeys(field.keys)
                    .render();
            return this;
        },
        close: function() {
            delete this.keyboard;
            vendors.off('change', undefined, this);
            return this;
        }
    });
});

