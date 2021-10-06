package eu.dlvm.iohardware.diamondsys.factories;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.messaging.DmmatBoardWithMsg;
import eu.dlvm.iohardware.diamondsys.messaging.OpalmmBoardWithMsg;
import eu.dlvm.iohardware.diamondsys.messaging.Opmm1616BoardWithMsg;

/**
 * @author dirk vaneynde
 */
public class XmlHwConfigurator implements IBoardFactory {

	private static final Logger log = LoggerFactory.getLogger(XmlHwConfigurator.class);
	private String cfgFilepath; 

	int boardNr, address;
	private ChannelType chtype = null;
	String desc;
	boolean digiIn, digiOut, anaIns, anaOuts;

	public XmlHwConfigurator(String cfgFilePath) {
		this.cfgFilepath = cfgFilePath;
	}

	@Override
	public void configure(final List<Board> boards, final ChannelMap map) {
		try {
			SAXParserFactory f = SAXParserFactory.newInstance();
			f.setValidating(true);
			f.setNamespaceAware(true);
			SAXParser p = f.newSAXParser();
			DefaultHandler h = new DefaultHandler() {
				public void startElement(String uri, String localName, String qqName, Attributes atts)
						throws SAXException {

					if (uri.equals("http://dlvmechanografie.eu/DiamondBoardsConfig")) {
						log.debug("Start Element '" + localName + "', namespace=" + uri);
						if (localName.equals("boards")) {
							;
						} else if (localName.equals("opalmm") || (localName.equals("dmmat"))
								|| (localName.equals("opmm1616"))) {
							parseBoardParams(atts);
						} else if (localName.equals("digital-input")) {
							chtype = ChannelType.DigiIn;
							digiIn = true;
						} else if (localName.equals("digital-output")) {
							chtype = ChannelType.DigiOut;
							digiOut = true;
						} else if (localName.equals("analog-input")) {
							chtype = ChannelType.AnlgIn;
							anaIns = true;
						} else if (localName.equals("analog-output")) {
							chtype = ChannelType.AnlgOut;
							anaOuts = true;
						} else if (localName.equals("channel")) {
							configureChannel(atts, map);
						} else {
							log.error("Parsing " + getCfgFilepath() + ", read unknown element '" + localName
									+ "'. This is a bug.");
						}
					} else {
						log.warn("Parsing config file " + getCfgFilepath() + ", unknown namesapce '" + uri
								+ "'. Ignored.");
					}
				}

				public void endElement(String uri, String localName, String qqName) throws SAXException {

					log.debug("End Element '" + localName + "'");
					if (localName.equals("opalmm")) {
						Board board = new OpalmmBoardWithMsg(boardNr, address, desc, digiIn, digiOut);
						log.debug("Created board: " + board.toString());
						boards.add(board);
						reset();
					} else if (localName.equals("dmmat")) {
						Board board = new DmmatBoardWithMsg(boardNr, address, desc, digiIn, digiOut, anaIns, anaOuts);
						log.debug("Created board: " + board.toString());
						boards.add(board);
						reset();
					} else if (localName.equals("opmm1616")) {
						Board board = new Opmm1616BoardWithMsg(boardNr, address, desc, digiIn, digiOut);
						log.debug("Created board: " + board.toString());
						boards.add(board);
						reset();
					}
				}

				public void reset() {
					boardNr = address = 0;
					desc = null;
					chtype = null;
					digiIn = digiOut = anaIns = anaOuts = false;
				}

				/*
				 * public void characters(char ch[], int start, int length)
				 * throws SAXException { String s = new String(ch, start,
				 * length); }
				 */
			};
			p.parse(getCfgFilepath(), h);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Error while parsing config file " + getCfgFilepath(), e);
		}
	}

	private void parseBoardParams(Attributes attributes) {
		boardNr = Integer.parseInt(attributes.getValue("board"));
		address = extractAddress(attributes);
		desc = attributes.getValue("desc");
	}

	private int extractAddress(Attributes attributes) {
		int address;
		String s = attributes.getValue("address");
		if (s.startsWith("0x")) {
			address = Integer.parseInt(s.substring(2), 16);
		} else {
			address = Integer.parseInt(s);
		}
		return address;
	}

	private void configureChannel(Attributes atts, ChannelMap cm) {
		int ch = Integer.parseInt(atts.getValue("channel"));
		String lc = atts.getValue("logical-id");
		if (lc != null) {
			FysCh fc = new FysCh(boardNr, chtype, ch);
			cm.add(lc, fc);
		}
	}

	// -------------------------------------------

	public String getCfgFilepath() {
		return cfgFilepath;
	}

}
