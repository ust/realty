var map = null;

function initialize() {
	var kiev = new google.maps.LatLng(50.4501, 30.5234);

	var mapOptions = {
	  center: kiev,
	  zoom: 12,
	  mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	
	map = new google.maps.Map(document.getElementById("map"),
	    mapOptions);

	google.maps.event.addListener(map, 'click', function(event) {
    	placeMarker(event.latLng);
    	// Send new point to home  
  	});	
}

function placeMarker(location) {
  var marker = new google.maps.Marker({
      position: location,
      map: map
  });  
}