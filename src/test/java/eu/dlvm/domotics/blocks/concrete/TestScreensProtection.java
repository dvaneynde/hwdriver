package eu.dlvm.domotics.blocks.concrete;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.connectors.AlarmEvent2Screen;
import eu.dlvm.domotics.connectors.ThresholdEvent2Screen;
import eu.dlvm.domotics.sensors.IAlarmListener;
import eu.dlvm.domotics.sensors.IThresholdListener;
import junit.framework.Assert;

// TODO vervangen door TestScreenController
@Deprecated
public class TestScreensProtection extends TestScreensBase {

	private AlarmEvent2Screen alarmEvent2Screen;
	private ThresholdEvent2Screen threshold2Screen;

	@Before
	public void init() {
		super.init();
		alarmEvent2Screen = new AlarmEvent2Screen("testAlarm", null);
		alarmEvent2Screen.registerListener(super.sr);
		threshold2Screen = new ThresholdEvent2Screen("testLight", null);
		threshold2Screen.registerListener(super.sr);
	}

	@Test
	public void upAtRestAndProtect() {
		// rest state
		loop();
		assertRestAndOpen();

		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.ALARM);
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
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.ALARM);
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
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.SAFE);
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

		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.ALARM);
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
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.ALARM);
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

		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
		loop();
		assertDown();	
		loop(sr.getMotorDnPeriod() * 1000L+10);
		assertRestAndClosed();
		loop(10);
		assertRestAndClosed();
		
		threshold2Screen.onEvent(null, IThresholdListener.EventType.LOW);
		loop();
		assertUp();
		loop(sr.getMotorUpPeriod() * 1000L+100);
		assertRestAndOpen();
	}
	
	@Test
	public void firstSunnyAndDown_thenProtectAndUp_thenSunnyButNoEffect() {
		loop();
		assertRestAndOpen();

		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
		loop();
		assertDown();	
		loop(sr.getMotorDnPeriod() * 1000L/2);
		assertDown();
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.ALARM);
		loop();
		Assert.assertEquals(Screen.States.DELAY_DOWN_2_UP, sr.getState());
		Assert.assertFalse(hw.dnRelais);
		Assert.assertFalse(hw.upRelais);
		Assert.assertEquals(true, sr.getProtect());
		loop(Screen.MOTOR_SWITCH_DELAY_PROTECTION+10);	
		assertUpProtected();	
		
		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
		loop();
		assertUpProtected();	
		loop(sr.getMotorUpPeriod() * 1000L+10);
		assertOpenInRestProtected();	
		
		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
		loop();
		assertOpenInRestProtected();			

		threshold2Screen.onEvent(null, IThresholdListener.EventType.LOW);
		loop();
		assertOpenInRestProtected();			

		sr.toggleDown();
		loop();
		assertOpenInRestProtected();			
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.SAFE);
		loop();
		assertRestAndOpen();	
				
		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
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
