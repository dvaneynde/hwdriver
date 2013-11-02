package eu.dlvm.domotica;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.concrete.DimmedLamp;
import eu.dlvm.domotica.blocks.concrete.DimmerSwitches;
import eu.dlvm.domotica.blocks.concrete.SwitchBoardDimmers;
import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.LogCh;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;
import eu.dlvm.iohardware.diamondsys.messaging.DmmatBoardWithMsg;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.OpalmmBoardWithMsg;

public class TestEnd2EndDimmer {

	static Logger log = Logger.getLogger(TestEnd2EndDimmer.class);

	public static final LogCh SW_DN_1 = new LogCh(0);
	public static final LogCh SW_UP_1 = new LogCh(1);
	public static final LogCh DIMMER1 = new LogCh(100);

	HardwareIO hw;
	HwDriverChannelMock drv;
	private long current;

	Domotic dom;
	DimmerSwitches dsw1;
	DimmedLamp dl1;
	SwitchBoardDimmers sbd;

	class TestConfigurator implements IBoardFactory {
		@Override
		public void configure(List<Board> boards, ChannelMap map) {
			boards.add(new OpalmmBoardWithMsg(0, 0x380, "Opalmm board, gebruikt voor Dimmer Switches."));
			boards.add(new DmmatBoardWithMsg(1, 0x300, "Dmmat board, gebruikt voor Analog Output."));
			map.add(SW_DN_1, new FysCh(0, ChannelType.DigiOut, 0));
			map.add(SW_UP_1, new FysCh(0, ChannelType.DigiIn, 1));
			map.add(DIMMER1, new FysCh(1, ChannelType.AnlgOut, 0));
		}
	}

	@Before
	public void setup() {
		BasicConfigurator.configure();
		current = 0L;
		// Hardware
		drv = new HwDriverChannelMock();
		hw = new HardwareIO(new TestConfigurator(), drv);
		// Domotic
		Domotic.resetSingleton();
		dom = Domotic.singleton(hw);
		dsw1 = new DimmerSwitches("dsw1", "Dimmer Switches 1", SW_DN_1,
				SW_UP_1, dom);
		dl1 = new DimmedLamp("dl1", "Dimmed Lamp 1", 99, DIMMER1, dom);
		sbd = new SwitchBoardDimmers("sbd", "Switchboard Dimmers");
		sbd.add(dsw1, dl1);
	}

	@Test
	public void testDimmer() {
		drv.reset("\n", "\n");
		dom.initialize();
		log.debug("testDimmer: initialize, sendInputUsed:\n"
				+ drv.sentToDriver0 + "---");
		Assert.assertEquals("INIT\nBOARD_INIT O 0x380\nBOARD_INIT D 0x300\n\n", drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x380 O 0\nSET_OUT 0x300 D 0 49 0\n\n", drv.sentToDriver1);
		Assert.assertEquals(DimmerSwitches.States.REST, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.ON, dl1.getState());
		Assert.assertEquals(50, dl1.getLevel());
		
		// Originally - when test was written - after startup initial level
		// was 0% and state OFF, now ON and 50%. So set off.
		dl1.off();
		Assert.assertEquals(DimmedLamp.States.OFF, dl1.getState());
		Assert.assertEquals(0, dl1.getLevel());

		// Start long-click up
		// Negative logic ! Input channel 1 must go up.
		drv.reset("INP_O 0x380 253\n\n", "\n");
		dom.loopOnce(current+=10);
		log.debug("testDimmer: 1, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals("REQ_INP 0x380 O\nREQ_INP 0x300 D YYY\n\n", drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x380 O 0\nSET_OUT 0x300 D 0 0 0\n\n", drv.sentToDriver1);
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.OFF, dl1.getState());
		Assert.assertEquals(0, dl1.getLevel());

		// Just loop...
		drv.reset("\n","\n");
		dom.loopOnce(current+=10);
		log.debug("testDimmer: 2, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals(DimmerSwitches.States.DOWN_SHORT, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.OFF, dl1.getState());
		Assert.assertEquals(0, dl1.getLevel());

		// Wait long enough to make it a long click
		// Should go up now
		drv.reset("\n","\n");
		dom.loopOnce(current += (dsw1.getClickedTimeoutMS() + 1));
		// Note that output value has not changed yet, we start at 0 with first loop.
		log.debug("testDimmer: 3, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.UP, dl1.getState());

		// Continue let it go up...
		drv.reset("\n","\n");
		dom.loopOnce(current += (dl1.getMsTimeFullDim() / 10));
		log.debug("testDimmer: 4, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals("SET_OUT 0x380 O 0\nSET_OUT 0x300 D 0 9 0\n\n", drv.sentToDriver1);
		String s1 = drv.sentToDriver1.substring(drv.sentToDriver1.indexOf('D')+4);
		int i1 = Integer.parseInt(s1.substring(0, s1.indexOf(' ')));
		log.debug("While going up, analog value on channel 0 = "+i1);
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.UP, dl1.getState());

		// Wait a bit and see if it counts up
		drv.reset("\n","\n");
		dom.loopOnce(current += (dl1.getMsTimeFullDim() / 10));
		log.debug("testDimmer: 5, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals("SET_OUT 0x380 O 0\nSET_OUT 0x300 D 0 19 0\n\n", drv.sentToDriver1);
		String s2 = drv.sentToDriver1.substring(drv.sentToDriver1.indexOf('D')+4);
		int i2 = Integer.parseInt(s2.substring(0, s2.indexOf(' ')));
		log.debug("While having goine up for a little longer, analog value on channel 0 = "+i2);
		Assert.assertEquals(DimmerSwitches.States.DOWN_LONG, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.UP, dl1.getState());
		Assert.assertTrue("Analog values must be higher.", i2 > i1);

		// All inputs off
		drv.reset("INP_O 0x380 255\n\n","\n");
		dom.loopOnce(current += 1);
		log.debug("testDimmer: 6, sendInputUsed:\n"
				+ drv.sentToDriver0 + "\nsendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals(DimmerSwitches.States.REST, dsw1.getState());
		Assert.assertEquals(DimmedLamp.States.ON, dl1.getState());
	}
}
