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

class DomoticXmlDefaultHandler extends DefaultHandler2 {

	private Block block;
	private Map<String, Block> blocks = new HashMap<String, Block>();
	private IDomoContext ctx;
	private String name, desc;
	private int channel;

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
			block = new Switch(name, desc, channel, ctx);
			blocks.put(block.getName(), block);
		} else if (localName.equals("dimmerSwitches")) {
			parseBaseBlock(atts);
			int channelDown = Integer.parseInt(atts.getValue("channelDown"));
			int channelUp = Integer.parseInt(atts.getValue("channelUp"));
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
			int channelDown = Integer.parseInt(atts.getValue("channelDown"));
			int channelUp = Integer.parseInt(atts.getValue("channelUp"));
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
		} else if (localName.equals("allSwitch")) {
			parseAllSwitch((SwitchBoard) block, atts);
		} else {
			throw new RuntimeException("Block " + qqName + " not supported.");
		}
	}

	public void endElement(String uri, String localName, String qqName)
			throws SAXException {
	}

	private void parseAllSwitch(SwitchBoard sb, Attributes atts) {
		String switchName = atts.getValue("switch");
		boolean allOn = Boolean.parseBoolean(atts.getValue("allOn"));
		boolean allOff = Boolean.parseBoolean(atts.getValue("allOff"));

		if (!blocks.containsKey(switchName)) {
			throw new ConfigurationException("SwitchBoard " + sb.getName()
					+ " needs a Switch named " + switchName
					+ ", which it did not encounter in configuration file.");
		}
		sb.add((Switch) blocks.get(switchName), allOff, allOn);
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
		sb.add((Switch) blocks.get(switchName), (Lamp) blocks.get(lampName));
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
		channel = Integer.parseInt(atts.getValue("channel"));
	}

	private void parseBaseBlock(Attributes atts) {
		name = atts.getValue("name");
		desc = atts.getValue("desc");
	}

}
