/* 
 * Main controller. 
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {Backbone.View.class} Header
 * @param {Backbone.View.class} Footer
 * @param {Backbone.View.class} Start
 * @param {Backbone.View.class} Group
 * @param {jpos.applet.BillAcceptor} billacceptor
 * @returns {Backbone.Router}
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
    'views/header',
    'views/footer',
    'views/start',
    'views/group',
    'views/payment',
    'devices/billacceptor',
    'devices/posprinter'
    ], function(app, $, _, Backbone, Header, Footer, Start, Group, Payment, billacceptor, posprinter){

        var Router =  Backbone.Router.extend({
        
            // Router states
            routes: {
                '': 'start',
                'start': 'start',
                'group/:id': 'group',
                'group/:id/:pageno': 'group',
                'payment/:id': 'payment'
            },
            
            // Initialize router. Create common views
            initialize: function () {
                app.router = this;
                // Before app start initialize empty content header and footer views
                app.on('beforestart', function () {
                    app.header = new Header();
                    app.footer = new Footer();
                    app.content = new Backbone.View({
                        className: 'content'
                    });
                    var $body = $('body');
                    $body.append(app.header.render().el);
                    $body.append(app.content.el)
                    $body.append(app.footer.render().el);
                    app.header.delegateEvents();
                    app.footer.delegateEvents();
                    
                    // Create set content app function
                    app.setContent = function (View, options) {
                        this.trigger('beforechangecontent', app);
                        options = options || {};
                        if (this.content) {
                            options.el = app.content.el;
                            if (this.content.close) {
                                this.content.close();
                            }
                        }
                        this.content = (new View(options)).render();
                        this.trigger('afterchangecontent', app);
                    }
                }, this);
            },

            // Start page
            start: function() {
                app.setContent(Start);
                app.footer.setButtons();
            },
            
            // Show page with vendors list
            group: function(id, pageno) {
                pageno = parseInt(pageno || 0);
                if (app.content instanceof Group && app.content.group == id) {
                    // If view is vendors list
                    // Simple change pageno and render view
                    app.content.pageno = pageno;
                    app.content.render();
                } else {
                    // Change content for vendors list
                    app.setContent(Group, {
                        group: id, 
                        pageno: pageno
                    });
                }
                // Set next and previos page on back and forward buttons
                app.footer.setButtons((pageno == 0) ? '#start' : '#group/'+id+'/'+(pageno-1),
                    (pageno+1 < app.content.pagecount ? '#group/'+id+'/'+(pageno+1) : undefined));
            },

            // Show payment page
            payment: function(id) {
                var back;
                if (app.content instanceof Group) {
                    back = '#group/'+app.content.group+'/'+app.content.pageno;
                } else {
                    back = '#start';
                }
                // Set payment content
                app.setContent(Payment, {
                   id: id 
                });
                // Set next and previos page on back and forward buttons
                app.footer.setButtons(back, "#accept");
            }

        });
       
        return new Router();
    });



