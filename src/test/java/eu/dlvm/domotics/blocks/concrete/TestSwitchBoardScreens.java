package eu.dlvm.domotics.blocks.concrete;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.mappers.Switch2Screen;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestSwitchBoardScreens {

	public static int LONGCLICKTIMEOUT = 50;
	public static final int SW_DN_1 = 0;
	public static final int SW_UP_1 = 1;
	public static final int SW_DN_2 = 2;
	public static final int SW_UP_2 = 3;
	public static final int REL_DN_1 = 10;
	public static final int REL_UP_1 = 11;
	public static final int REL_DN_2 = 12;
	public static final int REL_UP_2 = 13;

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		private Map<LogCh, Boolean> inputs = new HashMap<LogCh, Boolean>();
		private Map<LogCh, Boolean> outputs = new HashMap<LogCh, Boolean>();

		@Override
		public void writeDigitalOutput(LogCh channel, boolean value)
				throws IllegalArgumentException {
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

	private Hardware hw;

	private Domotic dom;
	private Switch swDn1, swUp1, swDn2, swUp2;
	private Screen sr1, sr2;
	private Switch2Screen s2s1, s2s2, s2sAll;
	private long cur;

	@Before
	public void init() {
		hw = new Hardware();
		hw.in(SW_DN_1, false);
		hw.in(SW_UP_1, false);
		hw.in(SW_DN_2, false);
		hw.in(SW_UP_2, false);
		hw.out(REL_DN_1, false);
		hw.out(REL_UP_1, false);
		hw.out(REL_DN_2, false);
		hw.out(REL_UP_2, false);

		Domotic.resetSingleton();
		dom = Domotic.singleton(hw);
		swDn1 = new Switch("Down1", "Down-Switch Screen Kitchen", new LogCh(
				SW_DN_1), dom);
		swUp1 = new Switch("Up1", "Up-Switch Screen Kitchen",
				new LogCh(SW_UP_1), dom);
		sr1 = new Screen("Screen1", "Screen Kitchen", new LogCh(REL_DN_1),
				new LogCh(REL_UP_1), dom);
		swDn2 = new Switch("Down2", "Down-Switch Screen Bathroom", new LogCh(
				SW_DN_2), dom);
		swUp2 = new Switch("Up2", "Up-Switch Screen Bathroom", new LogCh(
				SW_UP_2), dom);
		sr2 = new Screen("Screen2", "Screen Bathroom", new LogCh(REL_DN_2),
				new LogCh(REL_UP_2), dom);
		
		s2s1 = new Switch2Screen("s2s1", "s2s1",swDn1,swUp1,ISwitchListener.ClickType.SINGLE);
		s2s1.registerListener(sr1);
		s2s2 = new Switch2Screen("s2s2", "s2s2",swDn2,swUp2,ISwitchListener.ClickType.SINGLE);
		s2s2.registerListener(sr2);
		
		swDn1.setLongClickEnabled(true);
		swDn1.setLongClickTimeout(LONGCLICKTIMEOUT);
		swUp1.setLongClickEnabled(true);
		swUp1.setLongClickTimeout(LONGCLICKTIMEOUT);
		s2sAll = new Switch2Screen("all", "", swDn1, swUp1, ISwitchListener.ClickType.LONG);
		s2sAll.registerListener(sr1);
		s2sAll.registerListener(sr2);
		
		cur = 0L;
		dom.initialize(new HashMap<String, RememberedOutput> (0));

		BasicConfigurator.configure();
	}

	@Test
	public void screen1Down() {
		activateOne(SW_DN_1, REL_DN_1, REL_UP_1);
	}

	@Test
	public void screen1Up() {
		activateOne(SW_UP_1, REL_UP_1, REL_DN_1);
	}

	@Test
	public void screen2Down() {
		activateOne(SW_DN_2, REL_DN_2, REL_UP_2);
	}

	@Test
	public void screen2Up() {
		activateOne(SW_UP_2, REL_UP_2, REL_DN_2);
	}

	/**
	 * Single click on a screen switch, either up or down.
	 * 
	 * @param switchCh
	 *            Up or Down switch channel.
	 * @param relChOfSwitch
	 *            If switchCh is up switch then this must be the up relay, and vice versa
	 * @param relChOther
	 *            This must be the other relay, for the other direction, which must remain false
	 */
	private void activateOne(int switchCh, int relChOfSwitch, int relChOther) {
		dom.loopOnce(cur += 1);
		Assert.assertEquals(false, hw.out(relChOfSwitch));
		Assert.assertEquals(false, hw.out(relChOther));
		dom.loopOnce(cur += 1);

		click(switchCh);
		Assert.assertEquals(true, hw.out(relChOfSwitch));
		Assert.assertEquals(false, hw.out(relChOther));

		click(switchCh);
		Assert.assertEquals(false, hw.out(relChOfSwitch));
		Assert.assertEquals(false, hw.out(relChOther));
	}

	private void click(int switchChannel) {
		hw.in(switchChannel, true);
		dom.loopOnce(cur += 1);
		hw.in(switchChannel, false);
		dom.loopOnce(cur += 1);
	}

	@Test
	public void AllDown() {
		dom.loopOnce(cur += 1);
		Assert.assertTrue(!hw.out(REL_DN_1) && !hw.out(REL_UP_1)
				&& !hw.out(REL_DN_2) && !hw.out(REL_UP_2));
		dom.loopOnce(cur += 1);

		longClick(SW_DN_1);
		Assert.assertTrue(hw.out(REL_DN_1) && !hw.out(REL_UP_1)
				&& hw.out(REL_DN_2) && !hw.out(REL_UP_2));
	}

	@Test
	public void AllUp() {
		dom.loopOnce(cur += 1);
		Assert.assertTrue(!hw.out(REL_DN_1) && !hw.out(REL_UP_1)
				&& !hw.out(REL_DN_2) && !hw.out(REL_UP_2));
		dom.loopOnce(cur += 1);

		longClick(SW_UP_1);
		Assert.assertTrue(!hw.out(REL_DN_1) && hw.out(REL_UP_1)
				&& !hw.out(REL_DN_2) && hw.out(REL_UP_2));
	}

	@Test
	public void AllDownOneUp() {
		dom.loopOnce(cur += 10);
		Assert.assertFalse(hw.out(REL_DN_1));
		Assert.assertFalse(hw.out(REL_UP_1));
		Assert.assertFalse(hw.out(REL_DN_2));
		Assert.assertFalse(hw.out(REL_UP_2));

		longClick(SW_DN_1);
		dom.loopOnce(cur += 10);
		Assert.assertTrue(hw.out(REL_DN_1));
		Assert.assertFalse(hw.out(REL_UP_1));
		Assert.assertTrue(hw.out(REL_DN_2));
		Assert.assertFalse(hw.out(REL_UP_2));

		click(SW_UP_2);
		Assert.assertTrue(hw.out(REL_DN_1));
		Assert.assertFalse(hw.out(REL_UP_1));
		Assert.assertFalse(hw.out(REL_DN_2));
		Assert.assertFalse(hw.out(REL_UP_2));

		dom.loopOnce(cur += (Screen.MOTOR_SWITCH_DELAY_PROTECTION + 10));
		Assert.assertTrue(hw.out(REL_DN_1));
		Assert.assertFalse(hw.out(REL_UP_1));
		Assert.assertFalse(hw.out(REL_DN_2));
		Assert.assertTrue(hw.out(REL_UP_2));
	}

	private void longClick(int switchChannel) {
		hw.in(switchChannel, true);
		dom.loopOnce(cur += 1);
		hw.in(switchChannel, false);
		dom.loopOnce(cur += (LONGCLICKTIMEOUT + 1));
	}
}
