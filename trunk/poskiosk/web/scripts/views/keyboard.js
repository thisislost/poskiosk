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
            this.setKeys(options.keys);
            if (options.input)
                this.setInput(options.input);
        },
        render: function() {
            var html = '<div class="table">';
            _.each(this.keys, function(row) {
                html = html + '<div class="tr">';
                var i, l, ch;
                for (i = 0, l = row.length; i < l; i++) {
                    ch = row.charAt(i);
                    html = html + '<div class="td" style="font-size: 1px">&nbsp;';
                    if (ch == '#') {
                        html = html + '<a class="btn" id="delete-btn"></a>';
                    } else if (ch == '@') {
                        html = html + '<a class="btn" id="ok-btn"></a>';
                    } else {
                        html = html + '<a class="btn digit-btn">' + ch + '</a>';
                    }
                    html = html + '</div>';
                }
                html = html + '</div>';
            });
            html = html + '</div>';
            this.$el.html(html);
            return this;
        },
        setInput: function(input) {
            if (input) {
                this.input = (input instanceof $) ? input : $(input);
            } else {
                this.input = $('input');
            }
            return this;
        },
        setKeys: function(keys) {
            if (_.isString(keys)) {
                this.keys = keys.split('|');
            } else if (_.isArray(keys)) {
                this.keys = keys;
            } else {
                this.keys = ['123', '456', '789', '#0@'];
            }
            return this;
        },
        click: function(event) {
            var key = $(event.target).text(),
                    id = event.target.id,
                    val = this.input.val();
            if (id == 'delete-btn') {
                this.input.val('');
            } else if (id == 'ok-btn') {
                app.router.navigate($('#forward-btn').attr('href'),
                        {trigger: true});
            } else {
                if (val.length < parseInt(this.input.attr('maxlength'))) {
                    this.input.val(this.input.val() + key);
                }
            }
        }
    });

});

