package eu.dlvm.domotics.factories;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoContextMock;
import junit.framework.Assert;

public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoContextMock ctx = new DomoContextMock(null);
		XmlDomoticConfigurator cf = new XmlDomoticConfigurator();
		cf.setCfgFilepath("src/test/resources/TestDomoticConfig.xml");
		cf.configure(ctx);
		Assert.assertEquals(16, ctx.sensors.size());
		Assert.assertEquals(6, ctx.actuators.size());
		Assert.assertEquals(2, ctx.controllers.size());
	}
}
