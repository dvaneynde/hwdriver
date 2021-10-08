package eu.dlvm.domotics.connectors;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

/**
 * Tests with Switches connected to Screens with Connector.
 * <p>
 * No ScreenController used here.
 * 
 * @author dirk
 *
 */
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

		public void out(int ch, boolean val) {
			outputs.put(Integer.toString(ch), val);
		}

		public boolean out(int ch) {
			return outputs.get(Integer.toString(ch));
		}

		public void in(int ch, boolean val) {
			inputs.put(Integer.toString(ch), val);
		}

		public boolean in(int ch) {
			return inputs.get(Integer.toString(ch));
		}
	};

	private Hardware hw;

	private Domotic dom;
	private Switch swDn1, swUp1, swDn2, swUp2;
	private Screen sr1, sr2;
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

		dom = Domotic.createSingleton(hw);
		swDn1 = new Switch("Down1", "Down-Switch Screen Kitchen", Integer.toString(SW_DN_1), hw, dom);
		swUp1 = new Switch("Up1", "Up-Switch Screen Kitchen", Integer.toString(SW_UP_1), hw, dom);
		sr1 = new Screen("Screen1", "Screen Kitchen", null, Integer.toString(REL_DN_1), Integer.toString(REL_UP_1), hw, dom);
		swDn2 = new Switch("Down2", "Down-Switch Screen Bathroom", Integer.toString(SW_DN_2), hw, dom);
		swUp2 = new Switch("Up2", "Up-Switch Screen Bathroom", Integer.toString(SW_UP_2), hw, dom);
		sr2 = new Screen("Screen2", "Screen Bathroom", null, Integer.toString(REL_DN_2), Integer.toString(REL_UP_2), hw, dom);

		//		s2s1 = new Switch2Screen("s2s1", "s2s1", null, swDn1, swUp1, ISwitchListener.ClickType.SINGLE);
		//		s2s1.registerListener(sr1);
		//		s2s2 = new Switch2Screen("s2s2", "s2s2", null, swDn2, swUp2, ISwitchListener.ClickType.SINGLE);
		//		s2s2.registerListener(sr2);
		swDn1.registerListener(new Connector(EventType.SINGLE_CLICK, sr1, EventType.TOGGLE_DOWN, "switchDown1"));
		swUp1.registerListener(new Connector(EventType.SINGLE_CLICK, sr1, EventType.TOGGLE_UP, "switchUp1"));
		swDn2.registerListener(new Connector(EventType.SINGLE_CLICK, sr2, EventType.TOGGLE_DOWN, "switchDown2"));
		swUp2.registerListener(new Connector(EventType.SINGLE_CLICK, sr2, EventType.TOGGLE_UP, "switchUp2"));

		swDn1.setLongClickEnabled(true);
		swDn1.setLongClickTimeout(LONGCLICKTIMEOUT);
		swUp1.setLongClickEnabled(true);
		swUp1.setLongClickTimeout(LONGCLICKTIMEOUT);
		//		s2sAll = new Switch2Screen("all", "", null, swDn1, swUp1, ISwitchListener.ClickType.LONG);
		//		s2sAll.registerListener(sr1);
		//		s2sAll.registerListener(sr2);
		swDn1.registerListener(new Connector(EventType.LONG_CLICK, sr1, EventType.TOGGLE_DOWN, "switchDown1"));
		swDn1.registerListener(new Connector(EventType.LONG_CLICK, sr2, EventType.TOGGLE_DOWN, "switchDown1"));
		swUp1.registerListener(new Connector(EventType.LONG_CLICK, sr1, EventType.TOGGLE_UP, "switchUp1"));
		swUp1.registerListener(new Connector(EventType.LONG_CLICK, sr2, EventType.TOGGLE_UP, "switchUp1"));

		cur = 0L;
		dom.initialize(new HashMap<String, RememberedOutput>(0));
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
	 *            If switchCh is up switch then this must be the up relay, and
	 *            vice versa
	 * @param relChOther
	 *            This must be the other relay, for the other direction, which
	 *            must remain false
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
		Assert.assertTrue(!hw.out(REL_DN_1) && !hw.out(REL_UP_1) && !hw.out(REL_DN_2) && !hw.out(REL_UP_2));
		dom.loopOnce(cur += 1);

		longClick(SW_DN_1);
		Assert.assertTrue(hw.out(REL_DN_1) && !hw.out(REL_UP_1) && hw.out(REL_DN_2) && !hw.out(REL_UP_2));
	}

	@Test
	public void AllUp() {
		dom.loopOnce(cur += 1);
		Assert.assertTrue(!hw.out(REL_DN_1) && !hw.out(REL_UP_1) && !hw.out(REL_DN_2) && !hw.out(REL_UP_2));
		dom.loopOnce(cur += 1);

		longClick(SW_UP_1);
		Assert.assertTrue(!hw.out(REL_DN_1) && hw.out(REL_UP_1) && !hw.out(REL_DN_2) && hw.out(REL_UP_2));
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
