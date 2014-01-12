<!DOCTYPE html>
<html>
<head>
	<meta charset="US-ASCII">
	<meta name="viewport" content="width=320, maximum-scale=1.0" />
	<title>Domotica v2</title>
	<script type="text/javascript" src="js/jquery-2.0.3.js"></script>
	<script type="text/javascript" src="js/domo.js"></script>
	<style type="text/css" media="all">
div.block{
  overflow:hidden;
}
div.block .toggle{
  width:150px;
  display:block;
  float:left;
  text-align:left;
}
div.block .slider{
  margin-left:4px;
  float:left;
}
	</style>
	<style type="text/css" media="handheld">
	</style>
</head>
<!--
TOOD:
1. elke 5 seconden een refresh
2. bij click op checkbox, toggle doorsturen, en refresh
   function sendToggle(name) {
   }
-->
<body>
<!--
	<button id="veranda"
		onclick="location.href='quick/eten'">Veranda</button>
	<button id="tv"
		onclick="location.href='quick/tv'">TV</button>
	<button id="eco"
		onclick="location.href='quick/eco'">Eco</button>
	<button id="fel"
		onclick="location.href='quick/fel'">FEL</button>
		-->
		<#list model.actuators as act>
		<div class="block">
			<span class="toggle">
				<input type="checkbox" name="${act.name}" value="${act.name}" <#if act.parms["on"] = "1">checked</#if> onclick='sendToggle(this);'>${act.description}</input> 
			</span>
			<#if act.type = "DimmedLamp"><input class="slider" type="range" name="${act.name}" min="0" max="100" step="5" value="${act.parms['level']}" onchange='sendLevelDL(this);'/></#if>
	</div>
		</#list>
</body>
</html>