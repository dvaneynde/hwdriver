package eu.dlvm.domotica.factories;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotica.blocks.IDomoContext;

public class XmlDomoticConfigurator {
	static Logger log = Logger.getLogger(XmlDomoticConfigurator.class);
	private String cfgFilepath;

	public String getCfgFilepath() {
		return cfgFilepath;
	}

	public void setCfgFilepath(String cfgFilepath) {
		this.cfgFilepath = cfgFilepath;
	}

	public void configure(IDomoContext domoCtx) {
		try {
			SAXParserFactory f = SAXParserFactory.newInstance();
			f.setValidating(true);
			f.setNamespaceAware(true);
			SAXParser p = f.newSAXParser();
			DefaultHandler2 h = new DomoticXmlDefaultHandler(domoCtx);
			p.parse(getCfgFilepath(), h);
		} catch (ParserConfigurationException e) {
			log.error("Configuration Failed: ",e);
			throw new ConfigurationException(e.getMessage());
		} catch (SAXException e) {
			log.error("Configuration Failed: ",e);
			throw new ConfigurationException(e.getMessage());
		} catch (IOException e) {
			log.error("Configuration Failed: ",e);
			throw new ConfigurationException(e.getMessage());
		}

	}

}
