package eu.dlvm.domotics.blocks.concrete;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.IDimmerSwitchListener;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestDimmerSwitches {
	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inLeft, inRight;

		@Override
		public boolean readDigitalInput(String channel) {
			return (channel.equals("0") ? inLeft : inRight);
		}
	};

	private Hardware hw = new Hardware();
	private IDomoticContext ctx = new DomoContextMock(hw);
	private long seq, cur;
	private DimmerSwitch ds;
	private IDimmerSwitchListener.ClickType lastEvent;
	private IDimmerSwitchListener dsListener = new IDimmerSwitchListener() {
		@Override
		public void onEvent(DimmerSwitch source, ClickType click) {
			lastEvent = click;
		}
	};

	@Before
	public void init() {
		ds = new DimmerSwitch("TestDimmerSwitches", "Unit Test DimmerSwitches", Integer.toString(0), Integer.toString(1), ctx);
		lastEvent = null;
		ds.registerListener(dsListener);
		seq = (cur = 0L);
	}

	private void loop(long inc) {
		cur += inc;
		ds.loop(cur, seq++);
	}

	private void loop() {
		loop(10);
	}

	@Test
	public void singleClickLeft() throws InterruptedException {
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNull(lastEvent);
		hw.inLeft = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_SHORT, ds.getState());
		Assert.assertNull(lastEvent);

		loop(ds.getClickedTimeoutMS() - 30); // TODO why is 20 not enough???
		loop();

		hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.LEFT_CLICK, lastEvent);
	}

	@Test
	public void singleClickRight() throws InterruptedException {
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNull(lastEvent);
		hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_SHORT, ds.getState());
		Assert.assertNull(lastEvent);

		loop(ds.getClickedTimeoutMS() - 30); // TODO why is 20 not enough???
		loop();

		hw.inRight = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.RIGHT_CLICK, lastEvent);
	}

	@Test
	public void longClickLeft() throws InterruptedException {
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNull(lastEvent);
		hw.inLeft = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_SHORT, ds.getState());
		Assert.assertNull(lastEvent);

		loop(ds.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_LONG, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.LEFT_HOLD_DOWN, lastEvent);

		loop();
		hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.LEFT_RELEASED, lastEvent);
	}

	@Test
	public void longClickRight() throws InterruptedException {
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNull(lastEvent);
		hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_SHORT, ds.getState());
		Assert.assertNull(lastEvent);

		loop(ds.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_LONG, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.RIGHT_HOLD_DOWN, lastEvent);

		loop();
		hw.inRight = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(IDimmerSwitchListener.ClickType.RIGHT_RELEASED, lastEvent);
	}

	@Test
	public void leftThenRightReleaseLeftFirst() throws InterruptedException {
		_leftAndRight(true, false);
	}

	@Test
	public void leftThenRightReleaseRightFirst() throws InterruptedException {
		_leftAndRight(true, true);
	}

	@Test
	public void rightThenLeftReleaseLeftFirst() throws InterruptedException {
		_leftAndRight(false, false);
	}

	@Test
	public void rightThenLeftReleaseRightFirst() throws InterruptedException {
		_leftAndRight(false, true);
	}

	private void _leftAndRight(boolean leftFirst, boolean releaseRightFirst) throws InterruptedException {
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertNull(lastEvent);
		if (leftFirst)
			hw.inLeft = true;
		else
			hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_SHORT, ds.getState());
		Assert.assertNull(lastEvent);

		loop(ds.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitch.States.DOWN_LONG, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(leftFirst ? IDimmerSwitchListener.ClickType.LEFT_HOLD_DOWN : IDimmerSwitchListener.ClickType.RIGHT_HOLD_DOWN, lastEvent);

		loop();
		if (!leftFirst)
			hw.inLeft = true;
		else
			hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitch.States.BOTH_DOWN, ds.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(leftFirst ? IDimmerSwitchListener.ClickType.LEFT_WITH_RIGHTCLICK : IDimmerSwitchListener.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent);

		loop();
		if (releaseRightFirst)
			hw.inRight = false;
		else
			hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.BOTH_DOWN, ds.getState());
		Assert.assertEquals(leftFirst ? IDimmerSwitchListener.ClickType.LEFT_WITH_RIGHTCLICK : IDimmerSwitchListener.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent);
		loop();
		if (!releaseRightFirst)
			hw.inRight = false;
		else
			hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitch.States.REST, ds.getState());
		Assert.assertEquals(leftFirst ? IDimmerSwitchListener.ClickType.LEFT_WITH_RIGHTCLICK : IDimmerSwitchListener.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent);
	}

}
