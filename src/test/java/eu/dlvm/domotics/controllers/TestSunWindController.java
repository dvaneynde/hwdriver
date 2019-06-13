package eu.dlvm.domotics.controllers;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;

public class TestSunWindController {

	private Queue<EventType> events = new LinkedList<>();

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

	@Test
	public void testWindEventsGoThroughWhenDisabled() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		SunWindController swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		for (int i = 0; i < 3; i++) {
			swc.onEvent(null, EventType.ALARM);
			Assert.assertFalse(swc.isEnabled());
			Assert.assertEquals(EventType.ALARM, events.poll());
			Assert.assertTrue(events.isEmpty());

			swc.onEvent(null, EventType.SAFE);
			Assert.assertFalse(swc.isEnabled());
			Assert.assertEquals(EventType.SAFE, events.poll());
			Assert.assertTrue(events.isEmpty());
		}
	}

	@Test
	public void testSafeWindScenarios() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		SunWindController swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.off();
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_LOW);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.UP, events.poll());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testAlarmWindScnearios() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		SunWindController swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_LOW);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testScreensDownAfterWindSafeAndSunStillHigh() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		SunWindController swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.UP, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		Assert.assertEquals(EventType.SAFE, events.poll());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testPeriodicWindEventsDoesNotMoveScreens() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		SunWindController swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", domoticContext);
		swc.registerListener(new DummyScreen());
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.on();
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.DOWN, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.SAFE);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(EventType.ALARM, events.poll());
		Assert.assertTrue(events.isEmpty());

		swc.onEvent(null, EventType.ALARM);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertTrue(events.isEmpty());
	}
}
