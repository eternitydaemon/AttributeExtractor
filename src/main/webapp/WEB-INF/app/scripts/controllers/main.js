'use strict';

/**
 * @ngdoc function
 * @name testAppYeomanApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the testAppYeomanApp
 */
 angular.module('bulkAttribValExtractor')
 .controller('MainCtrl', function($scope, $http) {
 	$scope.testAjax = function () {
          /* the $http service allows you to make arbitrary ajax requests.
           * in this case you might also consider using angular-resource and setting up a
           * User $resource. */


           $http.get('http://localhost:8080/TestJersey/').
           success( function(response) {
           		$scope.dataRec = response; 
           		console.log("hello"+response);
           }).
           error(function(response){
           		console.log("error:"+response);
           });
       }

      $rootScope.logVal = "Login";
   });
