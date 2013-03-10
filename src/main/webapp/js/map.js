$(window).load(function() {
	var kiev = new google.maps.LatLng(50.4501, 30.5234);

	var mapOptions = {
	  center: kiev,
	  zoom: 12,
	  mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	
	map = new google.maps.Map(document.getElementById("map"),
	    mapOptions);

	google.maps.event.addListener(map, 'click', function(event) {
 		var marker = new google.maps.Marker({
		    position: event.latLng,
		    map: map
		});
  	});	
});