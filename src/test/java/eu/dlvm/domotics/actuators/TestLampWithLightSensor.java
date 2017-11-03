package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
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
	private IDomoticContext ctx;
	private long current, seq;

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		lamp = new Lamp("TestLamp", "TestLamp", LAMP_OUT, ctx);
		lightSensor = new LightSensor("TestLightSensor", "", "", LIGHT_IN, ctx, 2000, 2, 3);
		Connector c0 = new Connector(EventType.LIGHT_HIGH, lamp, EventType.OFF, "HIGH_to_OFF");
		Connector c1 = new Connector(EventType.LIGHT_LOW, lamp, EventType.ON, "LOW_to_ON");
		lightSensor.registerListener(c0);
		lightSensor.registerListener(c1);
		current = seq = 0L;
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
		current += 500;
		seq++;
		lightSensor.loop(current, seq);
		lamp.loop(current, seq);
	}

	@Test
	public void lampOffOnInitWhenSunny() {
		Assert.assertFalse(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		assertLampOff();

		hw.level = 2100;
		loop();
		loop();
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();
		loop();
		Assert.assertEquals(LightSensor.States.HIGH, lightSensor.getState());
		assertLampOff();
	}

	@Test
	public void lampOnInDarkThenOffWhenSunnyThenOffAgainBecauseOfEvening() {
		Assert.assertFalse(lamp.isEco());
		Assert.assertFalse(lamp.isBlink());
		assertLampOff();

		// donker, dus licht moet aangaan na warmup periode van 1 seconde + 1e sample
		hw.level = 1000;
		loop();
		assertLampOff();
		loop();
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOff();
		loop();
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();

		// licht, maar duurt 2 seconden + sample om lamp te doen uitgaan
		hw.level = 3000;
		loop();
		assertLampOn();
		Assert.assertEquals(LightSensor.States.LOW2HIGH_DELAY, lightSensor.getState());
		for (int i = 0; i < 4; i++)
			loop();
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
		for (int i = 0; i < 4; i++)
			loop();
		// na 2 seconden nog niks
		Assert.assertEquals(LightSensor.States.HIGH2LOW_DELAY, lightSensor.getState());
		assertLampOff();
		// nog 1 seconde erbij
		loop();
		loop();
		Assert.assertEquals(LightSensor.States.LOW, lightSensor.getState());
		assertLampOn();

	}

}
