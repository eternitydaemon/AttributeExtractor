/**
 * @ngdoc function
 * @name testAppYeomanApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the testAppYeomanApp
 */
 angular.module('bulkAttribValExtractor')
 .controller('UserRegCtrl', function($scope) {
 	
          $scope.newUser=[];
          $scope.registerUser=function()
          {
            console.log($scope.newUser);
          }
      
   });