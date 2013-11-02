package eu.dlvm.domotica.blocks.concrete;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.BaseHardwareMock;
import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.SensorEvent;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestSwitch {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;

		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inval;
		}
	};

	private Hardware hw = new Hardware();
	private DomoContextMock ctx = new DomoContextMock(hw);
	private long seq, cur;
	private Switch sw;
	private SensorEvent lastSwitchEvent;
	private ISensorListener sensorListener = new ISensorListener() {
		@Override
		public void notify(SensorEvent e) {
			lastSwitchEvent = e;
		}
	};

	private void loop(long inc) {
		cur += inc;
		sw.loop(cur, seq++);
	}

	private void loop() {
		loop(10);
	}

	@Before
	public void init() {
		sw = new Switch("TestSwitch", "Unit Test Switch", new LogCh(0), ctx);
		sw.registerListenerDeprecated(sensorListener);
		cur = (seq = 0L);
	}

	@Test
	public void singleClick() {
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		Assert.assertTrue(sw.isSingleClickEnabled()
				&& !sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.SINGLE, lastSwitchEvent
						.getEvent());
	}

	@Test
	public void doubleClickFastEnough() throws InterruptedException {
		sw.setDoubleClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setLongClickEnabled(false);
		sw.setDoubleClickTimeout(100);
		Assert.assertTrue(!sw.isSingleClickEnabled()
				&& sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.DOUBLE, lastSwitchEvent
						.getEvent());

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
	}

	@Test
	public void doubleClickNotFastEnough() throws InterruptedException {
		sw.setDoubleClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setLongClickEnabled(false);
		sw.setDoubleClickTimeout(50);
		Assert.assertTrue(!sw.isSingleClickEnabled()
				&& sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(30);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(30);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
	}

	@Test
	public void longClickLongEnough() throws InterruptedException {
		sw.setLongClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setDoubleClickEnabled(false);
		sw.setLongClickTimeout(50);
		Assert.assertTrue(!sw.isSingleClickEnabled()
				&& !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(60);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastSwitchEvent.getEvent());
		lastSwitchEvent = null;
		
		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
	}

	@Test
	public void longClickNotLongEnough() throws InterruptedException {
		sw.setLongClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setDoubleClickEnabled(false);
		sw.setLongClickTimeout(50);
		Assert.assertTrue(!sw.isSingleClickEnabled()
				&& !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
	}

	@Test
	public void singleAndDouble() throws InterruptedException {
		sw.setSingleClickEnabled(true);
		sw.setDoubleClickEnabled(true);
		sw.setLongClickEnabled(false);
		sw.setDoubleClickTimeout(50);
		Assert.assertTrue(sw.isSingleClickEnabled()
				&& sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.SINGLE, lastSwitchEvent
						.getEvent());
		lastSwitchEvent = null;

		// Double click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(10);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(10);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.DOUBLE, lastSwitchEvent
						.getEvent());

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
	}

	@Test
	public void singleAndLong() throws InterruptedException {
		sw.setSingleClickEnabled(true);
		sw.setDoubleClickEnabled(false);
		sw.setLongClickEnabled(true);
		sw.setLongClickTimeout(50);
		Assert.assertTrue(sw.isSingleClickEnabled()
				&& !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.SINGLE, lastSwitchEvent
						.getEvent());
		lastSwitchEvent = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(60);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastSwitchEvent.getEvent());
		lastSwitchEvent = null;
		
		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		lastSwitchEvent = null;
		
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
	}

	
	@Test
	public void singleAndDoubleAndLong() throws InterruptedException {
		sw.setSingleClickEnabled(true);
		sw.setDoubleClickEnabled(true);
		sw.setDoubleClickTimeout(50);
		sw.setLongClickEnabled(true);
		sw.setLongClickTimeout(100);
		Assert.assertTrue(sw.isSingleClickEnabled()
				&& sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(60);
		hw.inval = false;
		loop();
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.SINGLE, lastSwitchEvent
						.getEvent());
		lastSwitchEvent = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(110);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastSwitchEvent.getEvent());
		lastSwitchEvent = null;

		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastSwitchEvent);
		
		// Double click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(10);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastSwitchEvent);

		loop(10);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastSwitchEvent);
		Assert
				.assertEquals(ISwitchListener.ClickType.DOUBLE, lastSwitchEvent
						.getEvent());

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
	}
}
