package eu.dlvm.domotics.controllers;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;

public class TestSunWindController {

	private Queue<EventType> events = new LinkedList<>();
	public long middayLong = 1562500800000L;

	private class DummyScreen implements IEventListener {
		@Override
		public String getName() {
			return "DummyScreen";
		}

		@Override
		public void onEvent(Block source, EventType event) {
			events.add(event);
		}
	}

	SunWindController swc;

	@Before
	public void init() {
		DomoticMock domoticContext = new DomoticMock();
		swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());

	}

	@Test
	public void testWindEventsGoThroughWhenDisabled() {
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
		swc.loop(middayLong);

		for (int i = 0; i < 3; i++) {
			swc.onEvent(null, EventType.ALARM);
			swc.loop(middayLong);
			Assert.assertFalse(swc.isEnabled());
			Assert.assertEquals(EventType.ALARM, events.poll());
			Assert.assertTrue(events.isEmpty());

			swc.onEvent(null, EventType.SAFE);
			swc.loop(middayLong);
			Assert.assertFalse(swc.isEnabled());
			Assert.assertEquals(EventType.SAFE, events.poll());
			Assert.assertTrue(events.isEmpty());
		}
	}

	@Test
	public void testSafeWindScenarios() {
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
		swc.loop(middayLong);

		swc.onEvent(null, EventType.LIGHT_HIGH);
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.off();
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_LOW);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.UP, events.poll());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testAlarmWindScenarios() {
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
		swc.loop(middayLong);

		swc.onEvent(null, EventType.LIGHT_HIGH);
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_LOW);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testScreensDownAfterWindSafeAndSunStillHigh() {
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.UP, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		swc.loop(middayLong);
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		swc.loop(middayLong);
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		swc.loop(middayLong);
		Assert.assertEquals(EventType.SAFE, events.poll());
		swc.loop(middayLong);
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testPeriodicWindEventsDoesNotMoveScreens() {
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		swc.loop(middayLong);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		swc.loop(middayLong);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
	}
}
