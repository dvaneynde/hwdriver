package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
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
	private IDomoticBuilder dom;
	private long current;

	@Before
	public void init() {
		hw = new Hardware();
		dom = new DomoticMock();
		lamp = new Lamp("TestLamp", "TestLamp", true, LAMP_OUT, hw, dom);
		current = 0L;
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

		lamp.loop(current += 10);
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOn();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOff();

		lamp.on();
		lamp.loop(current += 10);
		assertLampOn();

		lamp.on();
		lamp.loop(current += 10);
		assertLampOn();

		lamp.off();
		lamp.loop(current += 10);
		assertLampOff();

		lamp.off();
		lamp.loop(current += 10);
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
		lamp.loop(current += 10);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000 - 30));
		assertLampOn();
		lamp.loop(current += 50);
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
		lamp.loop(current += 10);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000 - 30));
		assertLampOn();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOn();
		lamp.loop(current += (lamp.getAutoOffSec() * 1000 + 10));
		assertLampOff();
	}

	@Test
	public void autoOffWithDefaultBlinksTest() {
		lamp.setEco(true);
		lamp.setBlink(true);
		Assert.assertTrue(lamp.isEco());
		Assert.assertTrue(lamp.isBlink());
		Assert.assertEquals(1, lamp.getBlinks());
		Assert.assertEquals(Lamp.DEFAULT_AUTO_OFF_SEC, lamp.getAutoOffSec());
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000));
		assertLampOn();

		// blink off
		lamp.loop(current += 10);
		assertLampBlink(false);

		// graceperiod
		lamp.loop(current += Lamp.BLINK_TIME_MS);
		assertLampGoingOffUnlessInterrupted();
		lamp.loop(current += 10);
		assertLampGoingOffUnlessInterrupted();

		// off
		lamp.loop(current += 5000);
		assertLampOff();
	}

	@Test
	public void autoOffWith2BlinksTest() {
		lamp.setEco(true);
		lamp.setBlink(true);
		lamp.setBlinks(2);
		Assert.assertTrue(lamp.isEco());
		Assert.assertTrue(lamp.isBlink());
		Assert.assertEquals(2, lamp.getBlinks());
		Assert.assertEquals(Lamp.DEFAULT_AUTO_OFF_SEC, lamp.getAutoOffSec());
		assertLampOff();

		lamp.toggle();
		lamp.loop(current += 10);
		assertLampOn();

		lamp.loop(current += (lamp.getAutoOffSec() * 1000));
		assertLampOn();

		// blink off
		lamp.loop(current += 10);
		assertLampBlink(false);
		// blink on
		lamp.loop(current += Lamp.BLINK_TIME_MS);
		assertLampBlink(true);
		// blink off
		lamp.loop(current += Lamp.BLINK_TIME_MS);
		assertLampBlink(false);

		// graceperiod
		lamp.loop(current += Lamp.BLINK_TIME_MS);
		assertLampGoingOffUnlessInterrupted();
		lamp.loop(current += 10);
		assertLampGoingOffUnlessInterrupted();

		// off
		lamp.loop(current += 5000);
		assertLampOff();
	}
}
