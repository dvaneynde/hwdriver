package eu.dlvm.domotics.sensors;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
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

	private static final long SAMPLE_TIME = 500;
	private static final String LIGHTSENSOR_CH = Integer.toString(10);
	private Hardware hw = new Hardware();
	private DomoticMock dom = new DomoticMock();
	private LightSensor ls;
	private EventType lastEvent;
	private int nrEvents;
	private long cur;

	private void loopTo(long targetTime) {
		for (long time = cur + SAMPLE_TIME; time <= targetTime; time += SAMPLE_TIME) {
			ls.loop(time);
		}
		cur = targetTime;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		lastEvent = event;
		nrEvents++;
	}

	private void check(LightSensor.States stateExpected, EventType eventExpected, int nrEventsExpected) {
		Assert.assertEquals(stateExpected, ls.getState());
		Assert.assertEquals(eventExpected, lastEvent);
		Assert.assertEquals(nrEventsExpected, nrEvents);
	}

	// ===============
	// TESTS

	@Test
	public final void testInitWrong() {
		try {
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, hw, dom, -1000, 500, 500);
			fail("Should fail");
		} catch (ConfigurationException e) {
			;
		}
	}

	@Test
	public final void initLow() {
		ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, hw, dom, 1000, 2, 3);
		ls.registerListener(this);
		cur = -SAMPLE_TIME;

		hw.level = 100;
		loopTo(0);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);
		loopTo(10000);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);
	}

	@Test
	public final void initHigh() {
		ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, hw, dom, 1000, 2, 3);
		ls.registerListener(this);
		cur = -SAMPLE_TIME;

		hw.level = 1100;
		loopTo(0);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 1);
		loopTo(10000);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 1);
	}

	@Test
	public final void testLowHighLow() {
		try {
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, hw, dom, 1000, 2, 3);
			ls.registerListener(this);

			cur = -SAMPLE_TIME;
			loopTo(0);
			// Init, should be low
			hw.level = 900;
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);
			loopTo(2500);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);
			loopTo(3000);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);

			// High light
			hw.level = 1100;
			loopTo(5000);
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 1);
			loopTo(5500);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 2);

			// A little less, but not long enough, should stay high
			hw.level = 900;
			loopTo(6000);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 2);
			loopTo(8500);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 2);
			hw.level = 1100;
			loopTo(9000);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 2);

			// Should go low
			hw.level = 900;
			loopTo(9500);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 2);
			loopTo(12000);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 2);
			loopTo(12500);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 3);

			// A moment of sunshine, but not long enough
			hw.level = 1100;
			loopTo(13000);
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 3);
			loopTo(14500);
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 3);
			hw.level = 950;
			loopTo(15000);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 3);

		} catch (ConfigurationException e) {
			fail(e.getMessage());
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
