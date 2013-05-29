/* 
 * Universal eyboard view
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
    'backbone'
], function(app, $, _, Backbone) {

    return Backbone.View.extend({
        className: 'keyboard',
        events: {
            'click  a': 'click'
        },
        initialize: function(options) {
            options = options || {};
            this.keys = options.keys || ['123', '456', '789', '-0<'];
            if (options.input)
                this.setInput(options.input);
        },
        render: function() {
            var html = '<div class="table">';
            _.each(this.keys, function(row) {
                html = html + '<div class="tr">';
                var i, l;
                for (i = 0, l = row.length; i < l; i++) {
                    html = html + '<div class="td">&nbsp;' +
                            '<a class="btn digit-btn">' + row.charAt(i) + '</a>' +
                            '</div>';
                }
                html = html + '</div>';
            });
            html = html + '</div>';
            this.$el.html(html);
            return this;
        },
        setInput: function(input) {
            this.input = (input instanceof $) ? input : $(input);
        },
        click: function(event) {
            var key = $(event.target).text(), value = this.input.val();
            if (key == '<') {
                if (value.length > 0) {
                    this.input.val(value.substring(0, value.length - 1));
                }
            } else {
                this.input.val(value + key);
            }
        }
    });

});

