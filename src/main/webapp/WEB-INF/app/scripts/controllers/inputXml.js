    angular.module('bulkAttribValExtractor')
   .directive('fileModel', ['$parse', function ($parse) {
              return {
                 restrict: 'A',
                 link: function(scope, element, attrs) {
                    var model = $parse(attrs.fileModel);
                    var modelSetter = model.assign;
                    
                    element.bind('change', function(){
                       scope.$apply(function(){
                          modelSetter(scope, element[0].files[0]);
                       });
                    });
                 }
              };
           }]) 

  .service('fileUpload', ['$http','$rootScope', function ($http,$rootScope) {
              this.uploadFileToUrl = function(file, /*attributes,*/uploadUrl,currFileUploaded){
                 var fd = new FormData();
                 fd.append('file', file);
                 console.log('json of file:',JSON.stringify(fd));
              
                 $http.post(uploadUrl, fd, {
                    transformRequest: angular.identity,
                    headers: {'Content-Type': undefined}
                 })      
                 .success(function(response){  
                  console.log("response:"+JSON.stringify(response));
                  
                  //$rootScope.xmlAttrib[]
                  alert("File Uploaded successfully");
                  $scope.nextPage = "Next Page";
                  console.log(currFileUploaded);
                   //rootscope var to handle the list of attributes per xml to populate the attribute list
                  for(rootKey in $rootScope.xmlAttrib)
                  {
                    console.log($rootScope.xmlAttrib[rootKey].id);
                      if($rootScope.xmlAttrib[rootKey].id==currFileUploaded)
                      {
                          console.log("hello"+$rootScope.xmlAttrib[rootKey].id);
                          //console.log("response.attributes:"+response.attributes);
                          
                          for( var i = 0;i<response.length;i++)
                          {
                            for(key in response[i].attributes)
                            {

                              console.log("response[i].attributes.attribName:"+JSON.stringify(response[i].attributes[key]));
                              console.log("key:"+key);
                              $rootScope.xmlAttrib[rootKey].value.push(response[i].attributes[key].attribName);  
                            }
                            
                            
                            console.log("$rootScope.xmlAttrib[rootKey].value:"+$rootScope.xmlAttrib[rootKey].value);
                          }
                          
                      }
                  }               
                console.log("$rootScope.xmlAttrib:"+JSON.stringify($rootScope.xmlAttrib));
                 })            
                 .error(function(response){
                  console.log(response);
                  alert("fail");
                 });
              }
           }])
        
  .controller('AppCtrl', ['$scope', 'fileUpload','$location','$rootScope', function($scope, fileUpload, $location,$rootScope){
              
              $scope.inputXMLs=[{ 
                    'fname': '', 
                    'lname': '',
                    'email': '',
                }];

              $scope.uploadFile = function(){
                 var file = $scope.inputXMLs[0].myFile;
                 var attrib = "empID";
                 
                 console.log('file is ' );
                 console.dir(file);
                 $rootScope.xmlAttrib.push({
                  id:file.name,
                  value:[]
                 });
                 var currFileUploaded =file.name;
                 
                 var uploadUrl = "http://localhost:8080/BulkAttributeExtractor/upload";
                 fileUpload.uploadFileToUrl(file,/*attrib,*/ uploadUrl,currFileUploaded);
              };

  //to add new rows to the table
              $scope.addNew = function(inputXMLs){
              console.log($scope.inputXMLs);
              console.log("date:"+$rootScope.date);
                $scope.inputXMLs.push({ 
                    'fname': '', 
                    'lname': '',
                    'email': '',
                });
                $scope.PD = {};              
                
            };
        
  //to remove the added rows from the table
            $scope.remove = function(){
                var newDataList=[];
                $scope.selectedAll = false;
                if($scope.inputXMLs.length!=1){
                angular.forEach($scope.inputXMLs, function(selected){
                    if(!selected.selected){
                        newDataList.push(selected);
                    }
                }); 
                $scope.inputXMLs = newDataList;  
                }            
            };

  //to check all checkboxes
            $scope.checkAll = function () {
                if (!$scope.selectedAll) {
                    $scope.selectedAll = true;
                } else {
                    $scope.selectedAll = false;
                }
                angular.forEach($scope.inputXMLs, function (inputXMLs) {
                    inputXMLs.selected = $scope.selectedAll;
                });
            };  

            $scope.navToNextPage = function(){
              $location.path( "/extractAttrib" );
            }  





           }]);