package eu.dlvm.domotics.blocks.concrete;

import static org.junit.Assert.fail;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IllegalConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestLightSensor implements IThresholdListener {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public int level;

		@Override
		public int readAnalogInput(LogCh channel) throws IllegalArgumentException {
			Assert.assertTrue(channel == LIGHTSENSOR_CH);
			return level;
		}
	};

	private static final LogCh LIGHTSENSOR_CH = new LogCh(10);
	private Hardware hw = new Hardware();
	private IDomoticContext ctx = new DomoContextMock(hw);
	private long seq, cur;

	@BeforeClass
	public static void initLog() {
		BasicConfigurator.configure();
	}

	@Test
	public final void testInitWrong() {
		try {
			LightSensor ls = new LightSensor("MyLightSensor", "LightSensor Description", LIGHTSENSOR_CH, ctx, 1000, 100, 500);
			fail("Should fail, since lowThreshold > highThreshold. LightSensor=" + ls);
		} catch (IllegalConfigurationException e) {
			;
		}
	}

	private void loop200(LightSensor ls) {
		cur += 200;
		seq++;
		ls.loop(cur, seq);
	}

	@Override
	public void onEvent(Sensor source, EventType event) {
		lastEvent = event;
	}
	private IThresholdListener.EventType lastEvent;

	@Test
	public final void testLowHighLow() {
		try {
			LightSensor ls = new LightSensor("MyLightSensor", "LightSensor Description", LIGHTSENSOR_CH, ctx, 500, 1000, 300);
			ls.registerListener(this);

			seq = cur = 0L;
			Assert.assertEquals(LightSensor.States.LOW, ls.getState());
			hw.level = 1100;
			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW2HIGH_WAITING, ls.getState());
			Assert.assertNull(lastEvent);

			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW2HIGH_WAITING, ls.getState());
			Assert.assertNull(lastEvent);

			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH, ls.getState());
			Assert.assertEquals(EventType.HIGH, lastEvent);
			lastEvent = null;

			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH, ls.getState());
			Assert.assertNull(lastEvent);

			hw.level = 400;
			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH2LOW_WAITING, ls.getState());
			Assert.assertNull(lastEvent);

			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH2LOW_WAITING, ls.getState());
			Assert.assertNull(lastEvent);

			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW, ls.getState());
			Assert.assertEquals(EventType.LOW, lastEvent);
			lastEvent = null;

			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW, ls.getState());
			Assert.assertNull(lastEvent);

		} catch (IllegalConfigurationException e) {
			fail(e.getMessage());
		}
	}
}
