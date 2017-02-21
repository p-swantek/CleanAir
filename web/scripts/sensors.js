// Define sensorType class
var sensorType = function(type, warning, hazard) {
	this.type = type;
	this.warning = warning;
	this.hazard = hazard;
}
// Sensor Types are Static
var sensorTypes = {
	Co:new sensorType("Co",35,100),
	Co2:new sensorType("Co2",600,1000),
	O3:new sensorType("O3",0.1,1),
	No2:new sensorType("No2",0.04,20)
};