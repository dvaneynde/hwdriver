package eu.dlvm.domotics.factories;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import eu.dlvm.iohardware.IHardwareIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.IDomoticBuilder;

public class XmlDomoticConfigurator {

	private static Logger logger = LoggerFactory.getLogger(XmlDomoticConfigurator.class);

	public static void configure(String cfgFilepath,  IHardwareIO hardware, IDomoticBuilder builder) {
		try {
			File cfgFile = convertCfgFilepath(cfgFilepath);

			SAXParserFactory f = SAXParserFactory.newInstance();
			f.setValidating(true);
			f.setNamespaceAware(true);
			SAXParser p = f.newSAXParser();
			DefaultHandler2 h = new XmlElementHandlers(builder, hardware);
			p.parse(cfgFile, h);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Configuration Failed: ", e);
			throw new ConfigurationException(e.getMessage());
		}
	}

	private static File convertCfgFilepath(String cfgFilepath) {
		File cfgFile = new File(cfgFilepath);
		if (!cfgFile.exists())
			throw new IllegalArgumentException("File '" + cfgFilepath + "' does not exist.");
		return cfgFile;
	}

}
