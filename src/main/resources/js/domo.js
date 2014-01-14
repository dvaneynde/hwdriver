/*
 * JS code for domotic system
 */

/**
 * element is een HTMLInputElement localhost:8080/domo/act/LichtInkom/toggle
 */
function sendToggle(element) {
	// alert("toggle(): name=" + element.name);
	$.ajax("act/" + element.name + "/toggle");
}

function sendQuickie(name) {
	// alert("sendQuickie, name="+name);
	$.ajax("quick/"+name);
}

function sendLevelDL(inputRange) {
	// alert("sendLevelDL(): send act/" + inputRange.name + "/" + inputRange.value);
	$.ajax("act/" + inputRange.name + "/" + inputRange.value);
}

//function registerDimmedLampChangeEvents(el, timeout) {
//	var timer;
//	var trig = function() {
//		alert("Send name=" + el.name + ", level=" + el.value);
//	};
//	el.bind("change", function() {
//  	zolang changes snel binnenkomen wordt timer reset; als ze 'timeout' lang niet meer binnenkomen wordt trig opgeroepen
//		if (timer) {
//			clearTimeout(timer);
//		}
//		timer = setTimeout(trig, timeout);
//	});
//}
