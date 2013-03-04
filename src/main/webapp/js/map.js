function initialize() {
	var myLatlng = new google.maps.LatLng(-25.363882,131.044922);

	var mapOptions = {
	  center: myLatlng,
	  zoom: 8,
	  mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	
	var map = new google.maps.Map(document.getElementById("map"),
	    mapOptions);

	var marker = new google.maps.Marker({
	    position: myLatlng,
	    title:"Hello World!"
	});
	marker.setMap(map);
}