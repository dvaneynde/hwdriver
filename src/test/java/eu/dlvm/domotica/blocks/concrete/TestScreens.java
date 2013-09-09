package eu.dlvm.domotica.blocks.concrete;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.BaseHardwareMock;
import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.concrete.Screen;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestScreens {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean dnRelais;
		public boolean upRelais;

		@Override
		public void writeDigitalOutput(LogCh ch, boolean value)
				throws IllegalArgumentException {
			if (ch.id().equals("0"))
				dnRelais = value;
			else
				upRelais = value;
		}
	};

	private static int DN = 0;
	private static int UP = 1;
	private Screen sr;
	private Hardware hw;
	private IDomoContext ctx;
	private long seq, cur;

	private void loop(long inc) {
		cur += inc;
		sr.loop(cur, seq++);
	}

	private void loop() {
		loop(10);
	}

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		sr = new Screen("TestScreens", "TestScreens", new LogCh(DN), new LogCh(
				UP), ctx);
		sr.setMotorOnPeriod(30);
		seq = cur = 0L;
	}

	@Test
	public void down() {
		clickAndFullPeriod(true);
	}

	@Test
	public void up() {
		clickAndFullPeriod(false);
	}

	@Test
	public void downLongTime() {
		sr.setMotorOnPeriod(30);
		clickAndFullPeriod(true);
	}

	private void clickAndFullPeriod(boolean down) {
		// rest state
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// click
		if (down)
			sr.down();
		else
			sr.up();
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais
				&& hw.upRelais);

		loop(sr.getMotorOnPeriod() * 1000L + 10);
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
	}

	@Test
	public void downAndStopHalfway() {
		clickAndStopHalfway(true);
	}

	@Test
	public void upAndStopHalfway() {
		clickAndStopHalfway(false);
	}

	private void clickAndStopHalfway(boolean down) {
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
		loop();

		if (down)
			sr.down();
		else
			sr.up();
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais
				&& hw.upRelais);
		loop(sr.getMotorOnPeriod() * 1000L / 2);
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais
				&& hw.upRelais);

		if (down)
			sr.down();
		else
			sr.up();
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
	}

	@Test
	public void downAndSwitchHalfway() {
		activateAndSwitchHalfway(true);
	}

	@Test
	public void upAndSwitchHalfway() {
		activateAndSwitchHalfway(false);
	}

	private void activateAndSwitchHalfway(boolean firstDown) {
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
		loop();

		// switch pressed, going
		if (firstDown)
			sr.down();
		else
			sr.up();
		loop();
		Assert.assertEquals(
				(firstDown ? Screen.States.DOWN : Screen.States.UP),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == firstDown);
		Assert.assertTrue(hw.upRelais == !firstDown);
		Assert.assertTrue(firstDown ? hw.dnRelais && !hw.upRelais
				: !hw.dnRelais && hw.upRelais);

		// test halfway
		loop(sr.getMotorOnPeriod() * 1000L / 2);
		Assert.assertEquals(
				(firstDown ? Screen.States.DOWN : Screen.States.UP),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == firstDown);
		Assert.assertTrue(hw.upRelais == !firstDown);
		Assert.assertTrue(firstDown ? hw.dnRelais && !hw.upRelais
				: !hw.dnRelais && hw.upRelais);

		// halfway, press other key
		if (firstDown)
			sr.up();
		else
			sr.down();
		loop();
		Assert.assertEquals((firstDown ? Screen.States.SWITCH_DOWN_2_UP
				: Screen.States.SWITCH_UP_2_DOWN), sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// check motor switch delay protection, must still not have changed direction
		loop(Screen.MOTOR_SWITCH_DELAY_PROTECTION - 20);
		Assert.assertEquals((firstDown ? Screen.States.SWITCH_DOWN_2_UP
				: Screen.States.SWITCH_UP_2_DOWN), sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// after motor delay protection, must go in other direction
		loop(40);
		Assert.assertEquals(
				(firstDown ? Screen.States.UP : Screen.States.DOWN),
				sr.getState());
		Assert.assertTrue(hw.dnRelais == !firstDown);
		Assert.assertTrue(hw.upRelais == firstDown);
		Assert.assertTrue(firstDown ? !hw.dnRelais && hw.upRelais : hw.dnRelais
				&& !hw.upRelais);

		if (firstDown)
			sr.up();
		else
			sr.down();
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
	}
}
