package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestFanWithLamp {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean lampStatus;
		public boolean fanStatus;

		@Override
		public void writeDigitalOutput(String ch, boolean value) throws IllegalArgumentException {
			if (ch.equals(FAN_OUT)) {
				fanStatus = value;
			} else if (ch.equals(LAMP_OUT)) {
				lampStatus = value;
			} else {
				Assert.fail();
			}
		}
	};

	private static final String FAN_OUT = Integer.toString(10);
	private static final String LAMP_OUT = Integer.toString(11);
	private Fan fan;
	private Lamp lamp;
	private Hardware hw;
	private DomoticMock dom;
	private long current;

	@Before
	public void init() {
		hw = new Hardware();
		dom = new DomoticMock();
		lamp = new Lamp("TestLamp", "TestLamp", false, LAMP_OUT, hw, dom);
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", FAN_OUT, hw, dom).overrideDelayOff2OnSec(5).overrideDelayOn2OffSec(5)
				.overrideOnDurationSec(10);
		lamp.registerListener(new Connector(EventType.ON, fan, EventType.DELAY_ON, "Test_Lamp"));
		lamp.registerListener(new Connector(EventType.OFF, fan, EventType.DELAY_OFF, "Test_Lamp"));
		current = 0L;
	}

	private void assertOff() {
		Assert.assertEquals(FanStatemachine.States.OFF, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(!hw.fanStatus);
	}

	private void assertOn() {
		Assert.assertEquals(FanStatemachine.States.ON, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.fanStatus);
	}

	private void assertDelayedOn_LampOn() {
		Assert.assertEquals(FanStatemachine.States.OFF_DELAY2ON, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
	}

	private void assertOn_LampOn() {
		Assert.assertEquals(FanStatemachine.States.ON_DELAY, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.lampStatus && hw.fanStatus);
	}

	private void assertDelayedOff_LampOff() {
		Assert.assertEquals(FanStatemachine.States.ON_DELAY2OFF, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
	}

	@Test
	public void manuallyTurnOnAndOffFan() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Switch on, manually,
		fan.toggle();
		fan.loop(current += 10);
		assertOn();
		fan.loop(fan.getOnDurationSec() * 1000 - 20);
		assertOn();
		fan.toggle();
		fan.loop(current += 10);
		assertOff();
	}

	@Test
	public void manuallyTurnOnFanAndLetItTimeout() {
		fan.loop(current);
		assertOff();
		fan.toggle();
		fan.loop(current += 10);
		assertOn();
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10));
		assertOff();
		// Make sure it does not go on again...
		fan.loop(current += 10);
		fan.loop(current += fan.getDelayOff2OnSec() * 1000);
		fan.loop(current += 10);
		assertOff();
	}

	@Test
	public void lampLongEnoughOnToLetFanRun() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOff2OnSec() * 1000 + 10));
		assertOn_LampOn();
		// Turn off lamp, fan must still run
		lamp.off();
		fan.loop(current += 10);
		assertDelayedOff_LampOff();
		// Now wait until fan should have stopped
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10));
		assertOff();
	}

	/* added after bug, when fan went on after lamp was off again */
	@Test
	public void lampNotLongEnoughOnForFanToRun() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Wait just before end of delay period, fan must still not run
		fan.loop(current += (fan.getDelayOff2OnSec() - 20));
		assertDelayedOn_LampOn();
		// Turn off lamp, fan must not run
		lamp.off();
		fan.loop(current += 10);
		assertOff();
		// Now wait for running period, should still not run (of course not, but
		// this was a bug)
		fan.loop(current += (fan.getOnDurationSec() + 10));
		assertOff();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampOn() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOff2OnSec() * 1000 + 10));
		assertOn_LampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.toggle();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Now set lamp off, should go to OFF
		lamp.off();
		fan.loop(current += 10);
		assertOff();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampAlreadyOff() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOff2OnSec() * 1000 + 10));
		assertOn_LampOn();
		// Lamp off
		lamp.off();
		fan.loop(current += 10);
		assertDelayedOff_LampOff();
		// Toggle off
		fan.toggle();
		fan.loop(current += 10);
		assertOff();
	}

	@Test
	public void toggleToOnWhenInDelayedOn() {
		fan.loop(current);
		assertOff();
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Toggle, must go to on
		fan.toggle();
		fan.loop(current += 10);
		assertOn_LampOn();
	}

	@Test
	public void stopFanManuallyViaLongToggleWhileRunningWithLampOn() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOff2OnSec() * 1000 + 10));
		assertOn_LampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.reallyOff();
		fan.loop(current += 10);
		assertOff();
	}

	/*
	 * BUG
	2017-02-26 18:55:35 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Fan 'VentilatorWC0' received delay-off, keep running for 180 sec.
	2017-02-26 18:55:35 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes OFF, toggle() called.
	...
	2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.sensors.Switch - Switch 'SchakLichtWC0' notifies SINGLE click event (seq=30969012).
	2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Fan 'VentilatorWC0' in delay for ON for 180 sec.
	2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes on, on() called.
	2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes ON, toggle() called.
	2017-02-26 18:57:29 [Oscillator] INFO  eu.dlvm.domotics.sensors.Switch - Switch 'SchakLichtWC0' notifies SINGLE click event (seq=30969281).
	2017-02-26 18:57:29 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Lamp goes off before delay period has expired. No fanning.
	2017-02-26 18:57:29 [Oscillator] WARN  eu.dlvm.domotics.actuators.Fan - delayOff ignored, is missing code. status=Fan [onDurationMs=300000, delayToOnDurationMs=180000, delayTo
	OffDurationMs=180000, timeStateEntered=-1, state=OFF] 
	Error: Fan remained running, even if state was off.
	So: when in delay for off (still running) then lamp goes on so goes to delay for on (should not be running, but remains running). Should go to on-after-delay.
	*/
	@Test
	public void bugWhenInDelayToOffAndLightGoesOnMustGoToOnAfterDelay() {
		fan.loop(current);
		assertOff();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOff2OnSec() * 1000 + 10));
		assertOn_LampOn();
		// Lamp off
		lamp.off();
		fan.loop(current += 10);
		assertDelayedOff_LampOff();
		// Lamp on again
		lamp.on();
		fan.loop(current += 10);
		assertOn_LampOn();
	}
}
