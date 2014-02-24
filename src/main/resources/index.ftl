<!DOCTYPE html>
<html>
<head>
	<meta charset="US-ASCII">
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<!--
	<meta name="viewport" content="width=320, maximum-scale=1.0" />
	-->
	<title>Domotica</title>
<!--	
	<link rel="stylesheet" href="css/Yellow-C.css" />
-->
	<link rel="stylesheet" href="css/DomoticTheme.css" />
	<link rel="stylesheet" href="css/jquery.mobile.icons.min.css" />
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.0/jquery.mobile.structure-1.4.0.min.css" />
	<script src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
	<script src="http://code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0.min.js"></script>

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
	<div data-role="page" id="pageone" data-theme="a">
		<div data-role="header" >
			<h1>${model.title}</h1>
		</div>

		<div data-role="content" class="ui-content">
			<div data-role="collapsible-set" data-theme="a" data-content-theme="a">

 			<div data-role="collapsible" data-collapsed="true" data-theme="a">
	    	<h4 style="background: rgba(204,244,204,0.9);">Quickies...</h4>
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
<!-- TODO ui-group-theme-[a-z] zet theme voor een collapsible -->
			<#list model.groupNames as group>
			<#if model.groupOn[group]>
			<div id="${group}" data-role="collapsible" data-theme="c">
			<#else>
			<div id="${group}" data-role="collapsible" data-theme="a">
			</#if>
			<!-- TODO lichtgeel als er minstens 1 licht aan is; misschien gradaties van geel om aantal lichten (absoluut) aan te geven? -->
			<h4>${group}</h4>
				<div data-theme="a">
				<#list model.groupname2infos[group] as act>
					<label><!-- TODO Flip Switch ipv checkbox -->
						<input type="checkbox" id="${act.name}" name="${act.name}" value="${act.name}" <#if act.on>checked</#if> onclick='sendToggle(this);'>${act.description}</input> 
					</label>
					<#if act.type = "DimmedLamp">
						<#if act.on><#assign disableslider="false"><#else><#assign disableslider="true"></#if>
						<input type="range" id="${act.name}_lvl" name="${act.name}" min="0" max="100" step="5" value="${act.level}" data-disabled="${disableslider}" onchange='sendLevelDL(this);'/>
					</#if>
				</#list></div>
				</div>
			</#list>

		</div>
<!--
		<div data-role="footer" data-theme="b" data-position="fixed" >
		<div class="ui-bar">
	        <select name="flip-7" id="flip-7" data-role="slider" >
	            <option value="off">manual</option>
	            <option value="on">auto</option>
	        </select>
			<a href="#" class="ui-btn-right ui-btn ui-btn-inline ui-mini ui-corner-all ui-btn-icon-right ui-icon-refresh" onclick="refreshActuators();">Refresh</a>
			<span class="ui-title"></span> 
			-->
		</div>
		</div>
	</div>
</body>
</html>