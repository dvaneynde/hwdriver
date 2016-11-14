package eu.dlvm.domotics.blocks.concrete;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Fan;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.Switch2Fan;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;
import junit.framework.Assert;

public class TestSwitch2Fans {
	
	private final int LAMP1_OUT = 10;
	private final int LAMP2_OUT = 11;
	private final int FAN1_OUT = 3;
	private final int FAN2_OUT = 4;

	
	public static class TestHardware extends BaseHardwareMock implements IHardwareIO {
		private Map<LogCh, Boolean> inputs = new HashMap<LogCh, Boolean>();
		private Map<LogCh, Boolean> outputs = new HashMap<LogCh, Boolean>();

		@Override
		public void writeDigitalOutput(LogCh channel, boolean value) throws IllegalArgumentException {
			outputs.put(channel, value);
		}

		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inputs.get(channel);
		}

		public boolean out(int ch) {
			Boolean b = outputs.get(new LogCh(ch));
			return b.booleanValue();
		}

		public void in(int ch, boolean val) {
			inputs.put(new LogCh(ch), val);
		}

		public boolean in(int ch) {
			return inputs.get(new LogCh(ch));
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
		
		Domotic.resetSingleton();
		dom = Domotic.createSingleton(hw);
		sw1 = new Switch("Switch1", "Switch1", new LogCh(0), dom);
		sw2 = new Switch("Switch2", "Switch2", new LogCh(1), dom);
		l1 = new Lamp("Lamp1", "Lamp1", new LogCh(LAMP1_OUT), dom);
		l2 = new Lamp("Lamp2", "Lamp2", new LogCh(LAMP2_OUT), dom);
		f1 = new Fan("Fan1", "Fan1", l1, new LogCh(FAN1_OUT), dom);
		f2 = new Fan("Fan2", "Fan2", l2, new LogCh(FAN2_OUT), dom);
		
		
		Switch2Fan s2f1 = new Switch2Fan("s2f1", "s2f1");
		sw1.registerListener(s2f1);
		s2f1.registerListener(f1);
		
		Switch2Fan s2f2 = new Switch2Fan("s2f2", "s2f2");
		sw2.registerListener(s2f2);
		s2f2.registerListener(f2);

		dom.initialize(new HashMap<String, RememberedOutput> (0));
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
		dom.loopOnce(cur+=10);
		Assert.assertEquals(true, hw.out(FAN1_OUT));
		Assert.assertEquals(true, hw.out(FAN2_OUT));
	}
}
