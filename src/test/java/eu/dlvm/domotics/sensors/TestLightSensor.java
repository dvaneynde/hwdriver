package eu.dlvm.domotics.sensors;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.iohardware.IHardwareIO;

public class TestLightSensor implements IEventListener {

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
	private EventType lastEvent;
	private int nrEvents;
	private long seq, cur;

	private void loopTo(long targetTime) {
		for (long time = cur + SAMPLE_TIME; time <= targetTime; time += SAMPLE_TIME) {
			ls.loop(time, seq++);
		}
		cur = targetTime;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		lastEvent = event;
		nrEvents++;
	}

	private void check(LightSensor.States stateExpected, EventType eventExpected,
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
		} catch (ConfigurationException e) {
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
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 1);

			loopTo(2100);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 3);

			loopTo(2200);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 3);

			hw.level = 600;
			loopTo(3100);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 4);

			loopTo(6100);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 7);

			loopTo(6500L - SAMPLE_TIME);
			hw.level = 400;
			loopTo(6500);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 7);

			loopTo(7100);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 8);

			loopTo(9400);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 10);

			loopTo(9500);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 11);

			hw.level = 900;
			loopTo(11000);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 12);

		} catch (ConfigurationException e) {
			fail(e.getMessage());
		}
	}
}
