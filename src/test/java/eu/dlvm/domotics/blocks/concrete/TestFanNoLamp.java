package eu.dlvm.domotics.blocks.concrete;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;
import junit.framework.Assert;

public class TestFanNoLamp {
	public class Hardware extends BaseHardwareMock implements IHardwareIO{
		public boolean lampStatus;
		public boolean fanStatus;

		@Override
		public void writeDigitalOutput(LogCh ch, boolean value) throws IllegalArgumentException {
			if (ch.id() == FAN_OUT.id()) {
				fanStatus = value;
			} else {
				Assert.fail();
			}
		}
	};

	private static final LogCh FAN_OUT = new LogCh(10);
	private Fan fan;
	private Hardware hw;
	private IDomoticContext ctx;
	private long current, seq;

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", FAN_OUT, ctx);
		fan.setDelayPeriodSec(5000);
		fan.setRunningPeriodSec(10000);
		current = seq = 0L;
	}

	private void assertRest() {
		Assert.assertTrue(!hw.lampStatus && !hw.fanStatus);
		Assert.assertEquals(Fan.States.REST, fan.getState());
		Assert.assertFalse(fan.isOn());
	}

	private void assertRunNoLamp() {
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
		Assert.assertEquals(Fan.States.RUN, fan.getState());
		Assert.assertTrue(fan.isOn());
	}
	
	@SuppressWarnings("unused")
	private void assertRunWithLamp() {
		Assert.assertTrue(hw.fanStatus);
		Assert.assertEquals(Fan.States.RUN, fan.getState());
		Assert.assertTrue(fan.isOn());
	}
	
	@SuppressWarnings("unused")
	private void assertDelayedRun() {
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
		Assert.assertEquals(Fan.States.DELAYED_LAMP_ON, fan.getState());
		Assert.assertFalse(fan.isOn());
	}

	@Ignore("Not implemented yet.")
	@Test
	public void manuallyTurnOnAndOffFan() {
		Assert.assertEquals(0L,current);
		Assert.assertEquals(100L, current+=100);
	}
	
	@Ignore("Not implemented yet.")
	@Test
	public void manuallyTurnOnFanAndLetItTimeout() {
		fan.loop(current,seq++);
		assertRest();
		fan.toggle();
		fan.loop(current += 10, seq++);
		assertRunNoLamp();
		fan.loop(current+=fan.getRunningPeriodSec(),seq++);
		assertRest();
	}
	
	@Ignore("Not implemented yet.")
	@Test
	public void immediateOnAndOff() {
	}
	
}
