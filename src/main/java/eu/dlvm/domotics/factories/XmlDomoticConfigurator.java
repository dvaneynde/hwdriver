package eu.dlvm.domotics.factories;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.base.IDomoticContext;

public class XmlDomoticConfigurator {
	static Logger log = LoggerFactory.getLogger(XmlDomoticConfigurator.class);
	private String cfgFilepath;

	public String getCfgFilepath() {
		return cfgFilepath;
	}

	public void setCfgFilepath(String cfgFilepath) {
		this.cfgFilepath = cfgFilepath;
	}

	public void configure(IDomoticContext domoCtx) {
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
