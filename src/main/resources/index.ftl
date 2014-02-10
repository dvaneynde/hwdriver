<!DOCTYPE html>
<html>
<head>
	<meta charset="US-ASCII">
	<meta name="viewport" content="width=320, maximum-scale=1.0" />
	<title>Domotica</title>
	
	<!--
	<script type="text/javascript" src="js/jquery-2.0.3.js"></script>
	-->
	
	<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
	<script	src="http://code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0.min.js"></script>	
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0.min.css" />
	<script type="text/javascript" src="js/domo.js"></script>
</head>
<!--
TOOD:
1. elke 5 seconden een refresh
2. bij click op checkbox, toggle doorsturen, en refresh
   function sendToggle(name) {
   }
-->
<body>
	<div data-role="page" id="pageone">
		<div data-role="header">
			<h1>${model.title}</h1>
		</div>

		<div data-role="content">
			<div data-role="collapsible-set" data-theme="a" data-content-theme="a">

 			<div data-role="collapsible" data-collapsed="true">
	    	<h4>Quickies...</h4>
				<div data-role="controlgroup" data-type="horizontal"><!-- ui-mini ui-btn-inline -->
					<button id="veranda" class="ui-btn ui-corner-all"
						onclick="sendQuickie('eten');">Veranda</button>
					<button id="tv" class="ui-btn ui-corner-all"
						onclick="sendQuickie('tv');">TV</button>
					<button id="eco" class="ui-btn ui-corner-all"
						onclick="sendQuickie('eco');">Eco</button>
					<button id="fel" class="ui-btn ui-corner-all"
						onclick="sendQuickie('fel');">FEL</button>
				</div>
			</div>

			<#list model.groupNames as group>
			<div data-role="collapsible" >
			<!-- TODO lichtgeel als er minstens 1 licht aan is; misschien gradaties van geel om aantal lichten (absoluut) aan te geven? -->
			<h4>${group}</h4>
			<#list model.groupname2infos[group] as act>
				<label><!-- TODO Flip Switch ipv checkbox -->
					<input type="checkbox" name="${act.name}" value="${act.name}" <#if act.parms["on"] = "1">checked</#if> onclick='sendToggle(this);'>${act.description}</input> 
				</label>
				<#if act.type = "DimmedLamp"><input type="range" name="${act.name}" min="0" max="100" step="5" value="${act.parms['level']}" onchange='sendLevelDL(this);'/></#if>
			</#list>
			</div>
			</#list>

		</div>

		<div data-role="footer">
			<h1>TODO </h1>
		</div>
	</div>
</body>
</html>