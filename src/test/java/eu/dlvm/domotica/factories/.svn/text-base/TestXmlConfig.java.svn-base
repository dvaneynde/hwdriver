package eu.dlvm.domotica.factories;

import junit.framework.Assert;

import org.junit.Test;

import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.factories.XmlDomoticConfigurator;


public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoContextMock ctx = new DomoContextMock(null);
		XmlDomoticConfigurator cf = new XmlDomoticConfigurator();
		cf.setCfgFilepath("src/test/resources/TestDomoticConfig.xml");
		cf.configure(ctx);
		Assert.assertEquals(7, ctx.sensors.size());
		Assert.assertEquals(5, ctx.actuators.size());
	}
}
