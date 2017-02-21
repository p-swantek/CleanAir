window.nodes = {};
// nodeSpacing format {ZoomLevel:Spacing,ZoomLevel:Spacing}
window.nodeSpacing = {7:0.2,8:0.03,9:0.02,10:0.01,11:0.008,12:0.006,13:0.004,14:0.003,15:0.0025,16:0.001,17:0.0005,18:0.0003,19:0.0001,20:0.0001};
var mapType = "marker";
// How Often The Map Refreshes, Measured in ms
var refreshEvery = 60000;

// Handle Drag and Zoom Events
google.maps.event.addListener(map, 'zoom_changed', function() {
	SensorMap.updateDisplayedNodes();
});
google.maps.event.addListener(map, 'dragend', function() { 
	SensorMap.updateDisplayedNodes();
});
// Create Sensor Icons
// Blue for normal, Yellow for Warning, Red for Hazardous
var blueIcon = {url:'images/m1.png'};
var yellowIcon = {url:'images/m2.png'};
var redIcon = {url:'images/m3.png'};

// End Static Data
// *********************************************************************************

// sensorNode class
// Each node on the map is a sensorNode
// *********************************************************************************
var sensorNode = function(id, thisLat, thisLng, thisTime, value) {
	this.nodeID = id;
	this.lat = thisLat;
	this.long = thisLng;
	this.value = value;
	this.time = thisTime;
	//console.log(value);
	this.marker = new MarkerWithLabel({
				position: {lat: thisLat, lng: thisLng},
				icon: blueIcon,
				labelContent: value.toString(),
				labelClass: "labels",
				labelAnchor: new google.maps.Point(21, 30),
				title: id
			});

	var marker = this.marker;
	this.refreshMarker = function()
	{
		marker.set('icon',{url:'images/m1.png'});
		if(value >= sensorTypes[window.activeControl].warning) {
			marker.set('icon',{url:'images/m2.png'});
			marker.set('labelAnchor', new google.maps.Point(20, 31));
		}
		if(value >= sensorTypes[window.activeControl].hazard) {
			marker.set('icon',{url:'images/m3.png'});
			marker.set('labelAnchor', new google.maps.Point(20, 36));
		}
		marker.setMap(window.map);
	}

	this.delete = function(index)
	{
		marker.setMap(null);
		delete window.nodes[index];
		//console.log("Delete");
	}
}
// A nodeGroup Averages the data of overlapping dataPoints
var nodeGroup = function()
{
	var OuterThis = this;
	this.group = {};
	this.size = 0;

	this.addNode = function(node)
	{
		// Insert Node
		OuterThis.group[node.id.toString()] = node;
		OuterThis.size++;
	}
}
// sensorMap class
// Creates/Stores nodeCache and Manages
// *********************************************************************************
var sensorMap = function() {
	var OuterThis = this;
	this.cache = new nodeCache();
	this.displayedNodes = {};
	this.size = 0;
	this.spanDays = 0.0833;

	this.newestNode = null;
	this.oldestNode = null;

	this.setSpan = function(span)
	{
		OuterThis.spanDays = span;
	}

	this.addNode = function(node)
	{
		if(node.nodeID in OuterThis.displayedNodes) {
			return false;
		} else {
			if(OuterThis.newestNode < node.time || OuterThis.newestNode == null) {
				OuterThis.newestNode = node.time;
			}
			if(OuterThis.oldestNode > node.time || OuterThis.oldestNode == null) {
				OuterThis.oldestNode = node.time;
			}
			OuterThis.size++;
			OuterThis.displayedNodes[node.nodeID] = node;
			node.refreshMarker();
			return true;
		}
	}

	this.deleteNode = function(i)
	{
		OuterThis.displayedNodes[i].delete();
		delete OuterThis.displayedNodes[i];
		OuterThis.size--;
	}

	this.flushMap = function()
	{
		OuterThis.cache.flushCache();
		$.each(OuterThis.displayedNodes, function(i, node) {
			OuterThis.deleteNode(i);
		});
		OuterThis.newestNode = null;
		OuterThis.oldestNode = null;
	}

	// If the newest or oldest node is removed, force re-discovery
	this.recalcSpan = function()
	{
		console.log("Recalc Span");
		OuterThis.newestNode = null;
		OuterThis.oldestNode = null;
		$.each(OuterThis.displayedNodes, function(i, node) {
			if(OuterThis.newestNode < node.time || OuterThis.newestNode == null) {
				OuterThis.newestNode = node.time;
			}
			if(OuterThis.oldestNode > node.time || OuterThis.oldestNode == null) {
				OuterThis.oldestNode = node.time;
			}
		});
	}
	// This should look for situations where nodes should no longer be active
	// Being out of bounds, too close to another node from a 
	// zoom level change, or to Old after span change
	this.cleanUp = function()
	{
		var count = 0;
		var bounds = map.getBounds();
		var maxTime = new Date(OuterThis.cache.getMaxTime());
		var oldestWanted = new Date(maxTime);
		var recalcTime = false;
		oldestWanted.setDate(maxTime.getDate()-OuterThis.spanDays);

		$.each(OuterThis.displayedNodes, function(i, node) {
			var delNode = false;
			if(OuterThis.safeDistance(node) == false) {
				console.log("Failed Safe Distance Check");
				delNode = true;
			} else if(node.lat < bounds["H"]["H"] || node.lat > bounds["H"]["j"] || node.long > bounds["j"]["H"] || node.long < bounds["j"]["j"]) {
				console.log("Failed Bounds Check");
				delNode = true;
			} else if(node.time < oldestWanted) {
				console.log("Failed TimeSpan Check");
				delNode = true;
			}
			if(delNode == true) {
				if(node.time == OuterThis.newestNode || node.time == OuterThis.oldestNode) recalcTime = true;
				OuterThis.deleteNode(i);
				count++;
			}
		});
		if(recalcTime == true) OuterThis.recalcSpan();
		return count;
	}

	this.safeDistance = function(thisNode)
	{
		var safe = true;
		$.each(OuterThis.displayedNodes, function(i, node) {
			if(thisNode !== node) {
				var spacing = window.nodeSpacing[window.map.getZoom()];
				var latDiff = Math.abs(Math.abs(node.lat) - Math.abs(thisNode.lat));
				var longDiff = Math.abs(Math.abs(node.long) - Math.abs(thisNode.long));
				if( (latDiff < spacing) && (longDiff < spacing) ) {
					if(safe === true) safe = false;
					if(thisNode.time > node.time) safe = node;
				}
			}
		});
		return safe;
	}

	this.refreshTime = function()
	{
		OuterThis.recalcSpan();
		window.CM.refreshTime(OuterThis.oldestNode, OuterThis.newestNode);
	}
	this.updateDisplayedNodes = function()
	{
		var removed = OuterThis.cleanUp();
		var added = 0;
		var activeNodes = OuterThis.cache.getNewest(OuterThis.spanDays);
		$.each(activeNodes, function(i, node)
		{
			// Package into a Node Class
			var thisNode = new sensorNode(node.id.toString(), parseFloat(node.lat), parseFloat(node.long), new Date(node.createdAt), node[window.activeControl+"_Lvl"]);
			// Verify Spacing
			var doneEvaluating = false;
			while(doneEvaluating === false) {
				var nodeSafe = OuterThis.safeDistance(thisNode);
				// True is safe to place new node
				if(nodeSafe === true) {
					OuterThis.addNode(thisNode);
					added++;
					doneEvaluating = true;
				// False implies a collision where a newer node already exists
				} else if(nodeSafe === false) {
					// Do Nothing
					doneEvaluating = true;
				// Otherwise, return a node to be replaced
				} else {
					OuterThis.deleteNode(nodeSafe.nodeID);
					added--;
				}
			}
		});
		OuterThis.refreshTime();
		console.log("Displayed Nodes " + added + " Added, " + removed + " Removed " + OuterThis.size + " Total " + window.performance.memory.totalJSHeapSize + " Memory Used");
	}
}