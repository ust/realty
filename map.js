window.map = null;

window.onload = function () {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(displayLocation, displayError);
	} else {
		// let some shit out..
	}
}

function displayLocation (position) {
	window.map = new google.maps.Map(
		document.getElementById("map"), 
		{	zoom: 10,
			center: new google.maps.LatLng(
				position.coords.latitude, 
				position.coords.longitude
			),
			mapTypeId: google.maps.MapTypeId.ROADMAP
		}
	);
}

function displayError (error) {
	// body...
}