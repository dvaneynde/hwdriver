package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
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
	private IDomoticContext ctx;
	private long current, seq;

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		lamp = new Lamp("TestLamp", "TestLamp", LAMP_OUT, ctx);
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", FAN_OUT, ctx).overrideDelayOnDurationSec(5).overrideDelayOffDurationSec(5)
				.overrideOnDurationSec(10);
		lamp.registerListener(new Connector(EventType.ON, fan, EventType.DELAY_ON, "LampOn2FanDelayOn"));
		lamp.registerListener(new Connector(EventType.OFF, fan, EventType.DELAY_OFF, "LampOff2FanDelayOff"));
		current = seq = 0L;
	}

	private void assertOff() {
		Assert.assertEquals(Fan.States.OFF, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(!hw.fanStatus);
	}

	private void assertOn() {
		Assert.assertEquals(Fan.States.ON, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.fanStatus);
	}

	private void assertDelayedOn_LampOn() {
		Assert.assertEquals(Fan.States.DELAYED_ON, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
	}

	private void assertOn_LampOn() {
		Assert.assertEquals(Fan.States.ON_D, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.lampStatus && hw.fanStatus);
	}

	private void assertDelayedOff_LampOff() {
		Assert.assertEquals(Fan.States.DELAYED_OFF, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
	}

	@Test
	public void manuallyTurnOnAndOffFan() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Switch on, manually,
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertOn();
		fan.loop(fan.getOnDurationSec() * 1000 - 20, seq++);
		assertOn();
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertOff();
	}

	@Test
	public void manuallyTurnOnFanAndLetItTimeout() {
		fan.loop(current, seq++);
		assertOff();
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertOn();
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10), seq++);
		assertOff();
		// Make sure it does not go on again...
		fan.loop(current += 10, seq++);
		fan.loop(current += fan.getDelayOnDurationSec() * 1000, seq++);
		fan.loop(current += 10, seq++);
		assertOff();
	}

	@Test
	public void lampLongEnoughOnToLetFanRun() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOnDurationSec() * 1000 + 10), seq++);
		assertOn_LampOn();
		// Turn off lamp, fan must still run
		lamp.off();
		fan.loop(current += 10, seq++);
		assertDelayedOff_LampOff();
		// Now wait until fan should have stopped
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10), seq++);
		assertOff();
	}

	/* added after bug, when fan went on after lamp was off again */
	@Test
	public void lampNotLongEnoughOnForFanToRun() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Wait just before end of delay period, fan must still not run
		fan.loop(current += (fan.getDelayOnDurationSec() - 20), seq++);
		assertDelayedOn_LampOn();
		// Turn off lamp, fan must not run
		lamp.off();
		fan.loop(current += 10, seq++);
		assertOff();
		// Now wait for running period, should still not run (of course not, but
		// this was a bug)
		fan.loop(current += (fan.getOnDurationSec() + 10), seq++);
		assertOff();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampOn() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOnDurationSec() * 1000 + 10), seq++);
		assertOn_LampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Now set lamp off, should go to OFF
		lamp.off();
		fan.loop(current += 10, seq++);
		assertOff();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampAlreadyOff() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOnDurationSec() * 1000 + 10), seq++);
		assertOn_LampOn();
		// Lamp off
		lamp.off();
		fan.loop(current += 10, seq++);
		assertDelayedOff_LampOff();
		// Toggle off
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertOff();
	}

	@Test
	public void toggleToOnWhenInDelayedOn() {
		fan.loop(current, seq++);
		assertOff();
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Toggle, must go to on
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertOn_LampOn();
	}

	@Test
	public void stopFanManuallyViaLongToggleWhileRunningWithLampOn() {
		fan.loop(current, seq++);
		assertOff();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.on();
		fan.loop(current += 10, seq++);
		assertDelayedOn_LampOn();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayOnDurationSec() * 1000 + 10), seq++);
		assertOn_LampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.reallyOff();
		fan.loop(current += 10, seq++);
		assertOff();
	}

}
