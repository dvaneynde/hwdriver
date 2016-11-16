package eu.dlvm.domotics.factories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.connectors.DimmerSwitch2Dimmer;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.connectors.Switch2OnOffToggle;
import eu.dlvm.domotics.connectors.Switch2Screen;
import eu.dlvm.domotics.connectors.AlarmEvent2Screen;
import eu.dlvm.domotics.connectors.ThresholdEvent2Screen;
import eu.dlvm.domotics.controllers.NewYear;
import eu.dlvm.domotics.controllers.RepeatOffAtTimer;
import eu.dlvm.domotics.controllers.SunWindController;
import eu.dlvm.domotics.controllers.Timer;
import eu.dlvm.domotics.controllers.TimerDayNight;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.sensors.WindSensor;

class DomoticXmlDefaultHandler extends DefaultHandler2 {

	static Logger log = LoggerFactory.getLogger(DomoticXmlDefaultHandler.class);

	private Block block;
	private Map<String, Block> blocks = new HashMap<>();
	private List<Block> groupBlocks;
	private Map<String, List<Block>> group2Blocks = new HashMap<>();
	private IDomoticContext ctx;
	private String name, desc, ui;
	private String channel;

	public DomoticXmlDefaultHandler(IDomoticContext ctx) {
		super();
		this.ctx = ctx;
	}

	public void startElement(String uri, String localName, String qqName, Attributes atts) throws SAXException {
		if (localName.equals("domotic")) {
			;
		} else if (localName.equals("group")) {
			parseBaseBlock(atts);
			groupBlocks = new LinkedList<>();
			group2Blocks.put(name, groupBlocks);
		} else if (localName.equals("group-ref")) {
			name = atts.getValue("name");
			List<Block> blocksReferenced = group2Blocks.get(name);
			groupBlocks.addAll(blocksReferenced);
		} else if (localName.equals("block")) {
			name = atts.getValue("name");
			Block block2add = blocks.get(name);
			groupBlocks.add(block2add);
		} else if (localName.equals("windSensor")) {
			parseBaseBlockWithChannel(atts);
			int highFreqThreshold = parseIntAttribute("highFreq", atts);
			int lowFreqThreshold = parseIntAttribute("lowFreq", atts);
			int highTimeBeforeAlert = parseIntAttribute("highTimeBeforeAlert", atts);
			int lowTimeToResetAlert = parseIntAttribute("lowTimeToResetAlert", atts);
			block = new WindSensor(name, desc, ui, channel, ctx, highFreqThreshold, lowFreqThreshold, highTimeBeforeAlert, lowTimeToResetAlert);
			blocks.put(block.getName(), block);
		} else if (localName.equals("lightGauge")) {
			parseBaseBlockWithChannel(atts);
			int highThreshold = parseIntAttribute("high", atts);
			int lowThreshold = parseIntAttribute("low", atts);
			int low2highTime = parseIntAttribute("low2highTime", atts);
			int high2lowTime = parseIntAttribute("high2lowTime", atts);
			block = new LightSensor(name, desc, ui, channel, ctx, lowThreshold, highThreshold, low2highTime, high2lowTime);
			blocks.put(block.getName(), block);
		} else if (localName.equals("switch")) {
			parseBaseBlockWithChannel(atts);
			boolean singleClick = parseBoolAttribute("singleClick", true, atts);
			boolean longClick = parseBoolAttribute("longClick", false, atts);
			boolean doubleClick = parseBoolAttribute("doubleClick", false, atts);
			block = new Switch(name, desc, channel, singleClick, longClick, doubleClick, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timer")) {
			parseBaseBlock(atts);
			block = new Timer(name, desc, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timerDayNight")) {
			parseBaseBlock(atts);
			block = new TimerDayNight(name, desc, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("repeatOff")) {
			parseBaseBlock(atts);
			int intervalSec = parseIntAttribute("intervalSec", atts);
			block = new RepeatOffAtTimer(name, desc, ctx, intervalSec);
			blocks.put(block.getName(), block);
		} else if (localName.equals("on")) {
			((Timer) block).setOnTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("off")) {
			((Timer) block).setOffTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("dimmerSwitches")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			String channelDown = (s == null ? new String(name + "Down") : new String(s));
			s = atts.getValue("channelUp");
			String channelUp = (s == null ? new String(name + "Up") : new String(s));
			block = new DimmerSwitch(name, desc, channelDown, channelUp, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("lamp")) {
			parseBaseBlockWithChannel(atts);
			block = new Lamp(name, desc, ui, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("dimmedLamp")) {
			parseBaseBlockWithChannel(atts);
			int fullOn = Integer.parseInt(atts.getValue("fullOnHwOutput"));
			block = new DimmedLamp(name, desc, ui, fullOn, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("fan")) {
			parseBaseBlockWithChannel(atts);
			String lampName = atts.getValue("lamp");
			block = new Fan(name, desc, (Lamp) blocks.get(lampName), channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("screen")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			String channelDown = (s == null ? new String(name + "Down") : new String(s));
			s = atts.getValue("channelUp");
			String channelUp = (s == null ? new String(name + "Up") : new String(s));
			block = new Screen(name, desc, ui, channelDown, channelUp, ctx);
			if (atts.getValue("motor-up-time") != null)
				((Screen) block).setMotorUpPeriod(Integer.parseInt(atts.getValue("motor-up-time")));
			if (atts.getValue("motor-dn-time") != null)
				((Screen) block).setMotorDnPeriod(Integer.parseInt(atts.getValue("motor-dn-time")));
			blocks.put(block.getName(), block);
		} else if (localName.equals("connect")) {
			parseConnect(atts);
		} else if (localName.equals("connect-dimmer")) {
			parseConnectDimmer(atts);
		} else if (localName.equals("connect-screen")) {
			parseConnectScreen(atts);
			/*
			 * } else if (localName.equals("connect-alarm-to-screen")) {
			 * parseConnectAlarm2Screen(atts); } else if
			 * (localName.equals("connect-threshold-to-screen")) {
			 * parseConnectThreshold2Screen(atts);
			 */} else if (localName.equals("newyear")) {
			Date start = DatatypeConverter.parseDateTime(atts.getValue("start")).getTime();
			Date end = DatatypeConverter.parseDateTime(atts.getValue("end")).getTime();
			NewYear ny = new NewYearBuilder().build(blocks, start.getTime(), end.getTime(), ctx);
			blocks.put(ny.getName(), ny);
		} else if (localName.equals("screen-controller")) {
			parseScreenController(atts);
		} else {
			throw new RuntimeException("Block " + qqName + " not supported.");
		}
	}

	public void endElement(String uri, String localName, String qqName) throws SAXException {
	}

	private void parseConnect(Attributes atts) {
		String srcName = atts.getValue("src");
		String srcEventName = atts.getValue("src-event");
		String targetName = atts.getValue("target");
		String eventName = atts.getValue("event");

		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");

		Block src = blocks.get(srcName);
		if (src instanceof Switch) {
			handleConnectSwitch2OOT((Switch) src, srcEventName, targetName, eventName);
		} else if (src instanceof Timer) {
			handleConnectTimer((Timer) src, targetName);
		} else {
			throw new ConfigurationException("Unsupported type for connect, source=" + srcName + ", target=" + targetName + ", source event=" + srcEventName);
		}
	}

	private void parseScreenController(Attributes atts) {
		parseBaseBlock(atts);
		SunWindController enabler = new SunWindController(name, desc, ui, ctx);

		String srcAlarmName = atts.getValue("alarmSrc");
		WindSensor srcAlarm = (WindSensor) blocks.get(srcAlarmName);
		srcAlarm.registerListener(enabler);

		String srcLightName = atts.getValue("lightSrc");
		LightSensor lightSensor = (LightSensor) blocks.get(srcLightName);
		lightSensor.registerListener(enabler);

		List<Block> targetBlocks;
		String screenName = atts.getValue("screen");
		Block t = blocks.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}
		try {
			for (Block target : targetBlocks) {
				enabler.registerListener((Screen) target);
			}
		} catch (ClassCastException e) {
			throw new ConfigurationException("Unsupported type for screen-controller, name=" + name + ", screen=" + screenName + ".");
		}
		blocks.put(enabler.getName(), enabler);
	}

	private void handleConnectSwitch2OOT(Switch swtch, String srcEventName, String targetName, String eventName) {
		List<Block> targetBlocks;
		Block t = blocks.get(targetName);
		if (t == null)
			targetBlocks = group2Blocks.get(targetName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}

		String s2ootName = swtch.getName() + "_to_" + targetName;
		Switch2OnOffToggle s2oot = (Switch2OnOffToggle) blocks.get(s2ootName);
		if (s2oot == null) {
			s2oot = new Switch2OnOffToggle(s2ootName, (desc == null ? "Switch " + swtch.getName() + " connected to " + targetName : desc), ui);
//			if (ui != null && ui.length() > 0) {
//				// TODO algemenere oplossing...
//				ctx.addUiCapableBlock(s2oot);
//			}
			blocks.put(s2ootName, s2oot);
		}

		s2oot.map(ISwitchListener.ClickType.valueOf(srcEventName.toUpperCase()), IOnOffToggleCapable.ActionType.valueOf(eventName.toUpperCase()));
		swtch.registerListener(s2oot);
		for (Block target : targetBlocks) {
			s2oot.registerListener((IOnOffToggleCapable) target);
		}
	}

	private void handleConnectTimer(Timer timer, String targetName) {
		List<Block> targetBlocks;
		Block t = blocks.get(targetName);
		if (t == null)
			targetBlocks = group2Blocks.get(targetName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}

		for (Block target : targetBlocks)
			timer.register((IOnOffToggleCapable) target);
	}

	private void parseConnectDimmer(Attributes atts) {
		parseBaseBlock(atts);

		String switchName = atts.getValue("dimmerSwitch");
		String lampName = atts.getValue("dimmedLamp");
		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("Need a DimmerSwitches named " + switchName + ", which I did not encounter in configuration file.");
		}
		if (!blocks.containsKey(lampName)) {
			throw new ConfigurationException("Need a Lamp named " + lampName + ", which I did not encounter in configuration file.");
		}
		DimmerSwitch ds = (DimmerSwitch) blocks.get(switchName);
		DimmedLamp dl = (DimmedLamp) blocks.get(lampName);
		String ds2dName = switchName + "_to_" + lampName;
		DimmerSwitch2Dimmer ds2d = new DimmerSwitch2Dimmer(ds2dName, ds2dName);
		blocks.put(ds2d.getName(), ds2d);
		ds.registerListener(ds2d);
		ds2d.setLamp(dl);
	}

	private void parseConnectScreen(Attributes atts) {
		String switchDownName = atts.getValue("switchDown");
		String switchUpName = atts.getValue("switchUp");
		String screenName = atts.getValue("screen");
		String clickName = atts.getValue("click");

		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");

		List<Block> targetBlocks;
		Block t = blocks.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}

		String csName = "Switch2Screen_" + screenName;
		Switch down = (Switch) blocks.get(switchDownName);
		Switch up = (Switch) blocks.get(switchUpName);
		ISwitchListener.ClickType click = ISwitchListener.ClickType.valueOf(clickName.toUpperCase());
		Switch2Screen s2s = new Switch2Screen(csName, desc, ui, down, up, click);
//		if (ui != null && ui.length() > 0) {
//			// TODO algemenere oplossing...
//			ctx.addUiCapableBlock(s2s);
//		}
		blocks.put(s2s.getName(), s2s);

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
		Block t = blocks.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}

		try {
			WindSensor ws = (WindSensor) blocks.get(wsName);
			AlarmEvent2Screen ws2s = new AlarmEvent2Screen(wsName + "_2_" + screenName, desc);
			ws.registerListener(ws2s);
			for (Block target : targetBlocks) {
				ws2s.registerListener((Screen) target);
			}
		} catch (ClassCastException e) {
			throw new ConfigurationException("Unsupported type for connect, source=" + wsName + ", screen=" + screenName + ".");
		}
	}

	private void parseConnectThreshold2Screen(Attributes atts) {
		String srcName = atts.getValue("source");
		String screenName = atts.getValue("screen");

		// Not null in case of UiCapable mapper
		desc = atts.getValue("desc");
		ui = atts.getValue("ui");

		List<Block> targetBlocks;
		Block t = blocks.get(screenName);
		if (t == null)
			targetBlocks = group2Blocks.get(screenName);
		else {
			targetBlocks = new ArrayList<>(1);
			targetBlocks.add(t);
		}

		try {
			LightSensor ls = (LightSensor) blocks.get(srcName);
			ThresholdEvent2Screen ws2s = new ThresholdEvent2Screen(srcName + "_2_" + screenName, desc);
			ls.registerListener(ws2s);
			for (Block target : targetBlocks) {
				ws2s.registerListener((Screen) target);
			}
		} catch (ClassCastException e) {
			throw new ConfigurationException("Unsupported type for connect, source=" + srcName + ", screen=" + screenName + ".");
		}
	}

	private void parseBaseBlockWithChannel(Attributes atts) {
		parseBaseBlock(atts);
		String s = atts.getValue("channel");
		if (s == null)
			s = name;
		channel = new String(s);
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
		// if (atts.getValue(attName) == null)
		// return 0;
		// else
		return Integer.parseInt(atts.getValue(attName));
	}

}
