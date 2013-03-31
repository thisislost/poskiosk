/* 
 * Vendors collection and model
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
        url: 'entities/vendors',
        model: Backbone.Model.extend({
            urlRoot: 'entities/vendors'

        }),
        filterGroup: function(group) {
            return this.filter(function(item) {
                return item.get('group') == group;
            });
        }

    }))([{
            id: 'mts',
            title: 'Pay for MTS mobile communications',
            image: 'mts.png',
            group: 'phone',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'beeline',
            title: 'Pay for Beeline mobile communications',
            image: 'beeline.jpg',
            group: 'phone',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'megafon',
            title: 'Pay for Megafon mobile communications',
            image: 'megafon.png',
            group: 'phone',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'moscow',
            title: 'Pay of Moscow utilities',
            image: 'moscow.jpg',
            group: 'utilities',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'stpeterburg',
            title: 'Pay of St.Peresburg utilities',
            image: 'stpeterburg.jpg',
            group: 'utilities',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'nizhnynovgorod',
            title: 'Pay of Nizhny Novgorod utilities',
            image: 'nizhnynovgorod.jpg',
            group: 'utilities',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'novosibirsk',
            title: 'Pay of Novosibirsk utilities',
            image: 'novosibirsk.jpg',
            group: 'utilities',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'kamchatka',
            title: 'Pay of Petropavlovsk-Kamchatsky utilities',
            image: 'kamchatka.jpg',
            group: 'utilities',
            fields: [{
                    id: 'phone',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'vladivostok',
            title: 'Pay of Vladivostok utilities',
            image: 'vladivostok.jpg',
            group: 'utilities',
            fields: [{
                    id: 'utilities',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'stavropol',
            title: 'Pay of Stavropol utilities',
            image: 'stavropol.jpg',
            group: 'utilities',
            fields: [{
                    id: 'utilities',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'kurgan',
            title: 'Pay of Kurgan utilities',
            image: 'kurgan.jpg',
            group: 'utilities',
            fields: [{
                    id: 'utilities',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'starodub',
            title: 'Pay of Starodub utilities',
            image: 'starodub.jpg',
            group: 'utilities',
            fields: [{
                    id: 'utilities',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }, {
            id: 'serpuhov',
            title: 'Pay of Serpuhov utilities',
            image: 'serpuhov.jpg',
            group: 'utilities',
            fields: [{
                    id: 'utilities',
                    title: 'Phone number',
                    type: 'digital',
                    lenght: 10
                }]
        }]);

});