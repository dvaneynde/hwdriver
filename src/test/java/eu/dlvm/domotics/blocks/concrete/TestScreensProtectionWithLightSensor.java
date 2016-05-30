package eu.dlvm.domotics.blocks.concrete;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.connectors.ThresholdEvent2Screen;
import eu.dlvm.domotics.sensors.IAlarmListener;
import eu.dlvm.domotics.sensors.IThresholdListener;

public class TestScreensProtectionWithLightSensor extends TestScreensProtection {

	private ThresholdEvent2Screen threshold2Screen;

	@Before
	public void init() {
		super.init();
		threshold2Screen = new ThresholdEvent2Screen("testLight", null);
		threshold2Screen.registerListener(super.sr);
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

		sr.down();
		loop();
		assertOpenInRestProtected();			
		
		alarmEvent2Screen.onEvent(null, IAlarmListener.EventType.SAFE);
		loop();
		assertRestAndOpen();	
				
		threshold2Screen.onEvent(null, IThresholdListener.EventType.HIGH);
		loop();
		assertDown();	
	}
}
