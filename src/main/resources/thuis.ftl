<!DOCTYPE html>
<html>
<head>
<meta charset="US-ASCII">
<title>Domotica v2</title>
<script>
function sendToggle(name) {
}

</script>
</head>
<!--
TOOD:
1. elke 5 seconden een refresh
2. bij click op checkbox, toggle doorsturen, en refresh
   function sendToggle(name) {
   }
-->
<body>
	<button id="veranda"
		onclick="location.href='quick/eten'">Veranda</button>
	<button id="tv"
		onclick="location.href='quick/tv'">TV</button>
	<button id="eco"
		onclick="location.href='quick/eco'">Eco</button>
	<button id="fel"
		onclick="location.href='quick/fel'">FEL</button>
	<div align="left"><br>	
	 <#list model.actuators as act>
		<input type="checkbox" name="${act.name}" value="${act.name}" <#if act.parms["on"] = "1">checked</#if> >${act.description}</input> <br>
	</#list>
	</div>
</body>
</html>