$(function(){
	//alert("hey");
	$( "#add" ).on( "click", function( event ) {
		$( "h4" ).html("Add!");
	});

	$( "#search" ).on( "click", function( event ) {
		$( "h4" ).html("Search!");
		$("div.alert").toggleClass('alert-block');
		$("div.alert").toggleClass('alert-success');

		$.getJSON('number.json', $("input.search-query").value(), function(broker) {
			$("div.alert").append("<p>"+broker.name+"</p>");
		}).fail(function() {
			// fail			
		});

	});

});