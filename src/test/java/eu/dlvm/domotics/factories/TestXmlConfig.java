package eu.dlvm.domotics.factories;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoContextMock;
import junit.framework.Assert;

public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoContextMock ctx = new DomoContextMock(null);
		XmlDomoticConfigurator.configure("src/test/resources/TestDomoticConfig.xml", ctx);
		Assert.assertEquals(13, ctx.sensors.size());
		Assert.assertEquals(6, ctx.actuators.size());
		Assert.assertEquals(1, ctx.controllers.size());
	}
}
