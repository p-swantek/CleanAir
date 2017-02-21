// Start Controls Section
// *********************************************************************************
var timeBox = function() {
  this.controlDiv = document.createElement('div');

  // Set CSS for the control border.
  var controlUI = document.createElement('div');
  //controlUI.style.backgroundColor = '#fff';
  //controlUI.style.border = '2px solid #fff';
  //controlUI.style.borderRadius = '3px';
  //controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
  controlUI.style.cursor = 'pointer';
  controlUI.style.marginLeft = '5px';
  controlUI.style.marginTop = '22px';
  controlUI.style.marginBottom = '2px';
  controlUI.style.textAlign = 'right';
  //controlUI.title = 'TestTitle';
  this.controlDiv.appendChild(controlUI);

  var timeSpanDiv = document.createElement('div');

  // Create Span Selector Drop Down
  var selectList = document.createElement("select");
  selectList.id = "spanSelect";
  selectList.onchange = function() {
    var timeSpan = $('#spanSelect').val();
    SensorMap.setSpan(timeSpan);
    SensorMap.updateDisplayedNodes();
  }
  // Options for Drop Down
  var spans = {"2hrs":0.0833,"3Days":3};
  $.each( spans, function( key, value ) {
    var option = document.createElement("option");
    option.value = value;
    option.text = key;
    selectList.appendChild(option);
  });
  var t = document.createTextNode("Time Span:");
  timeSpanDiv.appendChild(t);
  timeSpanDiv.appendChild(selectList);
  controlUI.appendChild(timeSpanDiv);


  // Add Div that Displays the time span
  var timeDisplay = document.createElement('div');
  timeDisplay.style.color = 'rgb(25,25,25)';
  timeDisplay.style.fontFamily = 'Roboto,Arial,sans-serif';
  timeDisplay.style.fontSize = '14px';
  //timeDisplay.style.lineHeight = '38px';
  timeDisplay.style.paddingLeft = '5px';
  timeDisplay.style.paddingRight = '5px';
  timeDisplay.style.display = 'inline';
  controlUI.appendChild(timeDisplay);

  this.setTime = function(time)
  {
    timeDisplay.innerHTML = time;
  }
  this.setTime(new Date());
}

// controlObjs are the checkboxes for the various gas sensors
var controlObj = function(sensorName, index) {

  this.controlDiv = document.createElement('div');
  this.controlDiv.index = index;
  this.type = sensorName;

  // Set CSS for the control border.
  var controlUI = document.createElement('div');
  controlUI.style.backgroundColor = '#fff';
  controlUI.style.border = '2px solid #fff';
  controlUI.style.borderRadius = '3px';
  controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
  controlUI.style.cursor = 'pointer';
  controlUI.style.marginLeft = '5px';
  if(index == 1) controlUI.style.marginTop = '22px';
  controlUI.style.marginBottom = '2px';
  controlUI.style.textAlign = 'center';
  //controlUI.title = 'Click to recenter the map';
  this.controlDiv.appendChild(controlUI);

  // Set CSS for the control interior.
  var controlText = document.createElement('div');
  controlText.style.color = 'rgb(25,25,25)';
  controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
  controlText.style.fontSize = '14px';
  //controlText.style.lineHeight = '38px';
  controlText.style.paddingLeft = '5px';
  controlText.style.paddingRight = '5px';
  controlText.style.display = 'inline';
  controlText.innerHTML = sensorName;
  controlUI.appendChild(controlText);

  this.checkbox = document.createElement('input');
  this.checkbox.type = "checkbox";
  this.checkbox.name = sensorName;
  this.checkbox.value = "value";
  this.checkbox.id = sensorName;
  if(index == 1) this.checkbox.checked = true;
  controlUI.appendChild(this.checkbox);
    
  
  // Setup the click event listeners
  controlUI.addEventListener('click', function() {
	window.CM.setActive(sensorName);
  });

  this.checkbox.addEventListener('click', function() {
	window.CM.setActive(sensorName);
  });
}

var controlFactory = function() {
	this.index = 1;
	
	this.add = function(name) {
		var control = new controlObj(name, this.index);
		window.map.controls[google.maps.ControlPosition.LEFT_TOP].push(control.controlDiv);
		this.index++;
		return control;
	}
}
var activeControl = null;
var controlMgr = function() {
  // Add TimeLapse
  var OuterThis = this;
  this.timeDisplay = new timeBox();
  window.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(OuterThis.timeDisplay.controlDiv);
  this.refreshTime = function(oldestNode, newestNode)
  {
    //console.log(oldestNode + " " + newestNode);
    var formatDate = function(input) {
      if(input == null) return "No Data";
      return input.getMonth()+"/"+input.getDate()+"/"+input.getFullYear()+" "+input.getHours()+":"+input.getMinutes();
    }
    var oldest = formatDate(oldestNode);
    var newest = formatDate(newestNode);
    OuterThis.timeDisplay.setTime(oldest+" TO "+newest);
  }

	// Add checkboxes for different sensor types
	var CF = new controlFactory();
	var controlSet = Array();
	
	$.each( sensorTypes, function( key, value ) {
		if(window.activeControl == null) window.activeControl = key;
		controlSet.push(CF.add(sensorTypes[key].type));
	});
	
	this.setActive = function(type) {
    // Check Proper Box
		if(window.activeControl == type) {
			$("#"+type).prop("checked", true);
			return false;
		}
    window.activeControl = type;
    // Uncheck Other Boxes
		$.each( controlSet, function(index) {
			var thisType = controlSet[index].type;
			if(thisType == type) $("#"+thisType).prop("checked", true);
			else $("#"+thisType).prop("checked", false);
		});
    if (mapType == "marker") {
		  SensorMap.flushMap();
    } else {
      window.heatmap.cfg.valueField = type.toLowerCase() + "_Lvl";
      window.heatmap.update();
      console.log(window.heatmap.cfg.valueField);
    }
	}
}
// End Controls Section
// *********************************************************************************