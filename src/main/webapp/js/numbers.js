$(function(){

	$( "#add" ).on( "click", function( event ) {
		$( "h4" ).html("Add!");
	});

	$( "#search" ).on( "click", function( event ) {
		$( "h4" ).html("Search!");
		$("div.alert").toggleClass('alert-block');
		$("div.alert").toggleClass('alert-success');

		$.getJSON('number.json', $("input.search-query").value(), function() {
			// done
		}).fail(function() {
			// fail			
		});

	});

});