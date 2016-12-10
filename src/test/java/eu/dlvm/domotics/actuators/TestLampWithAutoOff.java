package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestLampWithAutoOff {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean lampStatus;

		@Override
		public void writeDigitalOutput(String ch, boolean value) throws IllegalArgumentException {
			if (ch.equals(LAMP_OUT)) {
				lampStatus = value;
			} else {
				Assert.fail();
			}
		}
	};

	private static final String LAMP_OUT = Integer.toString(10);
	private Lamp lamp;
	private Hardware hw;
	private IDomoticContext ctx;
	private long current, seq;

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		lamp = new Lamp("TestLamp", "TestLamp", LAMP_OUT, ctx);
		current = seq = 0L;
	}

	private void assertLampOn() {
		Assert.assertTrue(lamp.isOn());
		Assert.assertEquals(Lamp.States.ON, lamp.getState());
		Assert.assertEquals(true, hw.lampStatus);
	}

	private void assertLampOff() {
		Assert.assertFalse(lamp.isOn());
		Assert.assertEquals(Lamp.States.OFF, lamp.getState());
		Assert.assertEquals(false, hw.lampStatus);
	}

	private void assertLampBlink(boolean lampOn) {
		Assert.assertTrue(lamp.isOn());
		Assert.assertEquals(Lamp.States.GOING_OFF_BLINK, lamp.getState());
		Assert.assertEquals(lampOn, hw.lampStatus);
	}

	private void assertLampGoingOffUnlessInterrupted() {
		Assert.assertTrue(lamp.isOn());
		Assert.assertEquals(Lamp.States.GOING_OFF_UNLESS_CLICK, lamp.getState());
		Assert.assertEquals(true, hw.lampStatus);
	}

	@Test
	public void basicLampTest() {
		Assert.assertFalse(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		assertLampOff();

		lamp.loop(current += 10, seq++);
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOff();

		lamp.on();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.on();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.off();
		lamp.loop(current += 10, seq++);
		assertLampOff();

		lamp.off();
		lamp.loop(current += 10, seq++);
		assertLampOff();
	}

	@Test
	public void autoOffTest() {
		lamp.setEco(true);
		Assert.assertTrue(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		Assert.assertEquals(Lamp.DEFAULT_AUTO_OFF_SEC, lamp.getAutoOffSec());
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000 - 30), seq++);
		assertLampOn();
		lamp.loop(current += 50, seq++);
		assertLampOff();
	}

	@Test
	public void autoOffRestartBeforeOffTest() {
		lamp.setEco(true);
		Assert.assertTrue(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		Assert.assertEquals(Lamp.DEFAULT_AUTO_OFF_SEC, lamp.getAutoOffSec());
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000 - 30), seq++);
		assertLampOn();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOn();
		lamp.loop(current += (lamp.getAutoOffSec() * 1000 + 10), seq++);
		assertLampOff();
	}

	@Test
	public void autoOffWithBlinkTest() {
		lamp.setEco(true);
		lamp.setBlink(true);
		Assert.assertTrue(lamp.isEco());
		Assert.assertTrue(lamp.isBlink());
		Assert.assertEquals(Lamp.DEFAULT_AUTO_OFF_SEC, lamp.getAutoOffSec());
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10, seq++);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000), seq++);
		assertLampOn();

		// blink off
		lamp.loop(current += 20, seq++);
		assertLampBlink(false);
		// blink on
		lamp.loop(current += 1010, seq++);
		assertLampBlink(true);
		// blink off
		lamp.loop(current += 1010, seq++);
		assertLampBlink(false);

		// graceperiod
		lamp.loop(current += 1010, seq++);
		assertLampGoingOffUnlessInterrupted();
		lamp.loop(current += 10, seq++);
		assertLampGoingOffUnlessInterrupted();

		// off
		lamp.loop(current += 5010, seq++);
		assertLampOff();
	}
}
