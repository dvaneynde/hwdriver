package eu.dlvm.domotics.connectors;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestSwitch2Fans {

	private final int LAMP1_OUT = 10;
	private final int LAMP2_OUT = 11;
	private final int FAN1_OUT = 3;
	private final int FAN2_OUT = 4;

	public static class TestHardware extends BaseHardwareMock implements IHardwareIO {
		private Map<String, Boolean> inputs = new HashMap<String, Boolean>();
		private Map<String, Boolean> outputs = new HashMap<String, Boolean>();

		@Override
		public void writeDigitalOutput(String channel, boolean value) throws IllegalArgumentException {
			outputs.put(channel, value);
		}

		@Override
		public boolean readDigitalInput(String channel) {
			return inputs.get(channel);
		}

		public boolean out(int ch) {
			Boolean b = outputs.get(Integer.toString(ch));
			return b.booleanValue();
		}

		public void in(int ch, boolean val) {
			inputs.put(Integer.toString(ch), val);
		}

		public boolean in(int ch) {
			return inputs.get(Integer.toString(ch));
		}
	};

	private TestHardware hw;
	private Domotic dom;
	private Switch sw1, sw2;
	private Lamp l1, l2;
	private Fan f1, f2;
	private long cur;

	@Before
	public void init() {
		cur = 0L;

		hw = new TestHardware();
		hw.in(0, false);
		hw.in(1, false);

		dom = Domotic.createSingleton(hw);
		sw1 = new Switch("Switch1", "Switch1", Integer.toString(0), hw, dom);
		sw2 = new Switch("Switch2", "Switch2", Integer.toString(1), hw, dom);
		l1 = new Lamp("Lamp1", "Lamp1", false, Integer.toString(LAMP1_OUT), hw, dom);
		l2 = new Lamp("Lamp2", "Lamp2", false, Integer.toString(LAMP2_OUT), hw, dom);
		f1 = new Fan("Fan1", "Fan1", Integer.toString(FAN1_OUT), hw, dom);
		l1.registerListener(new Connector(EventType.ON, f1, EventType.DELAY_ON, "Test_Lamp"));
		l1.registerListener(new Connector(EventType.OFF, f1, EventType.DELAY_OFF, "Test_Lamp"));
		f2 = new Fan("Fan2", "Fan2", Integer.toString(FAN2_OUT), hw, dom);
		l2.registerListener(new Connector(EventType.ON, f2, EventType.DELAY_ON, "Test_Lamp"));
		l2.registerListener(new Connector(EventType.OFF, f2, EventType.DELAY_OFF, "Test_Lamp"));

		//Switch2Fan s2f1 = new Switch2Fan("s2f1", "s2f1");
		Connector s2f1_single = new Connector(EventType.SINGLE_CLICK, f1, EventType.TOGGLE, "Test_Switch2");
		sw1.registerListener(s2f1_single);
		
		//Switch2Fan s2f2 = new Switch2Fan("s2f2", "s2f2");
		Connector s2f2_single = new Connector(EventType.SINGLE_CLICK, f2, EventType.TOGGLE, "Test_Switch2");
		sw2.registerListener(s2f2_single);

		// TODO long click to stop until lamp off
		
		dom.initialize(new HashMap<String, RememberedOutput>(0));
	}

	@Test
	public void singleClick() throws InterruptedException {
		// switch 1, single click only
		sw2.setSingleClickEnabled(true);
		sw2.setDoubleClickEnabled(false);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		Assert.assertEquals(false, hw.out(FAN1_OUT));
		Assert.assertEquals(false, hw.out(FAN2_OUT));
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(FAN1_OUT));
		Assert.assertEquals(false, hw.out(FAN2_OUT));

		hw.in(0, true);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(FAN1_OUT));
		Assert.assertEquals(false, hw.out(FAN2_OUT));
		hw.in(0, false);
		dom.loopOnce(cur += 10);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(FAN1_OUT));
		Assert.assertEquals(false, hw.out(FAN2_OUT));

		// switch 2, single click
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(FAN1_OUT));
		Assert.assertEquals(true, hw.out(FAN2_OUT));
	}
}
