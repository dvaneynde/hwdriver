package eu.dlvm.domotics.connectors;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public class TestSwitchBoardDimmers {

	static Logger log = LoggerFactory.getLogger(TestSwitchBoardDimmers.class);

	public static int LONGCLICKTIMEOUT = 50;
	public static final int FULL_OUT_VAL = 1024;
	public static final String SW_DN_1 = Integer.toString(0);
	public static final String SW_UP_1 = Integer.toString(1);
	public static final String SW_DN_2 = Integer.toString(2);
	public static final String SW_UP_2 = Integer.toString(3);
	public static final String SW_ALL = Integer.toString(4);
	public static final String DIMMER1 = Integer.toString(10);
	public static final String DIMMER2 = Integer.toString(11);

	public static class Hardware extends BaseHardwareMock implements IHardwareIO {
		private Map<String, Boolean> inputs = new HashMap<String, Boolean>();
		private Map<String, Integer> outputs = new HashMap<String, Integer>();

		@Override
		public void writeAnalogOutput(String channel, int value) throws IllegalArgumentException {
			outputs.put(channel, value);
		}

		@Override
		public boolean readDigitalInput(String channel) {
			return inputs.get(channel);
		}
	};

	private Domotic dom;
	private Hardware hw;
	private DimmerSwitch dsw1;
	private DimmedLamp dl1;
	private Switch swAllOnOff;
	private long cur;

	@Before
	public void init() {
		cur = 0L;

		hw = new Hardware();
		hw.inputs.put(SW_DN_1, false);
		hw.inputs.put(SW_UP_1, false);
		hw.inputs.put(SW_DN_2, false);
		hw.inputs.put(SW_UP_2, false);
		hw.inputs.put(SW_ALL, false);
		hw.outputs.put(DIMMER1, 0);
		hw.outputs.put(DIMMER2, 0);

		dom = Domotic.createSingleton(hw);

		dsw1 = new DimmerSwitch("dsw1", "Dimmer Switches 1", SW_DN_1, SW_UP_1, hw, dom);
		dl1 = new DimmedLamp("dl1", "Dimmed Lamp 1", FULL_OUT_VAL, DIMMER1, hw, dom);
		dl1.setMsTimeFullDim(3000);
		//		ds2d = new DimmerSwitch2Dimmer("ds2d", "ds2d");
		//		ds2d.setLamp(dl1);
		//		dsw1.registerListener(ds2d);
		dsw1.registerListener(dl1);

		swAllOnOff = new Switch("swAll", "Switch All On/Off", SW_ALL, hw, dom);
		swAllOnOff.setDoubleClickEnabled(true);
		swAllOnOff.setDoubleClickTimeout(400L);
		swAllOnOff.setLongClickEnabled(true);
		swAllOnOff.setLongClickTimeout(1000L);
		swAllOnOff.setSingleClickEnabled(false);
//		Switch2OnOffToggle swtch2AllOff = new Switch2OnOffToggle("allOff", "allOff", null);
//		swtch2AllOff.map(ClickType.LONG, ActionType.OFF);
//		swAllOnOff.registerListener(swtch2AllOff);
//		swtch2AllOff.registerListener(dl1);
		swAllOnOff.registerListener(new Connector(EventType.LONG_CLICK, dl1, EventType.OFF, "AllOnOff"));
	}

	@Test
	public void testFullThenAllOff() {
		// Initialisatie
		dom.initialize(new HashMap<String, RememberedOutput>(0));
		Assert.assertEquals(FULL_OUT_VAL / 2, hw.outputs.get(DIMMER1).intValue());
		// Donker
		dl1.on(0);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(0, hw.outputs.get(DIMMER1).intValue());
		// Volledig aan (links down, dan rechts klik)
		hw.inputs.put(SW_DN_1, true);
		dom.loopOnce(cur += 1);
		hw.inputs.put(SW_UP_1, true);
		dom.loopOnce(cur += 1);
		hw.inputs.put(SW_UP_1, false);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(FULL_OUT_VAL, hw.outputs.get(DIMMER1).intValue());
		hw.inputs.put(SW_DN_1, false);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(FULL_OUT_VAL, hw.outputs.get(DIMMER1).intValue());
		// Alles uit
		hw.inputs.put(SW_ALL, true);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(FULL_OUT_VAL, hw.outputs.get(DIMMER1).intValue());
		dom.loopOnce(cur += (swAllOnOff.getLongClickTimeout() + 1));
		Assert.assertEquals(0, hw.outputs.get(DIMMER1).intValue());
		hw.inputs.put(SW_ALL, false);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(0, hw.outputs.get(DIMMER1).intValue());
	}

	@Test
	public void testDimUpAndDownDimmer1() {
		// Initialisatie
		dom.initialize(new HashMap<String, RememberedOutput>(0));
		Assert.assertEquals(FULL_OUT_VAL / 2, hw.outputs.get(DIMMER1).intValue());
		// Donker
		dl1.on(0);
		dom.loopOnce(cur += 1);
		Assert.assertEquals(0, hw.outputs.get(DIMMER1).intValue());
		// Dimmen
		int level1, level2;
		dom.loopOnce(cur += 1);
		Assert.assertEquals(0, hw.outputs.get(DIMMER1).intValue());
		dom.loopOnce(cur += 1);
		// go up...
		hw.inputs.put(SW_UP_1, true);
		dom.loopOnce(cur += 1);
		level1 = hw.outputs.get(DIMMER1).intValue();
		Assert.assertEquals(0, level1);
		dom.loopOnce(cur += (dsw1.getClickedTimeoutMS() + 1));
		level1 = hw.outputs.get(DIMMER1).intValue();
		log.debug("level1 = " + level1); // TODO zou toch bijna 0 moeten zijn?
		dom.loopOnce(cur += (dl1.getMsTimeFullDim() / 3));
		level2 = hw.outputs.get(DIMMER1).intValue();
		log.debug("level 1=" + level1 + ", level2=" + level2);
		Assert.assertTrue(level2 > level1);
		// stop going up
		hw.inputs.put(SW_UP_1, false);
		dom.loopOnce(cur += 1);
		level1 = hw.outputs.get(DIMMER1).intValue();
		dom.loopOnce(cur += (dl1.getMsTimeFullDim() / 3));
		level2 = hw.outputs.get(DIMMER1).intValue();
		log.debug("should be equal: level 1=" + level1 + ", level2=" + level2);
		Assert.assertTrue(level1 == level2);
	}
}
