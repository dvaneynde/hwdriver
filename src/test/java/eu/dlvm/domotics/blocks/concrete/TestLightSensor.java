package eu.dlvm.domotics.blocks.concrete;

import static org.junit.Assert.fail;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.ISensorListener;
import eu.dlvm.domotics.base.IllegalConfigurationException;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestLightSensor {

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
	private IHardwareAccess ctx = new DomoContextMock(hw);
	private long seq, cur;

	private ISensorListener sensorListener = new ISensorListener() {
		@Override
		public void notify(SensorEvent e) {
			lastEvent = e;
		}
	};
	private SensorEvent lastEvent;

	@BeforeClass
	public static void initLog() {
		BasicConfigurator.configure();
	}

	//	private void loop(long inc) {
	//		cur += inc;
	//		sw.loop(cur, seq++);
	//	}

	@Test
	public final void testInitWrong() {
		try {
			LightSensor ls = new LightSensor("MyLightSensor", "LightSensor Description", LIGHTSENSOR_CH, ctx, 1000, 100, 500,
					500);
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

	@Test
	public final void testLowHighLow() {
		try {
			LightSensor ls = new LightSensor("MyLightSensor", "LightSensor Description", LIGHTSENSOR_CH, ctx, 500, 1000, 300,
					300);
			ls.registerListenerDeprecated(sensorListener);
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
			Assert.assertEquals(LightSensor.States.HIGH, lastEvent.getEvent());
			lastEvent = null;
			
			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH, ls.getState());
			Assert.assertNull(lastEvent);
			
			hw.level=400;
			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH2LOW_WAITING, ls.getState());
			Assert.assertNull(lastEvent);
			
			loop200(ls);
			Assert.assertEquals(LightSensor.States.HIGH2LOW_WAITING, ls.getState());
			Assert.assertNull(lastEvent);

			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW, ls.getState());
			Assert.assertEquals(LightSensor.States.LOW, lastEvent.getEvent());
			lastEvent = null;
			
			loop200(ls);
			Assert.assertEquals(LightSensor.States.LOW, ls.getState());
			Assert.assertNull(lastEvent);
			
		} catch (IllegalConfigurationException e) {
			fail("Should be ok.");
		}

	}

}
