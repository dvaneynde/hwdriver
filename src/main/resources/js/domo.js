/*
 * JS code for domotic system
 */

/**
 * element is een HTMLInputElement localhost:8080/domo/act/LichtInkom/toggle
 */
function sendToggle(element) {
	//alert("toggle(): name=" + element.name);
	$.ajax("act/" + element.name + "/toggle");
}
