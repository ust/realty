function initialize() {
	var myLatlng = new google.maps.LatLng(50.4501, 30.5234);

	var mapOptions = {
	  center: myLatlng,
	  zoom: 12,
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