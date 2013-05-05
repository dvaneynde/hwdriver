package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;
import eu.dlvm.domotica.blocks.BaseHardwareMock;
import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.concrete.Fan;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestFanWithLamp {
	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean lampStatus;
		public boolean fanStatus;

		@Override
		public void writeDigitalOutput(LogCh ch, boolean value)
				throws IllegalArgumentException {
			if (ch.nr() == FAN_OUT.nr()) {
				fanStatus = value;
			} else if (ch.nr() == LAMP_OUT.nr()) {
				lampStatus = value;
			} else {
				Assert.fail();
			}
		}
	};

	private static final LogCh FAN_OUT = new LogCh(10);
	private static final LogCh LAMP_OUT = new LogCh(11);
	private Fan fan;
	private Lamp lamp;
	private Hardware hw;
	private IDomoContext ctx;
	private long current, seq;

	@BeforeClass
	public static void initLog() {
		BasicConfigurator.configure();
	}

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		lamp = new Lamp("TestLamp", "TestLamp", LAMP_OUT, ctx);
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", lamp, FAN_OUT, ctx);
		fan.setDelayPeriodSec(5);
		fan.setRunningPeriodSec(10);
		current = seq = 0L;
	}

	private void assertRest() {
		Assert.assertEquals(Fan.States.REST, fan.getState());
		Assert.assertFalse(fan.isRunning());
		Assert.assertTrue(!hw.fanStatus);
	}

	private void assertRun() {
		Assert.assertEquals(Fan.States.RUN, fan.getState());
		Assert.assertTrue(fan.isRunning());
		Assert.assertTrue(hw.fanStatus);
	}

	private void assertDelayedRun() {
		Assert.assertEquals(Fan.States.DELAYED_LAMP_ON, fan.getState());
		Assert.assertFalse(fan.isRunning());
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
	}

	private void assertRunLampOn() {
		Assert.assertEquals(Fan.States.RUN_LAMP_ON, fan.getState());
		Assert.assertTrue(fan.isRunning());
		Assert.assertTrue(hw.lampStatus && hw.fanStatus);
	}

	private void assertRunLampOff() {
		Assert.assertEquals(Fan.States.RUN_LAMP_OFF, fan.getState());
		Assert.assertTrue(fan.isRunning());
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
	}

	private void assertWaitLampOff() {
		Assert.assertEquals(Fan.States.WAIT_LAMP_OFF, fan.getState());
		Assert.assertTrue(!fan.isRunning());
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
	}

	@Test
	public void manuallyTurnOnAndOffFan() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Switch on, manually,
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRun();
		fan.loop(fan.getRunningPeriodSec() * 1000 - 20, seq++);
		assertRun();
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRest();
	}

	@Test
	public void manuallyTurnOnFanAndLetItTimeout() {
		fan.loop(current, seq++);
		assertRest();
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRun();
		fan.loop(current += (fan.getRunningPeriodSec() * 1000 + 10), seq++);
		assertRest();
		// Make sure it does not go on again...
		fan.loop(current += 10, seq++);
		fan.loop(current += fan.getDelayPeriodSec() * 1000, seq++);
		fan.loop(current += 10, seq++);
		assertRest();
	}

	@Test
	public void lampLongEnoughOnToLetFanRun() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayPeriodSec() * 1000 + 10), seq++);
		assertRunLampOn();
		// Turn off lamp, fan must still run
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRunLampOff();
		// Now wait until fan should have stopped
		fan.loop(current += (fan.getRunningPeriodSec() * 1000 + 10), seq++);
		assertRest();
	}

	/* added after bug, when fan went on after lamp was off again */
	@Test
	public void lampNotLongEnoughOnForFanToRun() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Wait just before end of delay period, fan must still not run
		fan.loop(current += (fan.getDelayPeriodSec() - 20), seq++);
		assertDelayedRun();
		// Turn off lamp, fan must not run
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRest();
		// Now wait for running period, should still not run (of course not, but
		// this was a bug)
		fan.loop(current += (fan.getRunningPeriodSec() + 10), seq++);
		assertRest();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampOn() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayPeriodSec()*1000 + 10), seq++);
		assertRunLampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Now set lamp off, should go to REST
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRest();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampAlreadyOff() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayPeriodSec()*1000 + 10), seq++);
		assertRunLampOn();
		// Lamp off
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRunLampOff();
		// Toggle off
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRest();
	}

	@Test
	public void toggleToRunWhenInDelayedRun() {
		fan.loop(current, seq++);
		assertRest();
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Toggle, must go to on
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRun();
	}

	@Test
	public void stopFanManuallyViaLongToggleWhileRunningWithLampOn() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayPeriodSec()*1000 + 10), seq++);
		assertRunLampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.turnOffUntilLampOff();
		fan.loop(current += 10, seq++);
		assertWaitLampOff();
		// Now set lamp off, should go to REST
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRest();
	}

	@Test
	public void stopFanManuallyViaLongToggleWhileRunningWithLampOn_ThenShortToggle() {
		fan.loop(current, seq++);
		assertRest();
		fan.loop(current += 10, seq++);
		// Lamp on
		lamp.setOn(true);
		fan.loop(current += 10, seq++);
		assertDelayedRun();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayPeriodSec()*1000 + 10), seq++);
		assertRunLampOn();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.turnOffUntilLampOff();
		fan.loop(current += 10, seq++);
		assertWaitLampOff();
		// Now toggle short
		fan.toggle();
		lamp.setOn(false);
		fan.loop(current += 10, seq++);
		assertRun();
	}

}
