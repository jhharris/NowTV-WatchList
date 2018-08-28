#### NowTV WatchList service Implementation

##### Run Service

* From the project directory
   * sbt clean compile run
* Server will run on port 8080

##### Run Test Suite
* From the project directory
   * sbt clean test
    
##### Example REST requests

**All requests use the path: /watchlist/{customerId}**


*To retrieve the watchlist for a customer*

**GET** 

curl -X GET http://localhost:8080/watchlist/12345


*To add a contentID to a customer's watchlist*

**PUT** 

curl -X PUT -H "Content-Type: application/json" -d '{"contentId":"11111"}' http://localhost:8080/watchlist/12345


*To delete a contentId from a customer's watchlist*

**DELETE** 

 curl -X DELETE -H "Content-Type: application/json" -d '{"contentId":"11111"}' http://localhost:8080/watchlist/12345
 

##### Assumptions
* Service URL path is /watchlist/{customerId}
* The watch list response is sorted into natural order.
* Only one item can be added or removed from the watchlist in a single request
* No requirement for logging / audit.

##### Improvements
* The tests can be refactored to remove test boilerplate duplication.
* Validation only checks that:
    * Customer IDs is a 5 digit numeric string.
    * Content ID is 5 digit alphanumeric string.
