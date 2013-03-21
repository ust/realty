$(window).load(function() {
	var kiev = new google.maps.LatLng(50.4501, 30.5234);

	var mapOptions = {
	  center: kiev,
	  zoom: 12,
	  mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	
	map = new google.maps.Map(document.getElementById("map"),
	    mapOptions);

	$.get('json/all', function (resp) {
		$.each(resp, function (index, item) {
			placeMarker(new google.maps.LatLng(item.lat, item.lang));
		});
	});

	google.maps.event.addListener(map, 'click', function(event) {
 		$.post('json/add', {
	 			lat: event.latLng.lat(),
	 			lng: event.latLng.lng()
	 		}, function () {
	 			placeMarker(event.latLng);
	 		}
	 	);
	 });
});

function placeMarker (loc) {
	var marker = new google.maps.Marker({
	    position: loc,
	    map: map
	});
}