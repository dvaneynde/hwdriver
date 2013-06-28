package eu.dlvm.domotica.blocks.concrete;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.dlvm.domotica.blocks.BaseHardwareMock;
import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.concrete.Fan;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

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
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", FAN_OUT, ctx);
		fan.setDelayPeriodSec(5000);
		fan.setRunningPeriodSec(10000);
		current = seq = 0L;
	}

	private void assertRest() {
		Assert.assertTrue(!hw.lampStatus && !hw.fanStatus);
		Assert.assertEquals(Fan.States.REST, fan.getState());
		Assert.assertFalse(fan.isRunning());
	}

	private void assertRunNoLamp() {
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
		Assert.assertEquals(Fan.States.RUN, fan.getState());
		Assert.assertTrue(fan.isRunning());
	}
	
	@SuppressWarnings("unused")
	private void assertRunWithLamp() {
		Assert.assertTrue(hw.fanStatus);
		Assert.assertEquals(Fan.States.RUN, fan.getState());
		Assert.assertTrue(fan.isRunning());
	}
	
	@SuppressWarnings("unused")
	private void assertDelayedRun() {
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
		Assert.assertEquals(Fan.States.DELAYED_LAMP_ON, fan.getState());
		Assert.assertFalse(fan.isRunning());
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
