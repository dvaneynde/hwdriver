package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.sensors.LightSensor;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestLampWithLightSensor {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean lampStatus;
		public int level;

		@Override
		public int readAnalogInput(String channel) throws IllegalArgumentException {
			Assert.assertTrue(channel.equals(LIGHT_IN));
			return level;
		}

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
	private static final String LIGHT_IN = Integer.toString(1);
	private Lamp lamp;
	private LightSensor lightSensor;
	private Hardware hw;
	private DomoticMock dom;
	private long current;

	@Before
	public void init() {
		hw = new Hardware();
		dom = new DomoticMock();
		lamp = new Lamp("TestLamp", "TestLamp", false, LAMP_OUT, hw, dom);
		lightSensor = new LightSensor("TestLightSensor", "", "", LIGHT_IN, hw, dom, 2000, 2, 3);
		Connector c0 = new Connector(EventType.LIGHT_HIGH, lamp, EventType.OFF, "HIGH_to_OFF");
		Connector c1 = new Connector(EventType.LIGHT_LOW, lamp, EventType.ON, "LOW_to_ON");
		lightSensor.registerListener(c0);
		lightSensor.registerListener(c1);
		current = -500L;
	}

	private void assertLampOn() {
		Assert.assertTrue("lamp should be ON", lamp.isOn());
		Assert.assertEquals(Lamp.States.ON, lamp.getState());
		Assert.assertEquals(true, hw.lampStatus);
	}

	private void assertLampOff() {
		Assert.assertFalse("lamp should be OFF", lamp.isOn());
		Assert.assertEquals(Lamp.States.OFF, lamp.getState());
		Assert.assertEquals(false, hw.lampStatus);
	}

	private void loop() {
		loop(1);
	}

	private void loop(int n) {
		for (int i = 0; i < n; i++) {
			current += 500;
			lightSensor.loop(current);
			lamp.loop(current);
		}
	}

	@Test
	public void lampOffOnInitWhenSunny() {
		Assert.assertFalse(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		assertLampOff();

		hw.level = 2100;
		loop();
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();
		loop(4);
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();
	}

	@Test
	public void lampOnInDarkThenOffWhenSunnyThenOffAgainBecauseOfEvening() {
		Assert.assertFalse(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		assertLampOff();

		// donker, dus licht moet aangaan na high2low delay = 3 sec.
		hw.level = 1900;
		loop();
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();
		loop(6);
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();
		loop();
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();

		// licht, maar duurt 2 seconden + sample om lamp te doen uitgaan
		hw.level = 3000;
		loop();
		assertLampOn();
		Assert.assertEquals(LightSensor.States.LOW2HIGH_DELAY, lightSensor.getState());
		loop(4);
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();
		loop();
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();

		// terug donker, duurt 3 seconden + 1 sample
		hw.level = 1000;
		loop();
		Assert.assertEquals(LightSensor.States.HIGH2LOW_DELAY, lightSensor.getState());
		assertLampOff();
		loop(4);
		// na 2 seconden nog niks
		Assert.assertEquals(LightSensor.States.HIGH2LOW_DELAY, lightSensor.getState());
		assertLampOff();
		// nog 1 seconde erbij
		loop(2);
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();

	}

}
