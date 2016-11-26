package eu.dlvm.domotics.actuators;

import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import junit.framework.Assert;

public class TestScreens extends TestScreensBase {

	@Test
	public void downUsingToggle() {
		clickAndFullPeriod(true, true);
	}

	@Test
	public void upUsingToggle() {
		clickAndFullPeriod(false, true);
	}

	@Test
	public void downLongTimeUsingToggle() {
		sr.setMotorUpPeriod(30);
		sr.setMotorDnPeriod(30);
		clickAndFullPeriod(true, true);
	}

	@Test
	public void down() {
		clickAndFullPeriod(true, false);
	}

	@Test
	public void up() {
		clickAndFullPeriod(false, false);
	}

	@Test
	public void downLongTime() {
		sr.setMotorUpPeriod(30);
		sr.setMotorDnPeriod(30);
		clickAndFullPeriod(true, false);
	}

	private void clickAndFullPeriod(boolean down, boolean useToggle) {
		// rest state
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// click
		if (down) {
			if (useToggle)
				sr.toggleDown();
			else
				sr.down();
		} else {
			if (useToggle)
				sr.toggleUp();
			else
				sr.up();
		}
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP), sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais && hw.upRelais);
		// TODO verschil down / up
		loop(sr.getMotorDnPeriod() * 1000L + 10);
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

		if (down) {
			sr.toggleDown();
		} else {
			sr.toggleUp();

		}
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP), sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais && hw.upRelais);
		// TODO verschil down / up
		loop(sr.getMotorUpPeriod() * 1000L / 2);
		loop();
		Assert.assertEquals((down ? Screen.States.DOWN : Screen.States.UP), sr.getState());
		Assert.assertTrue(hw.dnRelais == down);
		Assert.assertTrue(hw.upRelais == !down);
		Assert.assertTrue(down ? hw.dnRelais && !hw.upRelais : !hw.dnRelais && hw.upRelais);

		if (down) {
			sr.toggleDown();

		} else {
			sr.toggleUp();
		}

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
			sr.toggleDown();
		else
			sr.toggleUp();
		loop();
		Assert.assertEquals((firstDown ? Screen.States.DOWN : Screen.States.UP), sr.getState());
		Assert.assertTrue(hw.dnRelais == firstDown);
		Assert.assertTrue(hw.upRelais == !firstDown);
		Assert.assertTrue(firstDown ? hw.dnRelais && !hw.upRelais : !hw.dnRelais && hw.upRelais);

		// test halfway
		// TODO verschil down / up
		loop(sr.getMotorUpPeriod() * 1000L / 2);
		Assert.assertEquals((firstDown ? Screen.States.DOWN : Screen.States.UP), sr.getState());
		Assert.assertTrue(hw.dnRelais == firstDown);
		Assert.assertTrue(hw.upRelais == !firstDown);
		Assert.assertTrue(firstDown ? hw.dnRelais && !hw.upRelais : !hw.dnRelais && hw.upRelais);

		// halfway, press other key
		if (firstDown)
			sr.toggleUp();
		else
			sr.toggleDown();
		loop();
		Assert.assertEquals((firstDown ? Screen.States.DELAY_DOWN_2_UP : Screen.States.DELAY_UP_2_DOWN), sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// check motor switch delay protection, must still not have changed
		// direction
		loop(Screen.MOTOR_SWITCH_DELAY_PROTECTION - 20);
		Assert.assertEquals((firstDown ? Screen.States.DELAY_DOWN_2_UP : Screen.States.DELAY_UP_2_DOWN), sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);

		// after motor delay protection, must go in other direction
		loop(40);
		Assert.assertEquals((firstDown ? Screen.States.UP : Screen.States.DOWN), sr.getState());
		Assert.assertTrue(hw.dnRelais == !firstDown);
		Assert.assertTrue(hw.upRelais == firstDown);
		Assert.assertTrue(firstDown ? !hw.dnRelais && hw.upRelais : hw.dnRelais && !hw.upRelais);

		if (firstDown)
			sr.toggleUp();
		else
			sr.toggleDown();
		loop();
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue(!hw.dnRelais && !hw.upRelais);
	}
}
