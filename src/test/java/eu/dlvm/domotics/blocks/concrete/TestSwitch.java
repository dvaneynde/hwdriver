package eu.dlvm.domotics.blocks.concrete;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestSwitch {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;

		@Override
		public boolean readDigitalInput(String channel) {
			return inval;
		}
	};

	private Hardware hw = new Hardware();
	private DomoContextMock ctx = new DomoContextMock(hw);
	private long seq, cur;
	private Switch sw;
	private ISwitchListener.ClickType lastClick;
	private ISwitchListener listener = new ISwitchListener() {
		@Override
		public void onEvent(Switch s, ISwitchListener.ClickType e) {
			lastClick = e;
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
		sw = new Switch("TestSwitch", "Unit Test Switch", Integer.toString(0), ctx);
		sw.registerListener(listener);
		cur = (seq = 0L);
	}

	@Test
	public void singleClick() {
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
		Assert.assertTrue(sw.isSingleClickEnabled() && !sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.SINGLE, lastClick);
	}

	@Test
	public void doubleClickFastEnough() throws InterruptedException {
		sw.setDoubleClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setLongClickEnabled(false);
		sw.setDoubleClickTimeout(100);
		Assert.assertTrue(!sw.isSingleClickEnabled() && sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.DOUBLE, lastClick);

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
		Assert.assertTrue(!sw.isSingleClickEnabled() && sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(30);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(30);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
	}

	@Test
	public void longClickLongEnough() throws InterruptedException {
		sw.setLongClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setDoubleClickEnabled(false);
		sw.setLongClickTimeout(50);
		Assert.assertTrue(!sw.isSingleClickEnabled() && !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(60);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastClick);
		lastClick = null;

		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastClick);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
	}

	@Test
	public void longClickNotLongEnough() throws InterruptedException {
		sw.setLongClickEnabled(true);
		sw.setSingleClickEnabled(false);
		sw.setDoubleClickEnabled(false);
		sw.setLongClickTimeout(50);
		Assert.assertTrue(!sw.isSingleClickEnabled() && !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
	}

	@Test
	public void singleAndDouble() throws InterruptedException {
		sw.setSingleClickEnabled(true);
		sw.setDoubleClickEnabled(true);
		sw.setLongClickEnabled(false);
		sw.setDoubleClickTimeout(50);
		Assert.assertTrue(sw.isSingleClickEnabled() && sw.isDoubleClickEnabled() && !sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.SINGLE, lastClick);
		lastClick = null;

		// Double click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(10);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(10);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.DOUBLE, lastClick);

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
		Assert.assertTrue(sw.isSingleClickEnabled() && !sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(40);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.SINGLE, lastClick);
		lastClick = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(60);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastClick);
		lastClick = null;

		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastClick);
		lastClick = null;

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);
	}

	@Test
	public void singleAndDoubleAndLong() throws InterruptedException {
		sw.setSingleClickEnabled(true);
		sw.setDoubleClickEnabled(true);
		sw.setDoubleClickTimeout(50);
		sw.setLongClickEnabled(true);
		sw.setLongClickTimeout(100);
		Assert.assertTrue(sw.isSingleClickEnabled() && sw.isDoubleClickEnabled() && sw.isLongClickEnabled());

		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);

		// Single click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(60);
		hw.inval = false;
		loop();
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.SINGLE, lastClick);
		lastClick = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(110);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.LONG, lastClick);
		lastClick = null;

		loop();
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNull(lastClick);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNull(lastClick);

		// Double click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(10);
		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.WAIT_2ND_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(10);
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(ISwitchListener.ClickType.DOUBLE, lastClick);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
	}
}
