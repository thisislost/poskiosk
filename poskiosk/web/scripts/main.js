/* 
 * Application start point
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @param {Application} app
 * @param {Backbone.Router} router
 * @param {Backbone.Router} informer
 * @returns {}
 *
 * Changes:
 * 
 * 23.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

// Require.js allows us to configure shortcut alias
// There usage will become more apparent futher along in the tutorial.
require.config({
    paths: {
        // Libraries
        'jquery': 'libs/jquery/jquery-1.9.1.min',
        'jquery.inputmask': 'libs/jquery/jquery.inputmask-2.2.66.min',
        'underscore': 'libs/underscore/underscore-min',
        'backbone': 'libs/backbone/backbone-min',
        'require': 'libs/require/require',
        'i18n': 'libs/require/i18n',
        'text': 'libs/require/text',
        'applet': 'devices/applet',
        'templates': '../templates'
    },
    shim: {
        'jquery': {
            exports: 'jQuery'
        },
        'jquery.inputmask': {
            deps: ['jquery']
        },
        'underscore': {
            exports: '_'
        },
        'backbone': {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        }
    },
    config: {
        //Set the config for the i18n
        i18n: {
            locale: 'en'
        }
    }

});

// Load JQuery plugins
require(['jquery', 'jquery.inputmask']);

// Set DOM Library for backbone
require(['jquery', 'backbone'], function($, Backbone) {
    Backbone.setDomLibrary($);
});

require([
    // Some plugins have to be loaded in order due to there non AMD compliance
    // Because these scripts are not "modules" they do not pass any values to the definition function below
    'jquery',
    'underscore',
    'backbone',
    // Load our app module and pass it to our definition function
    'app',
    // Pass in our Router module and call it's initialize function
    'routers/router',
    'routers/informer'
], function($, _, Backbone, app, router, informer) {
    // Run application when DOM model loaded
    $(function() {
        app.run();
    });
});

    