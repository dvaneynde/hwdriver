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

function sendLevelDL(inputRange) {
	// alert("sendLevelDL(): send act/" + inputRange.name + "/" + inputRange.value);
	$.ajax("act/" + inputRange.name + "/" + inputRange.value);
}

//function sendLevelDL2(el, timeout) {
//	var timer;
//	var trig = function() {
//		alert("Send name=" + el.name + ", level=" + el.value);
//	};
//	el.bind("change", function() {
//		if (timer) {
//			clearTimeout(timer);
//		}
//		timer = setTimeout(trig, timeout);
//	});
//}
