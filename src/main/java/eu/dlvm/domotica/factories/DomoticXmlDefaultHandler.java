package eu.dlvm.domotica.factories;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.concrete.DimmedLamp;
import eu.dlvm.domotica.blocks.concrete.DimmerSwitches;
import eu.dlvm.domotica.blocks.concrete.Fan;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Screen;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardDimmers;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardFans;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardScreens;
import eu.dlvm.domotica.blocks.concrete.Timer;
import eu.dlvm.domotica.blocks.concrete.TimerDayNight;
import eu.dlvm.iohardware.LogCh;

class DomoticXmlDefaultHandler extends DefaultHandler2 {

	private Block block;
	private Map<String, Block> blocks = new HashMap<String, Block>();
	private IDomoContext ctx;
	private String name, desc;
	private LogCh channel;

	public DomoticXmlDefaultHandler(IDomoContext ctx) {
		super();
		this.ctx = ctx;
	}

	public void startElement(String uri, String localName, String qqName,
			Attributes atts) throws SAXException {
		if (localName.equals("domotic")) {
			;
		} else if (localName.equals("switch")) {
			parseBaseBlockWithChannel(atts);
			// TODO default attribute values, werkt dat niet?

			boolean singleClick = parseBoolAttribute("singleClick", true, atts);
			boolean longClick = parseBoolAttribute("longClick", false, atts);
			boolean doubleClick = parseBoolAttribute("doubleClick", false, atts);
			block = new Switch(name, desc, channel, singleClick, longClick,
					doubleClick, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timer") ) {
			parseBaseBlock(atts);
			block = new Timer(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("timerDayNight")) {
			parseBaseBlock(atts);
			block = new TimerDayNight(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("on")) {
			((Timer)block).setOnTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("off")) {
			((Timer)block).setOffTime(Integer.parseInt(atts.getValue("hour")), Integer.parseInt(atts.getValue("minute")));
		} else if (localName.equals("dimmerSwitches")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			LogCh channelDown = (s == null ? new LogCh(name + "Down")
					: new LogCh(s));
			s = atts.getValue("channelUp");
			LogCh channelUp = (s == null ? new LogCh(name + "Up")
					: new LogCh(s));
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
			block = new Fan(name, desc, (Lamp) blocks.get(lampName), channel,
					ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("screen")) {
			parseBaseBlock(atts);
			String s = atts.getValue("channelDown");
			LogCh channelDown = (s == null ? new LogCh(name + "Down")
					: new LogCh(s));
			s = atts.getValue("channelUp");
			LogCh channelUp = (s == null ? new LogCh(name + "Up")
					: new LogCh(s));
			block = new Screen(name, desc, channelDown, channelUp, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("switchboard")) {
			parseBaseBlock(atts);
			block = new SwitchBoard(name, desc);
			blocks.put(block.getName(), block);
		} else if (localName.equals("switchboardFans")) {
			parseBaseBlock(atts);
			block = new SwitchBoardFans(name, desc);
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
			if (block instanceof SwitchBoard) {
				parseConnection((SwitchBoard) block, atts);
			} else if (block instanceof SwitchBoardFans) {
				parseConnection((SwitchBoardFans) block, atts);
			} else if (block instanceof SwitchBoardDimmers) {
				parseConnection((SwitchBoardDimmers) block, atts);
			} else if (block instanceof SwitchBoardScreens) {
				parseConnection((SwitchBoardScreens) block, atts);
			} else {
				throw new ConfigurationException(
						"Connection found in unknown Block.");
			}
		} else if (localName.equals("c-timer")) {
			parseConnectionTimer((SwitchBoard) block, atts);
		} else if (localName.equals("allSwitch")) {
			parseAllSwitch(block, atts);
		} else if (localName.equals("allUpDownSwitches")) {
			parseAllUpDownSwitch(block, atts);
		} else {
			throw new RuntimeException("Block " + qqName + " not supported.");
		}
	}

	public void endElement(String uri, String localName, String qqName)
			throws SAXException {
	}

	private void parseAllSwitch(Block sb, Attributes atts) {
		String switchName = atts.getValue("switch");
		boolean allOn = Boolean.parseBoolean(atts.getValue("allOn"));
		boolean allOff = Boolean.parseBoolean(atts.getValue("allOff"));

		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Switch named " + switchName
					+ ", which it did not encounter in configuration file.");
		}
		// TODO introduce interface, ICapabilityAllOnOff
		if (sb instanceof SwitchBoard)
			((SwitchBoard) sb).add((Switch) blocks.get(switchName), allOff,
					allOn);
		else if (sb instanceof SwitchBoardDimmers)
			((SwitchBoardDimmers) sb).add((Switch) blocks.get(switchName),
					allOff, allOn);
	}

	private void parseAllUpDownSwitch(Block sb, Attributes atts) {
		String allDownSwitchName = atts.getValue("allDownSwitch");
		String allUpSwitchName = atts.getValue("allUpSwitch");

		if (!blocks.containsKey(allDownSwitchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Switch named " + allDownSwitchName
					+ ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(allUpSwitchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Switch named " + allUpSwitchName
					+ ", which it did not encounter in configuration file.");
		}

		SwitchBoardScreens sbs = (SwitchBoardScreens) sb;
		sbs.setAllUpDownWithSeparateSwitch(
				(Switch) blocks.get(allDownSwitchName),
				(Switch) blocks.get(allUpSwitchName));
	}

	private void parseConnection(SwitchBoard sb, Attributes atts) {
		String switchName = atts.getValue("switch");
		String lampName = atts.getValue("lamp");
		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Switch named " + switchName
					+ ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(lampName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Lamp named " + lampName
					+ ", which it did not encounter in configuration file.");
		}
		sb.add((Switch) blocks.get(switchName), (Lamp) blocks.get(lampName),
				parseBoolAttribute("allOff", false, atts),
				parseBoolAttribute("allOn", false, atts));
	}

	private void parseConnectionTimer(SwitchBoard sb, Attributes atts) {
		String sensorName = atts.getValue("timer");
		String lampName = atts.getValue("lamp");
		if (!blocks.containsKey(sensorName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Sensor named " + sensorName
					+ ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(lampName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Lamp named " + lampName
					+ ", which it did not encounter in configuration file.");
		}
		sb.add((Timer) blocks.get(sensorName), (Lamp) blocks.get(lampName));
	}

	private void parseConnection(SwitchBoardFans sb, Attributes atts) {
		String switchName = atts.getValue("switch");
		String fanName = atts.getValue("fan");
		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoardFans " + sb.getName()
					+ " needs a Switch named " + switchName
					+ ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(fanName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Fan named " + fanName
					+ ", which it did not encounter in configuration file.");
		}
		sb.add((Switch) blocks.get(switchName), (Fan) blocks.get(fanName));
	}

	private void parseConnection(SwitchBoardDimmers sb, Attributes atts) {
		String switchName = atts.getValue("dimmerSwitch");
		String lampName = atts.getValue("dimmedLamp");
		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoardDimmers "
					+ sb.getName() + " needs DimmerSwitches named "
					+ switchName
					+ ", which it did not encounter in configuration file.");
		}
		if (!blocks.containsKey(lampName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Lamp named " + lampName
					+ ", which it did not encounter in configuration file.");
		}
		sb.add((DimmerSwitches) blocks.get(switchName),
				(DimmedLamp) blocks.get(lampName));
	}

	private void parseConnection(SwitchBoardScreens sb, Attributes atts) {
		String switchDownName = atts.getValue("switchDown");
		String switchUpName = atts.getValue("switchUp");
		String screenName = atts.getValue("screen");
		sb.addScreen((Switch) blocks.get(switchDownName),
				(Switch) blocks.get(switchUpName),
				(Screen) blocks.get(screenName), false);
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

	private boolean parseBoolAttribute(String attName, boolean defaultVal,
			Attributes atts) {
		if (atts.getValue(attName) == null)
			return defaultVal;
		else
			return Boolean.parseBoolean(atts.getValue(attName));
	}

}
