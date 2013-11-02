package eu.dlvm.domotica.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.IHardwareAccess;
import eu.dlvm.domotica.blocks.concrete.DimmedLamp;
import eu.dlvm.domotica.blocks.concrete.DimmerSwitches;
import eu.dlvm.domotica.blocks.concrete.Fan;
import eu.dlvm.domotica.blocks.concrete.IOnOffToggleListener;
import eu.dlvm.domotica.blocks.concrete.ISwitchListener;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Screen;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.Switch2OnOffToggle;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardDimmers;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardScreens;
import eu.dlvm.domotica.blocks.concrete.Timer;
import eu.dlvm.domotica.blocks.concrete.TimerDayNight;
import eu.dlvm.iohardware.LogCh;

class DomoticXmlDefaultHandler extends DefaultHandler2 {

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
		} else if (localName.equals("connect")) {
			handleConnect(atts);
		} else if (localName.equals("switch")) {
			parseBaseBlockWithChannel(atts);
			// TODO default attribute values, werkt dat niet?

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
			block = new DimmerSwitches(name, desc, channelDown, channelUp, ctx);
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
		} else if (localName.equals("switchboardDimmer")) {
			parseBaseBlock(atts);
			block = new SwitchBoardDimmers(name, desc);
			blocks.put(block.getName(), block);
		} else if (localName.equals("switchboardScreens")) {
			parseBaseBlock(atts);
			block = new SwitchBoardScreens(name, desc);
			blocks.put(block.getName(), block);
		} else if (localName.equals("connection")) {
			// TODO introduce interface ICapabilitySwitchConnectable to avoid
			// below if/else, also update parseConnection() and rename
			// parseSwitchConnection
			if (block instanceof SwitchBoardDimmers) {
				parseConnection((SwitchBoardDimmers) block, atts);
			} else if (block instanceof SwitchBoardScreens) {
				parseConnection((SwitchBoardScreens) block, atts);
			} else {
				throw new ConfigurationException("Connection found in unknown Block.");
			}
		} else if (localName.equals("allUpDownSwitches")) {
			parseAllUpDownSwitch(block, atts);
		} else {
			throw new RuntimeException("Block " + qqName + " not supported.");
		}
	}

	private void handleConnect(Attributes atts) {
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
			 s2oot.registerListener((IOnOffToggleListener)target);
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

		for (Block target:targetBlocks)
			timer.register((IOnOffToggleListener)target);
	}

	public void endElement(String uri, String localName, String qqName) throws SAXException {
	}

	private void parseAllUpDownSwitch(Block sb, Attributes atts) {
		String allDownSwitchName = atts.getValue("allDownSwitch");
		String allUpSwitchName = atts.getValue("allUpSwitch");

		if (!blocks.containsKey(allDownSwitchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName() + " needs a Switch named " + allDownSwitchName + ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(allUpSwitchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName() + " needs a Switch named " + allUpSwitchName + ", which it did not encounter in configuration file.");
		}

		SwitchBoardScreens sbs = (SwitchBoardScreens) sb;
		sbs.setAllUpDownWithSeparateSwitch((Switch) blocks.get(allDownSwitchName), (Switch) blocks.get(allUpSwitchName));
	}

	private void parseConnection(SwitchBoardDimmers sb, Attributes atts) {
		String switchName = atts.getValue("dimmerSwitch");
		String lampName = atts.getValue("dimmedLamp");
		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoardDimmers " + sb.getName() + " needs DimmerSwitches named " + switchName + ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(lampName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName() + " needs a Lamp named " + lampName + ", which it did not encounter in configuration file.");
		}
		sb.add((DimmerSwitches) blocks.get(switchName), (DimmedLamp) blocks.get(lampName));
	}

	private void parseConnection(SwitchBoardScreens sb, Attributes atts) {
		String switchDownName = atts.getValue("switchDown");
		String switchUpName = atts.getValue("switchUp");
		String screenName = atts.getValue("screen");
		sb.addScreen((Switch) blocks.get(switchDownName), (Switch) blocks.get(switchUpName), (Screen) blocks.get(screenName), false);
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
