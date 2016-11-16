package eu.dlvm.domotics.blocks.concrete;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IllegalConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.iohardware.IHardwareIO;

public class TestLightSensor implements IThresholdListener {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public int level;

		@Override
		public int readAnalogInput(String channel) throws IllegalArgumentException {
			Assert.assertTrue(channel.equals(LIGHTSENSOR_CH));
			return level;
		}
	};

	private static final long SAMPLE_TIME = 50;
	private static final String LIGHTSENSOR_CH = Integer.toString(10);
	private Hardware hw = new Hardware();
	private IDomoticContext ctx = new DomoContextMock(hw);
	private LightSensor ls;
	private IThresholdListener.EventType lastEvent;
	private int nrEvents;
	private long seq, cur;

	private void loopTo(long targetTime) {
		for (long time = cur + SAMPLE_TIME; time <= targetTime; time += SAMPLE_TIME) {
			ls.loop(time, seq++);
		}
		cur = targetTime;
	}

	@Override
	public void onEvent(Sensor source, EventType event) {
		lastEvent = event;
		nrEvents++;
	}

	private void check(LightSensor.States stateExpected, IThresholdListener.EventType eventExpected,
			int nrEventsExpected) {
		Assert.assertEquals(stateExpected, ls.getState());
		Assert.assertEquals(eventExpected, lastEvent);
		Assert.assertEquals(nrEventsExpected, nrEvents);
	}

	// ===============
	// TESTS

	@Test
	public final void testInitWrong() {
		try {
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, 1000, 100, 500,
					500);
			fail("Should fail, since lowThreshold > highThreshold. LightSensor=" + ls);
		} catch (IllegalConfigurationException e) {
			;
		}
	}

	@Test
	public final void testLowHighLow() {
		try {
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, 500, 1000, 2,
					3);
			ls.registerListener(this);

			seq = cur = 0L;
			check(LightSensor.States.LOW, null, 0);

			hw.level = 1100;
			loopTo(100);
			check(LightSensor.States.LOW2HIGH_DELAY, null, 0);

			loopTo(1900);
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LOW, 1);

			loopTo(2100);
			check(LightSensor.States.HIGH, EventType.HIGH, 3);

			loopTo(2200);
			check(LightSensor.States.HIGH, IThresholdListener.EventType.HIGH, 3);

			hw.level = 600;
			loopTo(3100);
			check(LightSensor.States.HIGH, IThresholdListener.EventType.HIGH, 4);

			loopTo(6100);
			check(LightSensor.States.HIGH, IThresholdListener.EventType.HIGH, 7);

			loopTo(6500L - SAMPLE_TIME);
			hw.level = 400;
			loopTo(6500);
			check(LightSensor.States.HIGH2LOW_DELAY, IThresholdListener.EventType.HIGH, 7);

			loopTo(7100);
			check(LightSensor.States.HIGH2LOW_DELAY, IThresholdListener.EventType.HIGH, 8);

			loopTo(9400);
			check(LightSensor.States.HIGH2LOW_DELAY, IThresholdListener.EventType.HIGH, 10);

			loopTo(9500);
			check(LightSensor.States.LOW, IThresholdListener.EventType.LOW, 11);

			hw.level = 900;
			loopTo(11000);
			check(LightSensor.States.LOW, IThresholdListener.EventType.LOW, 12);

		} catch (IllegalConfigurationException e) {
			fail(e.getMessage());
		}
	}
}
