/* 
 * Group list view
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
 * 23.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([
    'app',
    'jquery',
    'underscore',
    'backbone',
    'text!templates/group.html',
    'models/vendors'
], function(app, $, _, Backbone, text, vendors) {

    return Backbone.View.extend({
        pagesize: 6,
        initialize: function(options) {
            this.group = options.group;
            this.models = vendors.filterGroup(this.group);
            this.pageno = options.pageno || 0;
            this.pagecount = Math.ceil(this.models.length / this.pagesize);
            this.template = _.template(text);
            vendors.on('reset change add remove', function() {
                this.models = vendors.filterGroup(this.group);
                this.render();
            }, this);
        },
        render: function() {
            var i, j, item, html = '<div class="table"><div class="tr">';
            for (i = this.pageno * this.pagesize, j = Math.min(this.models.length, (this.pageno + 1) * this.pagesize); i < j; i++) {
                item = this.models[i];
                if (i % 3 == 0 && i > 0) {
                    html = html + '</div><div class="tr">';
                }
                html = html + this.template(item.toJSON());
            }
            html = html + '</div></div>'
            this.$el.html(html);
            return this;
        },
        close: function() {
            vendors.off('reset change add remove', undefined, this);
            return this;
        }
    });
});

