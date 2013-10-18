package eu.dlvm.domotica.blocks.concrete;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.BaseHardwareMock;
import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestSwitchBoard {
	public static class Hardware extends BaseHardwareMock implements IHardwareIO {
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

		public void out(int ch, boolean val) {
			outputs.put(new LogCh(ch), val);
		}

		public boolean out(int ch) {
			return outputs.get(new LogCh(ch));
		}

		public void in(int ch, boolean val) {
			inputs.put(new LogCh(ch), val);
		}

		public boolean in(int ch) {
			return inputs.get(new LogCh(ch));
		}
	};

	private Domotic dom;
	private Hardware hw;
	private Switch sw1, sw2;
	private Lamp o1, o2;
	private SwitchBoard ssb;
	private long cur;

	@Before
	public void init() {
		cur = 0L;

		hw = new Hardware();
		hw.in(0, false);
		hw.in(1, false);
		hw.out(10, false);
		hw.out(11, false);

		dom = Domotic.s(hw);
		sw1 = new Switch("Switch1", "Switch1", new LogCh(0), dom);
		sw2 = new Switch("Switch2", "Switch2", new LogCh(1), dom);
		o1 = new Lamp("Lamp1", "Lamp1", new LogCh(10), dom);
		o2 = new Lamp("Lamp2", "Lamp2", new LogCh(11), dom);
		ssb = new SwitchBoard("ssb", "ssb");
		ssb.add(sw1, o1);
		ssb.add(sw2, o2, true, true);
	}

	@Test
	public void singleClick() throws InterruptedException {
		dom.initialize();
		
		//sw2.setSingleClickEnabled(true);
		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		// switch 1, single click
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(0, true);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(0, false);
		dom.loopOnce(cur += 10);// hier loopt het mis
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(false, hw.out(11));

		// switch 2, single click
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur+=(sw2.getDoubleClickTimeout()+10));
		dom.loopOnce(cur+=1);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
	}

	@Test
	public void allOff() throws InterruptedException {
		dom.initialize();

		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		// First put both on
		hw.in(0, true);
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(0, false);
		hw.in(1, false);
		dom.loopOnce(cur += 60);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
		// Now, all off with Switch 2
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += (sw2.getLongClickTimeout()+10));
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
	}

	@Test
	public void allOn() throws InterruptedException {
		dom.initialize();

		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += 10);
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
	}
	
	@Test
	public void testTimer() {
		Timer t = new Timer("timer","timer",null,dom);
		t.setOnTime(22, 0);
		t.setOffTime(7, 30);

		ssb.add(t, o1);
		dom.initialize();

		assertFalse(t.getStatus());
		assertFalse(o1.isOn());
		assertFalse(hw.out(10));
		assertFalse(hw.out(11));
	
		Calendar c = GregorianCalendar.getInstance();
		c.set(2013, 8, 2, 0, 0);	// 2 september 2013, toen geschreven ;-)
		t.loop(c.getTimeInMillis(), 0);
		assertTrue(t.getStatus());
		assertTrue(hw.out(10));
		
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis(),0);
		assertFalse(t.getStatus());
		assertFalse(hw.out(10));

		c.set(Calendar.HOUR_OF_DAY, 22);
		c.set(Calendar.MINUTE,30);
		t.loop(c.getTimeInMillis(),0);
		assertTrue(t.getStatus());
		assertTrue(hw.out(10));
}
}
