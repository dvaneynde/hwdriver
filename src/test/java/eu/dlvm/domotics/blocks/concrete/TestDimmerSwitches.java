package eu.dlvm.domotics.blocks.concrete;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.ISensorListener;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.DimmerSwitches;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestDimmerSwitches {
	public class Hardware extends BaseHardwareMock implements IHardwareIO{
		public boolean inLeft, inRight;

		@Override
		public boolean readDigitalInput(LogCh channel) {
			return (channel.id().equals("0") ? inLeft : inRight);
		}
	};

	private Hardware hw = new Hardware();
	private IHardwareAccess ctx = new DomoContextMock(hw);
	private long seq, cur;
	private DimmerSwitches sw;
	private SensorEvent lastEvent;
	private ISensorListener sensorListener = new ISensorListener() {
		@Override
		public void notify(SensorEvent e) {
			lastEvent = e;
		}
	};

	@Before
	public void init() {
		sw = new DimmerSwitches("TestDimmerSwitches", "Unit Test DimmerSwitches", new LogCh(0), new LogCh(1), ctx);
		sw.registerListenerDeprecated(sensorListener);
		seq = (cur = 0L);
	}

	private void loop(long inc) {
		cur += inc;
		sw.loop(cur, seq++);
	}

	private void loop() {
		loop(10);
	}

	@Test
	public void singleClickLeft() throws InterruptedException {
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNull(lastEvent);
		hw.inLeft = true;
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, sw.getState());
		Assert.assertNull(lastEvent);

		loop(sw.getClickedTimeoutMS() - 30);	// TODO why is 20 not enough???
		loop();
		
		hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.LEFT_CLICK, lastEvent.getEvent());
	}

	@Test
	public void singleClickRight() throws InterruptedException {
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNull(lastEvent);
		hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, sw.getState());
		Assert.assertNull(lastEvent);

		loop(sw.getClickedTimeoutMS() - 30);	// TODO why is 20 not enough???
		loop();
		
		hw.inRight = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.RIGHT_CLICK, lastEvent.getEvent());
	}

	@Test
	public void longClickLeft() throws InterruptedException {
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNull(lastEvent);
		hw.inLeft = true;
loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, sw.getState());
		Assert.assertNull(lastEvent);

		loop(sw.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.LEFT_HOLD_DOWN, lastEvent.getEvent());

		loop();
		hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.LEFT_RELEASED, lastEvent.getEvent());
	}

	@Test
	public void longClickRight() throws InterruptedException {
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNull(lastEvent);
		hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, sw.getState());
		Assert.assertNull(lastEvent);

		loop(sw.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.RIGHT_HOLD_DOWN, lastEvent.getEvent());

		loop();
		hw.inRight = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(DimmerSwitches.ClickType.RIGHT_RELEASED, lastEvent.getEvent());
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
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertNull(lastEvent);
		if (leftFirst)
			hw.inLeft = true;
		else
			hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, sw.getState());
		Assert.assertNull(lastEvent);

		loop(sw.getClickedTimeoutMS() + 10);
		loop();
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(leftFirst ? DimmerSwitches.ClickType.LEFT_HOLD_DOWN : DimmerSwitches.ClickType.RIGHT_HOLD_DOWN,
				lastEvent.getEvent());

		loop();
		if (!leftFirst)
			hw.inLeft = true;
		else
			hw.inRight = true;
		loop();
		Assert.assertEquals(DimmerSwitches.States.BOTH_DOWN, sw.getState());
		Assert.assertNotNull(lastEvent);
		Assert.assertEquals(leftFirst ? DimmerSwitches.ClickType.LEFT_WITH_RIGHTCLICK
				: DimmerSwitches.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent.getEvent());

		loop();
		if (releaseRightFirst)
			hw.inRight = false;
		else
			hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.BOTH_DOWN, sw.getState());
		Assert.assertEquals(leftFirst ? DimmerSwitches.ClickType.LEFT_WITH_RIGHTCLICK
				: DimmerSwitches.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent.getEvent());
		loop();
		if (!releaseRightFirst)
			hw.inRight = false;
		else
			hw.inLeft = false;
		loop();
		Assert.assertEquals(DimmerSwitches.States.REST, sw.getState());
		Assert.assertEquals(leftFirst ? DimmerSwitches.ClickType.LEFT_WITH_RIGHTCLICK
				: DimmerSwitches.ClickType.RIGHT_WITH_LEFTCLICK, lastEvent.getEvent());
	}

}
