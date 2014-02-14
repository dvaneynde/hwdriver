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
	$.ajax("quick/" + name);
}

function sendLevelDL(inputRange) {
	// alert("sendLevelDL(): send act/" + inputRange.name + "/" +
	// inputRange.value);
	$.ajax("act/" + inputRange.name + "/" + inputRange.value);
}

function refreshActuatorsTxt() {
	$.get("actuators_txt", function(data) {
		alert("Load was performed. Data=" + data);
	});
	// var acts = $.get("actuators_txt");
	// alert("actuator text: "+acts);
	// alert("Actuator 0="+acts[0]);
}

function refreshActuators() {
	$.getJSON("actuators", function(data) {
		// alert("Load was performed. Data0=" + data[0].name);
		for (i=0; i<data.length; i++) {
			act = data[i];
			$("#"+act.name).prop( "checked", act.on ).checkboxradio( "refresh" );
			if (act.type === "DimmedLamp") {
				$("#"+act.name+"_lvl").val(act.level);
				$("#"+act.name+"_lvl").slider( "option", "disabled", !act.on ).slider('refresh');
			}
		}
	});
}
// function registerDimmedLampChangeEvents(el, timeout) {
// var timer;
// var trig = function() {
// alert("Send name=" + el.name + ", level=" + el.value);
// };
// el.bind("change", function() {
// zolang changes snel binnenkomen wordt timer reset; als ze 'timeout' lang niet
// meer binnenkomen wordt trig opgeroepen
// if (timer) {
// clearTimeout(timer);
// }
// timer = setTimeout(trig, timeout);
// });
// }
