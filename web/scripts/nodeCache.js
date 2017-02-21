var nodeCache = function()
{
	var OuterThis = this;
	this.allNodes = {};
	var AllNodes = this.allNodes;

	this.activeNodes = {};
	var ActiveNodes = this.activeNodes;

	this.refreshEvery = 2000;
	this.size = 0;
	this.activeSize = 0;
	this.maxTime = Date.parse("March 1, 2000");
	this.midQuery = false;

	this.getMaxTime = function()
	{
		return OuterThis.maxTime;
	}

	this.addNode = function(node)
	{
		// If Node Exists See If It Needs Updating
		if(node.id.toString() in AllNodes) {
			return false;
		} else {
			AllNodes[node.id.toString()] = node;
			OuterThis.size++;
			return true;
		}
	}

	this.deleteNode = function(node)
	{
		delete OuterThis.allNodes[node.id.toString()];
		OuterThis.size--;
	}

	this.addActive = function(node)
	{
		var id = node.id.toString();
		// Verify it isn't already inserted
		if( !(id in OuterThis.activeNodes) ) {
			// Keep maxTime
			var nodeTime = new Date(node.createdAt);
			if(nodeTime > OuterThis.maxTime) {
				OuterThis.maxTime = nodeTime;
			}
			// Insert Node
			OuterThis.activeNodes[id] = node;
			OuterThis.activeSize++;
		}
	}

	// Returns True if Recheck Requied
	this.deleteActive = function(node)
	{
		delete OuterThis.activeNodes[node.id.toString()];
		OuterThis.activeSize--;
		// If Node With maxTime is removed, find Successor
		var nodeTime = new Date(node.createdAt);
		if(nodeTime == OuterThis.maxTime) {
			return false;
		}
		return true;
	}

	this.flushCache = function(node)
	{
		// Wait for existing query to complete
		if(OuterThis.midQuery == true) {
			setTimeout(function () {
				OuterThis.flushCache();
			}, 30);
		} else {
			$.each(OuterThis.allNodes, function(i, node) {
				OuterThis.deleteNode(node);
			});
			$.each(OuterThis.activeNodes, function(i, node) {
				OuterThis.deleteActive(node);
			});
			OuterThis.maxTime = Date.parse("March 1, 2000");
			OuterThis.refreshCache();
		}
	}

	this.getSize = function()
	{
		return OuterThis.size;
	}

	this.getActiveSize = function()
	{
		return OuterThis.activeSize;
	}

	this.getActiveNodes = function()
	{
		OuterThis.updateActive();
		var returnedCopy = {};
		$.each(OuterThis.activeNodes, function(i, node) {
			returnedCopy[i] = node;
		});
		return returnedCopy;
	}

	this.getNewest = function(spanDays)
	{
		OuterThis.updateActive();
		var counter = 0;
		var returnedCopy = {};
		// oldestWanted Nodes - Offset is in Days
		// Start with newest node and subtract number of days to go back
		var oldestWanted = new Date(OuterThis.maxTime);
		oldestWanted.setDate(OuterThis.maxTime.getDate()-spanDays);
		//console.log("DEBUG " + spanDays + " n " + oldestWanted.toString() + " AND " + OuterThis.maxTime.toString());
		$.each(OuterThis.activeNodes, function(i, node) {
			var nodeTime = new Date(node.createdAt);
			if(nodeTime > oldestWanted) {
				returnedCopy[i] = node;
				counter++;
			}
		});
		console.log(counter + " Newest Nodes Returned");
		return returnedCopy;
	}

	this.recheckTime = function()
	{
		OuterThis.maxTime = Date.parse("March 1, 2000");
		$.each(OuterThis.activeNodes, function(i, node) {
			var nodeTime = new Date(node.createdAt);
			if(nodeTime > OuterThis.maxTime) {
				OuterThis.maxTime = nodeTime;
			}
		});
		console.log("Recalculated maxTime : " + OuterThis.maxTime);
	}

	// Check Bounds And Activate / Deactive Nodes
	this.updateActive = function()
	{
		var bounds = map.getBounds();
		$.each(AllNodes, function(i, node) {
			// minLat: bounds["H"]["H"], maxLat: bounds["H"]["j"], minLong: bounds["j"]["H"], maxLong: bounds["j"]["j"]
			var nodeLat = parseFloat(node.lat);
			var nodeLong = parseFloat(node.long);
			// See if node is in bounds
			if(nodeLat > bounds["H"]["H"] && nodeLat < bounds["H"]["j"] && nodeLong < bounds["j"]["H"] && nodeLong > bounds["j"]["j"])
			{
				OuterThis.addActive(node);
			} else {
				// If out of bounds and active, deactivate it
				if(i in OuterThis.activeNodes) {
					delete OuterThis.activeNodes[i];
					OuterThis.activeSize--;
				}
			}
		});
		console.log("Recalculated Active " + OuterThis.getSize() + " Total " + OuterThis.getActiveSize() + " Active");
	}

	// Perform a single refresh from server
	this.refreshCache = function()
	{
		OuterThis.midQuery = true;
		var bounds = map.getBounds();
		var attrib = window.activeControl+ "_Lvl";
		var addedAny = false;
		// Query Server
		$.get("api/nodes", {attrib: attrib, minLat: bounds["H"]["H"], maxLat: bounds["H"]["j"], minLong: bounds["j"]["H"], maxLong: bounds["j"]["j"]}, function(nodes) {
			// Add Each Returned Node
			$.each(nodes, function(i, node) {
				if(OuterThis.addNode(node)) addedAny = true;
			});
			// If there was a change, trigger a re-draw
			if(addedAny) {
				console.log("Cache Changed " + OuterThis.getSize() + " Total " + OuterThis.getActiveSize() + " Active ");
				SensorMap.updateDisplayedNodes();
			}
			OuterThis.midQuery = false;
		});
	}

	// Keep a continuous loop to refresh data from server
	this.autoRefresh = function()
	{
		setTimeout(function () {
			OuterThis.refreshCache();
			OuterThis.autoRefresh();
		}, OuterThis.refreshEvery);
	}
	this.autoRefresh();
	console.log("Cache Initalized");
}