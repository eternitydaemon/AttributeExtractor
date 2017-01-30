	/**
	 * @ngdoc function
	 * @name testAppYeomanApp.controller:MainCtrl
	 * @description
	 * # MainCtrl
	 * Controller of the testAppYeomanApp
	 */
	 angular.module('bulkAttribValExtractor')
	 .controller('LoginCtrl', function($scope,$location, $rootScope) {
	 	
	 	$scope.loginDetail=[];
		$rootScope.logVal = "Log In";
		$rootScope.signUp = "Sign Up";
		$rootScope.loggedIn = "";
		$rootScope.loggedUser = "";
		$rootScope.xmlAttrib=[];
	 	$scope.login=function()
	 	{
	 		console.log($scope.loginDetail);
	 		if($scope.loginDetail.uname=="test" && $scope.loginDetail.password=="test")
	 		{
	 			$rootScope.loggedUser =$scope.loginDetail;
	 			console.log("login success");
	 			$rootScope.logVal = "Log Out";
	 			$rootScope.loggedUser = "Welcome Test User";
	 			$rootScope.signUp = "";
	 			$rootScope.loggedIn = true;
	 			$location.path( "/inputFile" );
	 		}else
	 		{
	 			alert("Invalid username/password");
	 		}
	 	}   

//check if not logged in switch to login page
	 	$scope.$watch(function() { return $location.path(); }, function(newValue, oldValue){  
    if ($scope.loggedIn == false && newValue != '/login'){  
            $location.path('/login');  
    }  
});

	 });
