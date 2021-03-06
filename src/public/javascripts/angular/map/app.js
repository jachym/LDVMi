define([
        'angular',
        './controllers/controllers',
        './controllers/layoutController',
        './directives',
        './filters',
        './services',
        'angular-resource',
        'angular-route',
        'angular-ui',
        'ui-bootstrap',
        'angular-loading-bar'
    ],
    function (ng) {
        'use strict';

        return ng.module('map', [
            'map.services',
            'map.controllers',
            'map.filters',
            'map.directives',
            'ngRoute',
            'ngResource',
            'ui.bootstrap',
            'angular-loading-bar'
        ]);
    });