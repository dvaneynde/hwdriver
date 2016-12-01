package eu.dlvm.domotics.factories;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoContextMock;
import junit.framework.Assert;

public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoContextMock ctx = new DomoContextMock(null);
		XmlDomoticConfigurator.configure("src/test/resources/TestDomoticConfig.xml", ctx);
		Assert.assertEquals(15, ctx.sensors.size());
		Assert.assertEquals(8, ctx.actuators.size());
		Assert.assertEquals(2, ctx.controllers.size());
	}
}
