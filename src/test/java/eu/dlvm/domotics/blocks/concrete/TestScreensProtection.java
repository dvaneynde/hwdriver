package eu.dlvm.domotics.blocks.concrete;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;

public class TestScreensProtection extends TestScreensBase {

	@Before
	public void init() {
		super.init();
	}

	@Test
	public void upAtRestAndProtect() {
		// rest state
		loop();
		assertOpenAndRest();

		sr.setProtect(true);
		loop();
		assertUpProtected();

		loop(sr.getMotorUpPeriod() * 1000L + 10);
		assertOpenInRestProtected();
	}

	@Test
	public void firstDownAndThenProtect_WhileUpDoDownOrUp_ThenUnprotectAndDown() {
		loop();
		assertOpenAndRest();

		sr.down();
		loop();
		assertDown();
		
		loop(sr.getMotorDnPeriod() * 1000L + 10);
		assertRestAndClosed();
		
		sr.setProtect(true);
		loop();
		assertUpProtected();

		loop(sr.getMotorUpPeriod() * 1000L + 10);
		assertOpenInRestProtected();
		
		sr.down();
		loop();
		assertOpenInRestProtected();
		
		sr.up();
		loop();
		assertOpenInRestProtected();
		
		sr.setProtect(false);
		loop();
		sr.down();
		loop();
		assertDown();
	}

	@Test
	public void goDownThenHalfWayProtect_ShouldGoUpAfterDelay() {
		loop();
		assertOpenAndRest();

		sr.down();
		loop();
		assertDown();
		
		loop(sr.getMotorDnPeriod() * 1000L/2);
		assertDown();

		sr.setProtect(true);
		loop();
		Assert.assertEquals(Screen.States.DELAY_DOWN_2_UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue( sr.getProtect());
		loop();
		Assert.assertEquals(Screen.States.DELAY_DOWN_2_UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertTrue( sr.getProtect());

		loop(Screen.MOTOR_SWITCH_DELAY_PROTECTION+10);
		assertUpProtected();
		loop();
		assertUpProtected();
		
		loop(sr.getMotorUpPeriod() * 1000L+10);
		assertOpenInRestProtected();
		loop();
		assertOpenInRestProtected();		
	}
	
	@Test
	public void goDownAndThenUpAndHalfWayProtect_ShouldContinueGoingUp() {
		loop();
		assertOpenAndRest();
		sr.down();
		loop();
		assertDown();		
		loop(sr.getMotorDnPeriod() * 1000L + 10);
		assertRestAndClosed();
		sr.up();
		loop();
		assertUp();	
		loop(sr.getMotorUpPeriod() * 1000L/2);
		assertUp();	
		
		sr.setProtect(true);
		loop();
		assertUpProtected();	
		
		loop(sr.getMotorUpPeriod()*1000L/2+10);
		assertOpenInRestProtected();
		loop();
		assertOpenInRestProtected();			
	}
	
	private void assertOpenAndRest() {
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(0.0, sr.getRatioClosed());
		Assert.assertEquals(false, sr.getProtect());
	}
	private void assertRestAndClosed() {
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(1.0, sr.getRatioClosed());
		Assert.assertEquals(false, sr.getProtect());
	}
	private void assertDown() {
		Assert.assertEquals(Screen.States.DOWN, sr.getState());
		Assert.assertTrue(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertFalse(sr.getProtect());
	}
	private void assertUp() {
		Assert.assertEquals(Screen.States.UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertTrue(hw.upRelais);
		Assert.assertFalse(sr.getProtect());
	}
	private void assertUpProtected() {
		Assert.assertEquals(Screen.States.UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertTrue(hw.upRelais);
		Assert.assertTrue(sr.getProtect());
	}
	private void assertOpenInRestProtected() {
		Assert.assertEquals(Screen.States.REST_PROTECT, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(true, sr.getProtect());
		Assert.assertEquals(0.0, sr.getRatioClosed());
	}
}
