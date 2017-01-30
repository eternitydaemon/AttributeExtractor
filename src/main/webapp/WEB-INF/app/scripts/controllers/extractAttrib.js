  angular.module('bulkAttribValExtractor')
  .controller('ExtractAttribCtrl', ['$scope', 'fileUpload','$location','$http','$rootScope', function($scope, fileUpload,$location,$http,$rootScope){
  	$scope.attribDetails=[];
  	$scope.extract = "Extract";   

$scope.$on('$viewContentLoaded', function() {
  var data = [];
  for(var i = 0;i<Object.keys($rootScope.xmlAttrib).length;i++)
  {
    data.push({
                  id:$rootScope.xmlAttrib[i].id,
                  value:$rootScope.xmlAttrib[i].id
                 });
  }
  console.log("dropdown data:"+JSON.stringify(data));
  $scope.xmlAttribData=data;

  //for setting the row with a blank value
  $scope.attribDetails.push({ 
                    'fileName':'',
                    'attribName': '', 
                    'colName': '',
                    'dataValidation': '',
                });
});

$scope.populateAttrib= function()
{
    console.log("in populateAttrib");
    console.log("$scope.xmlAttribData:"+$scope.xmlAttribData);
    console.log("$scope.xmlDropDown:"+$scope.xmlDropDown);
    for(key in $rootScope.xmlAttrib)
    {
      console.log("$rootScope.xmlAttrib[$scope.xmlDropDown].value:"+$rootScope.xmlAttrib[key].value);
      //stores the autocomplete value (attribute name) for the specific xml
     $scope.attribName =$rootScope.xmlAttrib[key].value;
    }
    if($scope.attribDetails.length==1)
  {
    $scope.disableRemove = true;
  }
    
}
  
  //to add new rows to the table
              $scope.addNew = function(attribDetails){
                console.log('dropdown:'+JSON.stringify($scope.xmlAttribData));
              console.log($scope.attribDetails);
                $scope.attribDetails.push({ 
                    'fileName':'',
                    'attribName': '', 
                    'colName': '',
                    'dataValidation': '',
                });
            };
        
  //to remove the added rows from the table
            $scope.remove = function(){
                var newDataList=[];
                $scope.selectedAll = false;
                console.log("in remove");
                if($scope.attribDetails.length!=1){
                angular.forEach($scope.attribDetails, function(selected){
                  console.log("selected:"+selected);
                    if(!selected.selected){
                        newDataList.push(selected);
                    }
                }); 
                $scope.attribDetails = newDataList;
              }
            };

  //to check all checkboxes
            $scope.checkAll = function () {
                if (!$scope.selectedAll) {
                    $scope.selectedAll = true;
                } else {
                    $scope.selectedAll = false;
                }
                angular.forEach($scope.attribDetails, function (attribDetails) {
                    attribDetails.selected = $scope.selectedAll;
                });
            }; 


    //to get the data for xml extraction
            $scope.extractData = function()
            {
            	console.log("send data for parsing");
              var url = "http://localhost:8080/BulkAttributeExtractor/getAttributes";
              var jsonData=[];
              for(key in $scope.attribDetails)
              {
                /*console.log("$rootscope.attribDetails in log:"+JSON.stringify($rootScope.xmlAttrib));
                for(keyVal in $rootScope.xmlAttrib)
                {
                    if($rootScope.xmlAttrib[keyVal].id==$scope.attribDetails[key].fileName)
                    {
                      //wrirte the code for genewrating the json
                      jsonData
                    }
                }*/
               // $scope.attribDetails[key].attribName = $scope.selected;
               /* jsonData.push( $scope.xmlDropDown:[{
                    "attribName": $scope.attribDetails.attribName, 
                    "colName": $scope.attribDetails.colName,
                    "dataValidation": $scope.attribDetails.dataValidation
              }]});*/

                jsonData[key] = $scope.attribDetails[key];
              }
              console.log('json of file:',jsonData);
              $http.post(url, jsonData)            
                 .success(function(jsonData,status, headers, config){
                  alert("success");
                  var blob = new Blob([jsonData], {
                      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                      });
                     /* var objectUrl = URL.createObjectURL(blob);
                      window.open(objectUrl);*/
                     saveAs(blob, "ParsedExcel.xlsx");    
                     $location.path('/inputFile');        
                 })   
                 //saveAs(new Blob([data],{type:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}), "excel.xlsx");           
                 .error(function(data){                  
                  alert("fail"+data);
                 });
            };
            $scope.navToPrevPage = function()
            {
              $location.path("/inputFile");
            }
           }]);