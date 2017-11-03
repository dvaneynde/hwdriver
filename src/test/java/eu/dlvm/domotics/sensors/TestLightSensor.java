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

	private static final long SAMPLE_TIME = 500;
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
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, -1000, 500, 500);
			fail("Should fail");
		} catch (ConfigurationException e) {
			;
		}
	}

	@Test
	public final void initLow() {
		ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, 1000, 2, 3);
		ls.registerListener(this);
		seq = 0L;
		cur = -SAMPLE_TIME;

		hw.level = 100;
		loopTo(0);
		check(LightSensor.States.LOW, null, 0);
		loopTo(500);
		check(LightSensor.States.LOW, null, 0);
		loopTo(1000);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);
		loopTo(1000 + LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 2);
		loopTo(1000 + 500 + LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 2);
		loopTo(1000 + 2 * LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.LOW, EventType.LIGHT_LOW, 3);
	}

	@Test
	public final void initHigh() {
		Assert.assertEquals(1000L, LightSensor.DEFAULT_REPEAT_EVENT_MS); // zoniet parameter van maken
		Assert.assertTrue("Sample Time must divide Repeat", LightSensor.DEFAULT_REPEAT_EVENT_MS % SAMPLE_TIME == 0);

		ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, 1000, 2, 3);
		ls.registerListener(this);
		seq = 0L;
		cur = -SAMPLE_TIME;

		hw.level = 1100;
		loopTo(0);
		check(LightSensor.States.HIGH, null, 0);
		loopTo(500);
		check(LightSensor.States.HIGH, null, 0);
		loopTo(1000);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 1);
		loopTo(1000 + LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 2);
		loopTo(1000 + 500 + LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 2);
		loopTo(1000 + 2 * LightSensor.DEFAULT_REPEAT_EVENT_MS);
		check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 3);
	}

	@Test
	public final void testLowHighLow() {
		try {
			ls = new LightSensor("MyLightSensor", "LightSensor Description", null, LIGHTSENSOR_CH, ctx, 1000, 2, 3);
			ls.registerListener(this);
			Assert.assertEquals(1000L, LightSensor.DEFAULT_REPEAT_EVENT_MS); // zoniet parameter van maken

			seq = 0L;
			cur = -SAMPLE_TIME;
			loopTo(0);
			hw.level = 100;
			check(LightSensor.States.LOW, null, 0);

			loopTo(LightSensor.DEFAULT_REPEAT_EVENT_MS); // warmup
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 1);

			hw.level = 2000;
			loopTo(SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS); // let op, dit is begintijd LOW2HIGH !
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 1);

			loopTo(2000 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 4);

			hw.level = 900;
			loopTo(5000 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.HIGH2LOW_DELAY, EventType.LIGHT_HIGH, 7);

			hw.level = 1100;
			loopTo(6000 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 8);

			hw.level = 900;
			loopTo(9500 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 12);

			hw.level = 1100;
			loopTo(11500 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.LOW2HIGH_DELAY, EventType.LIGHT_LOW, 14);

			hw.level = 900;
			loopTo(12000 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.LOW, EventType.LIGHT_LOW, 14);

			hw.level = 1100;
			loopTo(14500 + SAMPLE_TIME + LightSensor.DEFAULT_REPEAT_EVENT_MS);
			check(LightSensor.States.HIGH, EventType.LIGHT_HIGH, 17);
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
