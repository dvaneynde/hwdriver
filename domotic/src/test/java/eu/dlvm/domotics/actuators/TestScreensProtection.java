package eu.dlvm.domotics.actuators;

import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.events.EventType;
import junit.framework.Assert;

// TODO vervangen door TestScreenController

public class TestScreensProtection extends TestScreensBase {

	@Test
	public void upAtRestAndProtect() {
		// rest state
		loop();
		assertRestAndOpen();

		sr.onEvent(null, EventType.ALARM);
		loop();
		assertUpProtected();

		loop(sr.getMotorUpPeriod() * 1000L + 10);
		assertOpenInRestProtected();
	}
	
	@Test
	public void firstDownAndThenProtect_WhileUpDoDownOrUp_ThenUnprotectAndDown() {
		loop();
		assertRestAndOpen();

		sr.toggleDown();
		loop();
		assertDown();
		
		loop(sr.getMotorDnPeriod() * 1000L + 10);
		assertRestAndClosed();
		
		sr.onEvent(null, EventType.ALARM);
		loop();
		assertUpProtected();

		loop(sr.getMotorUpPeriod() * 1000L + 10);
		assertOpenInRestProtected();
		
		sr.toggleDown();
		loop();
		assertOpenInRestProtected();
		
		sr.toggleUp();
		loop();
		assertOpenInRestProtected();
		
		sr.onEvent(null, EventType.SAFE);
		loop();
		sr.toggleDown();
		loop();
		assertDown();
	}

	@Test
	public void goDownThenHalfWayProtect_ShouldGoUpAfterDelay() {
		loop();
		assertRestAndOpen();

		sr.toggleDown();
		loop();
		assertDown();
		
		loop(sr.getMotorDnPeriod() * 1000L/2);
		assertDown();

		sr.onEvent(null, EventType.ALARM);
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
		assertRestAndOpen();
		sr.toggleDown();
		loop();
		assertDown();		
		loop(sr.getMotorDnPeriod() * 1000L + 10);
		assertRestAndClosed();

		sr.toggleUp();
		loop();
		assertUp();	
		loop(sr.getMotorUpPeriod() * 1000L/2);
		assertUp();	
		
		sr.onEvent(null, EventType.ALARM);
		loop();
		assertUpProtected();	
		
		loop(sr.getMotorUpPeriod()*1000L/2+10);
		assertOpenInRestProtected();
		loop();
		assertOpenInRestProtected();			
	}
	
	@Test
	public void sunnyAndDown_cloudyAndUp() {
		loop();
		assertRestAndOpen();

		sr.onEvent(null, EventType.DOWN);
		loop();
		assertDown();	
		loop(sr.getMotorDnPeriod() * 1000L+10);
		assertRestAndClosed();
		loop(10);
		assertRestAndClosed();
		
		sr.onEvent(null, EventType.UP);
		loop();
		assertUp();
		loop(sr.getMotorUpPeriod() * 1000L+100);
		assertRestAndOpen();
	}
	
	@Test
	public void firstSunnyAndDown_thenProtectAndUp_thenSunnyButNoEffect() {
		loop();
		assertRestAndOpen();

		sr.onEvent(null, EventType.DOWN);
		loop();
		assertDown();	
		loop(sr.getMotorDnPeriod() * 1000L/2);
		assertDown();
		
		sr.onEvent(null, EventType.ALARM);
		loop();
		Assert.assertEquals(Screen.States.DELAY_DOWN_2_UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(true, sr.getProtect());
		loop(Screen.MOTOR_SWITCH_DELAY_PROTECTION+10);	
		assertUpProtected();	
		
		sr.onEvent(null, EventType.DOWN);
		loop();
		assertUpProtected();	
		loop(sr.getMotorUpPeriod() * 1000L+10);
		assertOpenInRestProtected();	
		
		sr.onEvent(null, EventType.DOWN);
		loop();
		assertOpenInRestProtected();			

		sr.onEvent(null, EventType.UP);
		loop();
		assertOpenInRestProtected();			

		sr.toggleDown();
		loop();
		assertOpenInRestProtected();			
		
		sr.onEvent(null, EventType.SAFE);
		loop();
		assertRestAndOpen();	
				
		sr.onEvent(null, EventType.DOWN);
		loop();
		assertDown();	
	}

	/*
	 * Helpers
	 */
	protected void assertRestAndOpen() {
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(0.0, sr.getRatioClosed());
		Assert.assertEquals(false, sr.getProtect());
	}
	protected void assertRestAndClosed() {
		Assert.assertEquals(Screen.States.REST, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(1.0, sr.getRatioClosed());
		Assert.assertEquals(false, sr.getProtect());
	}
	protected void assertDown() {
		Assert.assertEquals(Screen.States.DOWN, sr.getState());
		Assert.assertTrue(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertFalse(sr.getProtect());
	}
	protected void assertUp() {
		Assert.assertEquals(Screen.States.UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertTrue(hw.upRelais);
		Assert.assertFalse(sr.getProtect());
	}
	protected void assertUpProtected() {
		Assert.assertEquals(Screen.States.UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertTrue(hw.upRelais);
		Assert.assertTrue(sr.getProtect());
	}
	protected void assertOpenInRestProtected() {
		Assert.assertEquals(Screen.States.REST_PROTECT, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(true, sr.getProtect());
		Assert.assertEquals(0.0, sr.getRatioClosed());
	}
}
