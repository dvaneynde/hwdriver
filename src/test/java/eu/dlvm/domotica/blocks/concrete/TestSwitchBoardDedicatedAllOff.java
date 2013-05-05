package eu.dlvm.domotica.blocks.concrete;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard;
import eu.dlvm.iohardware.LogCh;

public class TestSwitchBoardDedicatedAllOff {
	static Logger log = Logger.getLogger(TestSwitchBoardDedicatedAllOff.class);
	private TestSwitchBoard.Hardware hw;
	private Domotic dom;
	private Switch swLamp, swAllOff;
	private Lamp lamp;
	private SwitchBoard ssb;
	private long cur;

	@Before
	public void init() {
		cur = 0L;
		
		hw = new TestSwitchBoard.Hardware();
		hw.in(0, false);
		hw.in(1, false);
		hw.out(10, false);

		dom = new Domotic(hw);
		swLamp = new Switch("SwitchLamp", "Switch Lamp", new LogCh(0), dom);
		swAllOff = new Switch("SwitchAllOff", "Switch All Off", new LogCh(1),
				dom);
		lamp = new Lamp("Lamp1", "Lamp1", new LogCh(10), dom);
		ssb = new SwitchBoard("ssb", "ssb");
		ssb.add(swLamp, lamp);
		ssb.add(swAllOff, true, false);
		
		dom.initialize();
	}

	@Test
	public void testAllOff() throws InterruptedException {
		swAllOff.setLongClickEnabled(true);
		swAllOff.setLongClickTimeout(100);
		swAllOff.setDoubleClickEnabled(false);
		swAllOff.setSingleClickEnabled(false);
		Assert.assertEquals(false, hw.out(10));

		hw.in(0, true);
		hw.in(1, false);
		dom.loopOnce(cur += 60);
		hw.in(0, false);
		dom.loopOnce(cur += 1);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(true, hw.out(10));
		hw.in(1, true);
		dom.loopOnce(cur += 1);
		hw.in(1, false);
		dom.loopOnce(cur += 120);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(false, hw.out(10));
	}
}
