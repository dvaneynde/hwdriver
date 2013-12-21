package eu.dlvm.domotics.factories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.actuators.NewYear;
import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.actuators.newyear.OnOff;
import eu.dlvm.domotics.actuators.newyear.RandomOnOff;
import eu.dlvm.domotics.actuators.newyear.Sinus;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.mappers.DimmerSwitch2Dimmer;
import eu.dlvm.domotics.mappers.IOnOffToggleListener;
import eu.dlvm.domotics.mappers.Switch2OnOffToggle;
import eu.dlvm.domotics.mappers.Switch2Screen;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.sensors.Timer;
import eu.dlvm.domotics.sensors.TimerDayNight;
import eu.dlvm.iohardware.LogCh;

class DomoticXmlDefaultHandler extends DefaultHandler2 {

	static Logger log = Logger.getLogger(DomoticXmlDefaultHandler.class);

	private Block block;
	private Map<String, Block> blocks = new HashMap<>();
	private List<Block> groupBlocks;
	private Map<String, List<Block>> group2Blocks = new HashMap<>();
	private IHardwareAccess ctx;
	private String name, desc;
	private LogCh channel;

	public DomoticXmlDefaultHandler(IHardwareAccess ctx) {
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
		} else if (localName.equals("switch")) {
			parseBaseBlockWithChannel(atts);
			boolean singleClick = parseBoolAttribute("singleClick", true, atts);
			boolean longClick = parseBoolAttribute("longClick", false, atts);
			boolean doubleClick = parseBoolAttribute("doubleClick", false, atts);
			block = new Switch(name, desc, channel, singleClick, longClick, doubleClick, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timer")) {
			parseBaseBlock(atts);
			block = new Timer(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timerDayNight")) {
			parseBaseBlock(atts);
			block = new TimerDayNight(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("on")) {
			((Timer) block).setOnTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("off")) {
			((Timer) block).setOffTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("dimmerSwitches")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			LogCh channelDown = (s == null ? new LogCh(name + "Down") : new LogCh(s));
			s = atts.getValue("channelUp");
			LogCh channelUp = (s == null ? new LogCh(name + "Up") : new LogCh(s));
			block = new DimmerSwitch(name, desc, channelDown, channelUp, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("lamp")) {
			parseBaseBlockWithChannel(atts);
			block = new Lamp(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("dimmedLamp")) {
			parseBaseBlockWithChannel(atts);
			int fullOn = Integer.parseInt(atts.getValue("fullOnHwOutput"));
			block = new DimmedLamp(name, desc, fullOn, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("fan")) {
			parseBaseBlockWithChannel(atts);
			String lampName = atts.getValue("lamp");
			block = new Fan(name, desc, (Lamp) blocks.get(lampName), channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("screen")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			LogCh channelDown = (s == null ? new LogCh(name + "Down") : new LogCh(s));
			s = atts.getValue("channelUp");
			LogCh channelUp = (s == null ? new LogCh(name + "Up") : new LogCh(s));
			block = new Screen(name, desc, channelDown, channelUp, ctx);
			if (atts.getValue("motor-on-time") != null) {
				((Screen) block).setMotorOnPeriod(Integer.parseInt(atts.getValue("motor-on-time")));
			}
			blocks.put(block.getName(), block);
		} else if (localName.equals("connect")) {
			parseConnect(atts);
		} else if (localName.equals("connect-dimmer")) {
			parseConnectDimmer(atts);
		} else if (localName.equals("connect-screen")) {
			parseConnectScreen(atts);
		} else if (localName.equals("newyear")) {
			Date start = DatatypeConverter.parseDateTime(atts.getValue("start")).getTime();
			Date end = DatatypeConverter.parseDateTime(atts.getValue("end")).getTime();
			NewYear ny = new NewYearBuilder().build(blocks, start.getTime(), end.getTime(), ctx);
			blocks.put(ny.getName(), ny);
/*
		} else if (localName.equals("onoff")) {
			NewYear ny = (NewYear) blocks.get("newyear");
			String lampName = atts.getValue("lamp");
			Lamp lamp = (Lamp) blocks.get(lampName);
			OnOff oo = new OnOff(lamp);
			// TODO tijden in xml; ook start/stop moet hier in doorgaan?
			oo.add(oo.new Entry(0, false));
			oo.add(oo.new Entry((ny.getEndTimeMs() - ny.getStartTimeMs()) / 1000, true));
			ny.addGadget(oo);
		} else if (localName.equals("random")) {
			NewYear ny = (NewYear) blocks.get("newyear");
			String lampName = atts.getValue("lamp");
			Lamp lamp = (Lamp) blocks.get(lampName);
			int minTimeOnOffMs = Integer.parseInt(atts.getValue("min-on-ms"));
			int randomMultiplierMs = Integer.parseInt(atts.getValue("rand-mult-ms"));
			RandomOnOff roo = new RandomOnOff(lamp, minTimeOnOffMs, randomMultiplierMs);
			ny.addGadget(roo);
		} else if (localName.equals("sine")) {
			NewYear ny = (NewYear) blocks.get("newyear");
			String dimName = atts.getValue("lamp");
			DimmedLamp dl = (DimmedLamp) blocks.get(dimName);
			int cycleTimeMs = Integer.parseInt(atts.getValue("cycle-ms"));
			int cycleStartRd = Integer.parseInt(atts.getValue("cycle-start-deg"));
			Sinus s = new Sinus(dl, cycleTimeMs, cycleStartRd);
			ny.addGadget(s);
*/
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

		Block src = blocks.get(srcName);
		if (src instanceof Switch) {
			handleConnectSwitch2OOT((Switch) src, srcEventName, targetName, eventName);
		} else if (src instanceof Timer) {
			handleConnectTimer((Timer) src, targetName);
		} else {
			throw new ConfigurationException("Unsupported type for connect, source=" + srcName + ", target=" + targetName + ", source event=" + srcEventName);
		}
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
			s2oot = new Switch2OnOffToggle(s2ootName, "Switch " + swtch.getName() + " connected to " + targetName);
			blocks.put(s2ootName, s2oot);
		}

		s2oot.map(ISwitchListener.ClickType.valueOf(srcEventName.toUpperCase()), IOnOffToggleListener.ActionType.valueOf(eventName.toUpperCase()));
		swtch.registerListener(s2oot);
		for (Block target : targetBlocks) {
			s2oot.registerListener((IOnOffToggleListener) target);
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
			timer.register((IOnOffToggleListener) target);
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
		Switch2Screen s2s = new Switch2Screen(csName, csName, down, up, click);
		blocks.put(s2s.getName(), s2s);

		for (Block target : targetBlocks) {
			s2s.registerListener((Screen) target);
		}
	}

	private void parseBaseBlockWithChannel(Attributes atts) {
		parseBaseBlock(atts);
		String s = atts.getValue("channel");
		if (s == null)
			s = name;
		channel = new LogCh(s);
	}

	private void parseBaseBlock(Attributes atts) {
		name = atts.getValue("name");
		desc = atts.getValue("desc");
	}

	private boolean parseBoolAttribute(String attName, boolean defaultVal, Attributes atts) {
		if (atts.getValue(attName) == null)
			return defaultVal;
		else
			return Boolean.parseBoolean(atts.getValue(attName));
	}

}
