'use strict';

/**
 * @ngdoc overview
 * @name testAppYeomanApp
 * @description
 * # testAppYeomanApp
 *
 * Main module of the application.
 */
 angular
 .module('bulkAttribValExtractor', [
  'ngAnimate',
  'ngAria',
  'ngCookies',
  'ngMessages',
  'ngResource',
  'ngRoute',
  'ngSanitize',
  'ngTouch',
  'lr.upload',
  'ui.router',
  'ngAnimate',
  'ui.bootstrap'
  
  ])
 .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.otherwise("/login");

  $stateProvider
  .state('home',{
    url:'/login',
    views: {
      '':{
        templateUrl:'views/login.html',
        controller: 'LoginCtrl'
      },
  'footer':{
              
              templateUrl:'views/footer.html',
              controller: 'FooterCtrl'
  },
}

})
  .state('inputFile',{    
    url:"/inputFile",
    templateUrl:'views/inputXml.html',
    controller: 'AppCtrl'
  })
  .state('userReg',{
    url:"/userReg",
    templateUrl:"views/UserRegistration.html",
    controller: "UserRegCtrl"
  })
  .state('forgotPass',{
    url:'/forgotPass',
    templateUrl:'views/ForgotPassword.html',
    controller: 'ForgotPassCtrl'
  })
  .state('extractAttrib',{
    url:'/extractAttrib',
    templateUrl:'views/ExtractAttributes.html',
    controller: 'ExtractAttribCtrl'
  })
  /*.state('downloadFile',{
    url:'/downloadFile',
    templateUrl:'views/DownloadExcel.html',
    controller: 'DownloadControl'
  })*/
  
});
 /*.run( function($rootScope, $location) {

    // register listener to watch route changes
    $rootScope.$on( "$routeChangeStart", function(event, next, current) {
      if ( $rootScope.loggedIn == false ) {
        // no logged user, we should be going to #login
        if ( next.templateUrl == "login.html" ) {
          // already going to #login, no redirect needed
        } else {
          // not going to #login, we should redirect now
          $location.path( "/login" );
        }
      }         
    });
 });*/