package eu.dlvm.domotics.factories;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.controllers.RepeatOffAtTimer;
import eu.dlvm.domotics.controllers.SunWindController;
import eu.dlvm.domotics.controllers.Timer;
import eu.dlvm.domotics.controllers.TimerDayNight;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.sensors.WindSensor;

class DomoticXmlDefaultHandler extends DefaultHandler2 {

	static Logger logger = LoggerFactory.getLogger(DomoticXmlDefaultHandler.class);

	private IDomoticContext ctx;
	private Block currentBlock;
	private String name, desc, ui;
	private String channel, channelDown, channelUp;
	private Map<String, Block> blocksSoFar = new HashMap<>();

	private static class ActionEvent {
		public Block srcBlock;
		public EventType srcEvent;;
	}

	public DomoticXmlDefaultHandler(IDomoticContext ctx) {
		super();
		this.ctx = ctx;
	}

	public void startElement(String uri, String localName, String qqName, Attributes atts) throws SAXException {
		if (localName.equals("domotic")) {
			;

			// ===== Inner elements

		} else if (localName.equals("on")) {
			if (currentBlock instanceof Timer) {
				((Timer) currentBlock).setOnTime(Integer.parseInt(atts.getValue("hour")),
						Integer.parseInt(atts.getValue("minute")));
			} else if (currentBlock instanceof Actuator) {
				connectEvent2Action(atts, EventType.ON);
			} else
				throw new RuntimeException("Bug.");

		} else if (localName.equals("off")) {
			if (currentBlock instanceof Timer) {
				((Timer) currentBlock).setOffTime(Integer.parseInt(atts.getValue("hour")),
						Integer.parseInt(atts.getValue("minute")));
			} else if (currentBlock instanceof Actuator) {
				connectEvent2Action(atts, EventType.OFF);
			} else
				throw new RuntimeException("Bug.");

		} else if (localName.equals("toggle")) {
			connectEvent2Action(atts, EventType.TOGGLE);

		} else if (localName.equals("delayedOnOff")) {
			IEventListener target = (IEventListener) currentBlock;
			Block source = blocksSoFar.get(atts.getValue("src"));
			source.registerListener(new Connector(EventType.ON, target, EventType.DELAY_ON,
					"delayOn_" + source.getName() + "_to_" + ((Block) target).getName()));
			source.registerListener(new Connector(EventType.OFF, target, EventType.DELAY_OFF,
					"delayOff_" + source.getName() + "_to_" + ((Block) target).getName()));
		} else if (localName.equals("wind")) {
		} else if (localName.equals("sun")) {
		} else if (localName.equals("upDown")) {
			Sensor srcUp = (Sensor) blocksSoFar.get(atts.getValue("srcUp"));
			Sensor srcDown = (Sensor) blocksSoFar.get(atts.getValue("srcDown"));
			String eventName = atts.getValue("event");
			srcUp.registerListener(
					new Connector(EventType.fromAlias(eventName), (Screen) currentBlock, EventType.TOGGLE_UP, "TODO"));
			srcDown.registerListener(
					new Connector(EventType.fromAlias(eventName), (Screen) currentBlock, EventType.TOGGLE_DOWN, "TODO"));
			// ===== Sensors 

		} else if (localName.equals("switch")) {
			parseBaseBlockWithChannel(atts);
			boolean singleClick = parseBoolAttribute("singleClick", true, atts);
			boolean longClick = parseBoolAttribute("longClick", false, atts);
			boolean doubleClick = parseBoolAttribute("doubleClick", false, atts);
			currentBlock = new Switch(name, desc, channel, singleClick, longClick, doubleClick, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("dimmerSwitches")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			String channelDown = (s == null ? new String(name + "Down") : new String(s));
			s = atts.getValue("channelUp");
			String channelUp = (s == null ? new String(name + "Up") : new String(s));
			currentBlock = new DimmerSwitch(name, desc, channelDown, channelUp, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("windSensor")) {
			parseBaseBlockWithChannel(atts);
			int highFreqThreshold = parseIntAttribute("highFreq", atts);
			int lowFreqThreshold = parseIntAttribute("lowFreq", atts);
			int highTimeBeforeAlert = parseIntAttribute("highTimeBeforeAlert", atts);
			int lowTimeToResetAlert = parseIntAttribute("lowTimeToResetAlert", atts);
			currentBlock = new WindSensor(name, desc, ui, channel, ctx, highFreqThreshold, lowFreqThreshold, highTimeBeforeAlert,
					lowTimeToResetAlert);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("lightGauge")) {
			parseBaseBlockWithChannel(atts);
			int highThreshold = parseIntAttribute("high", atts);
			int lowThreshold = parseIntAttribute("low", atts);
			int low2highTime = parseIntAttribute("low2highTime", atts);
			int high2lowTime = parseIntAttribute("high2lowTime", atts);
			currentBlock = new LightSensor(name, desc, ui, channel, ctx, lowThreshold, highThreshold, low2highTime, high2lowTime);
			blocksSoFar.put(currentBlock.getName(), currentBlock);
			//		} else if (localName.equals("connect")) {
			//			parseConnect(atts);
			//		} else if (localName.equals("connect-dimmer")) {
			//			parseConnectDimmer(atts);
			//		} else if (localName.equals("connect-screen")) {
			//			parseConnectScreen(atts);
			//			/*
			//			 * } else if (localName.equals("connect-alarm-to-screen")) {
			//			 * parseConnectAlarm2Screen(atts); } else if
			//			 * (localName.equals("connect-threshold-to-screen")) {
			//			 * parseConnectThreshold2Screen(atts);
			//			 */} else if (localName.equals("newyear")) {
			//			Date start = DatatypeConverter.parseDateTime(atts.getValue("start")).getTime();
			//			Date end = DatatypeConverter.parseDateTime(atts.getValue("end")).getTime();
			//			NewYear ny = new NewYearBuilder().build(blocksSoFar, start.getTime(), end.getTime(), ctx);
			//			blocksSoFar.put(ny.getName(), ny);

			// ===== Controllers 

		} else if (localName.equals("timer")) {
			parseBaseBlock(atts);
			currentBlock = new Timer(name, desc, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("timerDayNight")) {
			parseBaseBlock(atts);
			currentBlock = new TimerDayNight(name, desc, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("repeatOff")) {
			parseBaseBlock(atts);
			int intervalSec = parseIntAttribute("intervalSec", atts);
			currentBlock = new RepeatOffAtTimer(name, desc, ctx, intervalSec);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("sunWindController")) {
			parseBaseBlock(atts);
			currentBlock = new SunWindController(name, desc, ui, ctx);

			// ===== Actuators

		} else if (localName.equals("lamp")) {
			parseBaseBlockWithChannel(atts);
			currentBlock = new Lamp(name, desc, ui, channel, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);
		} else if (localName.equals("toggle")) {
			// actuators

		} else if (localName.equals("dimmedLamp")) {
			parseBaseBlockWithChannel(atts);
			int fullOn = Integer.parseInt(atts.getValue("fullOnHwOutput"));
			currentBlock = new DimmedLamp(name, desc, ui, fullOn, channel, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("fan")) {
			parseBaseBlockWithChannel(atts);
			currentBlock = new Fan(name, desc, channel, ctx);
			blocksSoFar.put(currentBlock.getName(), currentBlock);

		} else if (localName.equals("screen")) {
			if (currentBlock instanceof SunWindController) {
				SunWindController swc = (SunWindController) currentBlock;
				Screen screen = (Screen) blocksSoFar.get(atts.getValue("name"));
				swc.registerListener(screen);
			} else {
				parseBaseBlockWithUpDownChannel(atts);
				currentBlock = new Screen(name, desc, ui, channelDown, channelUp, ctx);
				if (atts.getValue("motor-up-time") != null)
					((Screen) currentBlock).setMotorUpPeriod(Integer.parseInt(atts.getValue("motor-up-time")));
				if (atts.getValue("motor-dn-time") != null)
					((Screen) currentBlock).setMotorDnPeriod(Integer.parseInt(atts.getValue("motor-dn-time")));
				blocksSoFar.put(currentBlock.getName(), currentBlock);
			}
		} else {
			throw new RuntimeException("Element '" + qqName + "' not supported.");
		}
	}

	private void connectEvent2Action(Attributes atts, EventType targetEventType) {
		ActionEvent ae = parseActionEvent(atts);
		Connector c = new Connector(ae.srcEvent, (Actuator) currentBlock, targetEventType, "(" + ae.srcBlock.getName() + ','
				+ ae.srcEvent + ")_TO_(" + currentBlock.getName() + "," + targetEventType.name() + ")");

		ae.srcBlock.registerListener(c);
	}

	public void endElement(String uri, String localName, String qqName) throws SAXException {
	}

	private ActionEvent parseActionEvent(Attributes atts) {
		ActionEvent ae = new ActionEvent();
		String srcName = atts.getValue("src");
		String eventAlias = atts.getValue("event");

		Block src = blocksSoFar.get(srcName);
		if (src != null) {
			ae.srcBlock = src;
			ae.srcEvent = EventType.fromAlias(eventAlias);
		} else {
			throw new ConfigurationException("Could not find srcBlock =" + srcName + ". Check config of " + currentBlock.getName());
		}
		return ae;
	}

	/*
	private void parseScreenController(Attributes atts) {
		parseBaseBlock(atts);
		SunWindController sunWindController = new SunWindController(name, desc, ui, ctx);
	
		String srcAlarmName = atts.getValue("alarmSrc");
		WindSensor srcAlarm = (WindSensor) blocksSoFar.get(srcAlarmName);
		srcAlarm.registerListener(sunWindController);
	
		String srcLightName = atts.getValue("lightSrc");
		LightSensor lightSensor = (LightSensor) blocksSoFar.get(srcLightName);
		lightSensor.registerListener(sunWindController);
	
		List<Block> targetBlocks;
		String screenName = atts.getValue("screen");
		Block t = blocksSoFar.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
		try {
			for (Block target : targetBlocks) {
				sunWindController.registerListener((Screen) target);
			}
		} catch (ClassCastException e) {
			throw new ConfigurationException(
					"Unsupported type for screen-controller, name=" + name + ", screen=" + screenName + ".");
		}
		blocksSoFar.put(sunWindController.getName(), sunWindController);
	}
	*/

	/*
	private void handleConnectSwitch2OOT(Switch swtch, String srcEventName, String targetName, String eventName) {
		List<Block> targetBlocks;
		Block t = blocksSoFar.get(targetName);
		if (t == null)
			targetBlocks = group2Blocks.get(targetName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
	
		String s2ootName = swtch.getName() + "_to_" + targetName;
		Switch2OnOffToggle s2oot = (Switch2OnOffToggle) blocksSoFar.get(s2ootName);
		if (s2oot == null) {
			s2oot = new Switch2OnOffToggle(s2ootName,
					(desc == null ? "Switch " + swtch.getName() + " connected to " + targetName : desc), ui);
			//			if (ui != null && ui.length() > 0) {
			//				// TODO algemenere oplossing...
			//				ctx.addUiCapableBlock(s2oot);
			//			}
			blocksSoFar.put(s2ootName, s2oot);
		}
	
		s2oot.map(ISwitchListener.ClickType.valueOf(srcEventName.toUpperCase()),
				IOnOffToggleCapable.ActionType.valueOf(eventName.toUpperCase()));
		swtch.registerListener(s2oot);
		for (Block target : targetBlocks) {
			s2oot.registerListener((IOnOffToggleCapable) target);
		}
	}
	*/

	/*
	private void handleConnectTimer(Timer timer, String targetName) {
		List<Block> targetBlocks;
		Block t = blocksSoFar.get(targetName);
		if (t == null)
			targetBlocks = group2Blocks.get(targetName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
	
		for (Block target : targetBlocks)
			timer.register((IOnOffToggleCapable) target);
	}
	*/

	/*
	private void parseConnectDimmer(Attributes atts) {
		parseBaseBlock(atts);
	
		String switchName = atts.getValue("dimmerSwitch");
		String lampName = atts.getValue("dimmedLamp");
		if (!blocksSoFar.containsKey(switchName)) {
			throw new ConfigurationException(
					"Need a DimmerSwitches named " + switchName + ", which I did not encounter in configuration file.");
		}
		if (!blocksSoFar.containsKey(lampName)) {
			throw new ConfigurationException(
					"Need a Lamp named " + lampName + ", which I did not encounter in configuration file.");
		}
		DimmerSwitch ds = (DimmerSwitch) blocksSoFar.get(switchName);
		DimmedLamp dl = (DimmedLamp) blocksSoFar.get(lampName);
		String ds2dName = switchName + "_to_" + lampName;
		DimmerSwitch2Dimmer ds2d = new DimmerSwitch2Dimmer(ds2dName, ds2dName);
		blocksSoFar.put(ds2d.getName(), ds2d);
		ds.registerListener(ds2d);
		ds2d.setLamp(dl);
	}
	*/

	/*
	private void parseConnectScreen(Attributes atts) {
		String switchDownName = atts.getValue("switchDown");
		String switchUpName = atts.getValue("switchUp");
		String screenName = atts.getValue("screen");
		String clickName = atts.getValue("click");
	
		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");
	
		List<Block> targetBlocks;
		Block t = blocksSoFar.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
	
		String csName = "Switch2Screen_" + screenName;
		Switch down = (Switch) blocksSoFar.get(switchDownName);
		Switch up = (Switch) blocksSoFar.get(switchUpName);
		ISwitchListener.ClickType click = ISwitchListener.ClickType.valueOf(clickName.toUpperCase());
		Switch2Screen s2s = new Switch2Screen(csName, desc, ui, down, up, click);
		//		if (ui != null && ui.length() > 0) {
		//			// TODO algemenere oplossing...
		//			ctx.addUiCapableBlock(s2s);
		//		}
		blocksSoFar.put(s2s.getName(), s2s);
	
		for (Block target : targetBlocks) {
			s2s.registerListener((Screen) target);
		}
	}
	
	private void parseConnectAlarm2Screen(Attributes atts) {
		String wsName = atts.getValue("source");
		String screenName = atts.getValue("screen");
	
		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");
	
		List<Block> targetBlocks;
		Block t = blocksSoFar.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
	
		try {
			WindSensor ws = (WindSensor) blocksSoFar.get(wsName);
			/*
			 * FIXME AlarmEvent2Screen ws2s = new AlarmEvent2Screen(wsName +
			 * "_2_" + screenName, desc); ws.registerListener(ws2s); for (Block
			 * target : targetBlocks) { ws2s.registerListener((Screen) target);
			 * }
			 *
		} catch (ClassCastException e) {
			throw new ConfigurationException(
					"Unsupported type for connect, source=" + wsName + ", screen=" + screenName + ".");
		}
	}
	
	private void parseConnectThreshold2Screen(Attributes atts) {
		String srcName = atts.getValue("source");
		String screenName = atts.getValue("screen");
	
		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");
	
		List<Block> targetBlocks;
		Block t = blocksSoFar.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
	
		try {
			LightSensor ls = (LightSensor) blocksSoFar.get(srcName);
			/*
			 * FIXME ThresholdEvent2Screen ws2s = new
			 * ThresholdEvent2Screen(srcName + "_2_" + screenName, desc);
			 * ls.registerListener(ws2s); for (Block target : targetBlocks) {
			 * ws2s.registerListener((Screen) target); }
			 *
		} catch (ClassCastException e) {
			throw new ConfigurationException(
					"Unsupported type for connect, source=" + srcName + ", screen=" + screenName + ".");
		}
	}
	 */

	private void parseBaseBlockWithChannel(Attributes atts) {
		parseBaseBlock(atts);
		String s = atts.getValue("channel");
		if (s == null)
			s = name;
		channel = new String(s);
	}

	private void parseBaseBlockWithUpDownChannel(Attributes atts) {
		parseBaseBlock(atts);
		String s = atts.getValue("channelDown");
		channelDown = (s == null ? new String(name + "Down") : new String(s));
		s = atts.getValue("channelUp");
		channelUp = (s == null ? new String(name + "Up") : new String(s));
	}

	private void parseBaseBlock(Attributes atts) {
		name = atts.getValue("name");
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");
	}

	private boolean parseBoolAttribute(String attName, boolean defaultVal, Attributes atts) {
		if (atts.getValue(attName) == null)
			return defaultVal;
		else
			return Boolean.parseBoolean(atts.getValue(attName));
	}

	private int parseIntAttribute(String attName, Attributes atts) {
		if (atts.getValue(attName) == null)
			return 0;
		else
			return Integer.parseInt(atts.getValue(attName));
	}

}
