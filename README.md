# Deploying the server locally
CleanAir infrastructure consists of a Node.js web server and MySQL database. In order to deploy  the website and API, you will need to install both Node.js and MySQL.

## Prerequisites
The following two components must be installed before following the below directions.
 - [Node.js ~ Current Build](https://nodejs.org/en/)
 - [MySQL Community Edition Server](https://dev.mysql.com/downloads/installer/)

After installing MySQL server, run the MySQL Command Line Client and run the command: **CREATE DATABASE SE491;** to create a MySQL table for the project. See the below screenshot for an example.

![MySQL table creation](http://i.imgur.com/aOtxF8f.png)

## Setup
The following steps will walk you through downloading a local copy of the repository and setting it up to run locally.
### Part 1 - Cloning and configuring 
1. Clone the most update to date git branch of the CleanAir repository, [available here](https://github.com/SAREC-Lab/SEStudio-Environment).
2. Open server\api\models\db.js in a text editor.
3. Update lines 4 and 5 (highlighted below) to match the root MySQL username and password that you specified during the installation.
![MySQL config](http://i.imgur.com/cBvZiIQ.png)

### Part 2 - Launching the server
1. Open a command prompt and navigate to the server folder.
2. Type the following command to install required modules: **npm install** 
4. Type the following command to start the installer and create the the MySQL DB (on first run): **node app.js**
5. You should see output similar to "Web server listening on port undefined"

You can now access the website at http://localhost:5000/. The two relevant map files demoed throughout studio are viewable at http://localhost:5000/map.html and http://localhost:5000/heatmap2.html. They will display no data since the database is empty by default. Part 3 covers adding data using a test script.

### Part 3 - Adding test data
Navigate to  http://localhost:5000/data and follow each test step in order to add and view test data to the database



# CleanAir API
The CleanAir API is accessible at 168.62.234.19
## Node retrieval -- HTTP GET

*Getting all nodes with all fields:*
> api/nodes [GET]

###gas Parameter
The specific gas returned can be specified by passing the ***gas*** parameter. ID, LAT, LONG, and createdAt fields will always be returned, even when not specified as they are fundamental fields.

*Example*: Get co_Lvl (with ID, LAT, LONG, createdAt):
> api/nodes?gas=co


###minDate and maxDate Parameters
The ***minDate*** and ***maxDate*** parameters can be used to narrow the retrieved nodes to a specific date/time range. By default, the ***minDate*** and ***maxDate*** parameters do not need to be specified. Dates must be specifed in EPOCH format.

*Example*: Get all gas data and accompanying UserId (with ID, LAT, LONG, createdAt) between:
> api/nodes?minDate=2016-04-26T23:50:34.000Z&maxDate=2016-04-26T23:52:27.000Z


###lat and long Parameters
The ***lat*** and ***long*** parameters can be used to narrow the retrieved nodes to the area between two latitudinal and/or longitudinal points. By default, ***lat*** and ***long*** parameters do not need to be specified. If they are specified, they must be specified in pairs or they will be ignored.

*Example*: Get all nodes between lat 41.87566252 and lat 41.87656252 (with ID, LAT, LONG, createdAt):
> api/nodes?lat=41.87566252&lat=41.87656252


###attrib Parameter
Additional fields can be requested by passing the ***attrib*** parameter. ID, LAT, LONG, and createdAt fields will always be returned, even when not specified as they are fundamental fields.

*Example*: Get co2 gas and accompanying UserId (with ID, LAT, LONG, createdAt):
> api/nodes?attrib=co2&attrib=UserId


##Node Insertion -- HTTP POST
*Creating a new node:*
> api/nodes [POST]

+ Attributes
    + lat *(required attribute)* - node latitude
    + long *(required attribute)* - node longitude
    + userId - ID of user inserting data
    + co_Lvl - node CO level
    + co2_Lvl - node CO2 level
    + o3_Lvl - node O3 level
    + no2_Lvl - node NO2 level
+ Request type: application/x-www-form-urlencoded
+ Success response
	+  HTTP Status: 201 (Created)
+ Failure response
	+ HTTP Status: 400 (Bad Request)
	+ JSON error message
        + SequelizeValidationError - A null value was passed for a required attribute


##Authentication -- HTTP POST
*Creating a new user account:*
> api/register [POST]

+ Attributes
    + fullname *(required attribute)* - user's first and last name
    + username *(required attribute)* - user's chosen username, must be unique
    + email *(required attribute)* - user's email, must be unique
    + password *(required attribute)* - user's password (encrypted upon DB insertion)
+ Request type: application/x-www-form-urlencoded
+ Success response
    + HTTP Status 201 (Created)
    + JSON error message
        + SequelizeValidationError - A null value was passed for a required attribute
        + SequelizeUniqueConstraintError - An already used email or username has been specified
		
		
#User Manual

###Hardware Setup
1. Attach each of the 4 sensors to the gas board.
2. Insert bluetooth chip onto the Waspmote, then insert the gasboard with attached sensors.  Plug the battery into the Waspmote.
3. By using the associated Waspmote IDE, upload the program fast_main_measurement.pde to the board (this will get sensor readings every 15 seconds).
4. If program uploads succesfully, the Wasmpote will now run the measurement program whenever it is powered on.  The Waspmote is now ready for use with the mobile app.
5. Click the link below for a youtube tutorial on developing a program that will get gas sensor data from the Waspmote:
	- [Waspmote development tutorial](https://www.youtube.com/watch?v=zcsJBDPNhws&feature=youtu.be)
6. More information on the specifics of hardware setup and program development can be found via official Waspmote documentation:
	- [Waspmote programming documentation](http://www.libelium.com/development/waspmote/documentation/?cat=programming)
	- [Waspmote technical guide](http://www.libelium.com/development/waspmote/documentation/waspmote-technical-guide/)
	- [Gas sensor board manual](http://www.libelium.com/development/waspmote/documentation/gases-board-technical-guide/)
	- [Using Bluetooth low energy on Waspmote](http://www.libelium.com/development/waspmote/documentation/bluetooth-low-energy-networking-guide/)
	

###Linking Waspmote to Mobile App to Collect Data
1. Turn on the Waspmote to start the measurement program.  The Waspmote will now wait for 2 minutes for the mobile app to establish an initial connection via bluetooth as well as heat up the gas sensors that need heating.
2. Open the mobile app. From the main screen, open the menu and click the settings option.  Under general, check the box to indicate that you want to collect gas readings.  Navigate back to main screen.  If bluetooth is not enabled, a prompt to enable it will display.
3. A message will display when the mobile app establishes the initial bluetooth link to the Waspmote.
4. The Waspmote will continue performing its measurement cycle in the background. Any time sensor data is gathered, it will be sent to the mobile app which will then post that data to the web server.

###Viewing Maps
1. From the main screen, open the menu and select which map you would like to view (heatmap or marker map).
2. For each map, the user can select which specific gas he or she would like to monitor using the associated check boxes.  The user can also use the drop down to select a time span.

###Configuring Notifications
1. From the main screen, open the menu and select settings. From the settings menu, select the notifications.
2. The user can use the checkbox to indicate if he or she would like to receive notifications from the app. If selected, the app will monitor the data from the Waspmote in addition to the surrounding area and send push notifications if any gas reaches a dangerous level.
3. The user can also select one or more gas values and manually enter in a gas level that should be considered by the app to be dangerous.  Notifications sent will be with respect to this newly indicated gas level.
