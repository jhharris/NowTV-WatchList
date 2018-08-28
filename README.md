####NowTV WatchList service Implementation

##### Jason Hodges-Harris
jasonhh@mac.com

##### Run Service

* From the project directory
   * sbt clean compile run
* Server will run on port 8080

    
##### Example REST requests

**All requests use the path: /watchlist/{customerId}**

*To retrieve the watchlist for a customer*

**GET** http://localhost:8080/watchlist/{customerId}

*To add a contentID to a customer's watchlist*

**PUT** http://localhost:8080/watchlist/{customerId}
* Headers: Content-Type application/json
* Request body: { "contentId" : "11111"}  

*To delete a contentId from a customer's watchlist*

**DELETE** http://localhost:8080/watchlist/{customerId}
* Headers: Content-Type application/json
* Request body: { "contentId" : "11111"} 


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
