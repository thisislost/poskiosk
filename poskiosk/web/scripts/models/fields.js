/* 
 * Fields collection and model
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * @param {Application} app
 * @param {JQuery} $
 * @param {Underscore} _
 * @param {Backbone} Backbone
 * @returns {Backbone@pro;Collection}
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
    'backbone'], function(app, $, _, Backbone) {

    return new (Backbone.Collection.extend({
        url: 'entities/fields',
    }))([{
            id: 'phone',
            label: 'Phone number',
            keys: '123|456|789|#0@',
            inputmask: '(999)999-9999',
            title: 'Input phone number (10 digits)'
        }, {
            id: 'contract',
            label: 'Contract number',
            keys: '123|456|789|#0@',
            title: 'Input contract number (1-15 digits)',
            pattern: '[0-9]{1,15}',
            maxlength: 15
        }, {
            id: 'control',
            label: 'Control code',
            keys: 'ABCDEFG|HIJKLMN|OPQRSTU|#VWXYZ@',
            title: 'Input control code',
            pattern: '[A-Fa-f0-9]{4}',
            maxlength: 4
        }]);

});