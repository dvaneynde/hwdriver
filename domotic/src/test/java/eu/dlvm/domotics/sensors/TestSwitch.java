package eu.dlvm.domotics.sensors;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
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
	private DomoticMock dom = new DomoticMock();
	private long cur;
	private Switch sw;
	private EventType lastClick;
	private IEventListener listener = new IEventListener() {
		@Override
		public void onEvent(Block s, EventType e) {
			lastClick = e;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private void loop(long inc) {
		cur += inc;
		sw.loop(cur);
	}

	private void loop() {
		loop(10);
	}

	@Before
	public void init() {
		sw = new Switch("TestSwitch", "Unit Test Switch", Integer.toString(0), hw, dom);
		sw.registerListener(listener);
		cur = 0L;
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
		Assert.assertEquals(EventType.SINGLE_CLICK, lastClick);
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
		Assert.assertEquals(EventType.DOUBLE_CLICK, lastClick);

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
		Assert.assertEquals(EventType.LONG_CLICK, lastClick);
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
		Assert.assertEquals(EventType.SINGLE_CLICK, lastClick);
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
		Assert.assertEquals(EventType.DOUBLE_CLICK, lastClick);

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
		Assert.assertEquals(EventType.SINGLE_CLICK, lastClick);
		lastClick = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(60);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(EventType.LONG_CLICK, lastClick);
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
		Assert.assertEquals(EventType.SINGLE_CLICK, lastClick);
		lastClick = null;

		// Long click
		hw.inval = true;
		loop();
		Assert.assertEquals(Switch.States.FIRST_PRESS, sw.getState());
		Assert.assertNull(lastClick);

		loop(110);
		Assert.assertEquals(Switch.States.WAIT_RELEASE, sw.getState());
		Assert.assertNotNull(lastClick);
		Assert.assertEquals(EventType.LONG_CLICK, lastClick);
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
		Assert.assertEquals(EventType.DOUBLE_CLICK, lastClick);

		hw.inval = false;
		loop();
		Assert.assertEquals(Switch.States.REST, sw.getState());
	}
}
