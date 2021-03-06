define(['angular', './controllers'], function (ng) {
    'use strict';

    return ng.module('ldvm.controllers')
        .controller('List',
        ['$scope', 'Pipelines', '$routeParams',
            function ($scope, pipelines, $routeParams) {

                $scope.page = $routeParams.page || 1;
                $scope.count = $routeParams.count || 50;
                $scope.filter = {
                    discoveryId: $routeParams.discoveryId,
                    visualizerId: $routeParams.visualizerId
                };
                $scope.total = 0;

                $scope.pages = function () {
                    var num = Math.ceil($scope.total / $scope.count);
                    return Array.apply(null, Array(num)).map(function (_, i) {
                        return i;
                    });
                };

                var showPagination = !($routeParams.discoveryId || $routeParams.visualizerId);

                $scope.pipelines = [];

                var getPipelines = function () {
                    var promise = pipelines.findPaginated($scope.page, $scope.count, $scope.filter);
                    promise.then(function (data) {
                        $scope.total = (showPagination ? data.count : data.data.length);
                        $scope.pipelines = data.data;
                    });
                };

                $scope.$watch('page', getPipelines);

                $scope.time = function (a, b) {
                    return a || b;
                };

            }]);
});