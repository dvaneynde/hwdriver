package eu.dlvm.domotica;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard;
import eu.dlvm.iohardware.LogCh;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.ChannelType;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.HardwareIO;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;

public class TestEnd2EndSwitchLamp {
	
	static Logger log = Logger.getLogger(TestEnd2EndSwitchLamp.class);

	public enum IO {
		S_KEUKENLICHT(0, "Schakelaar Licht Keuken"), L_KEUKEN(0, "Licht Keuken");

		private final int ch;
		private final String desc;

		IO(int channel, String description) {
			this.ch = channel;
			this.desc = description;
		}

		public LogCh ch() {
			return new LogCh(ch);
		}

		public String desc() {
			return "Ch:" + ch + " " + name() + " - " + desc;
		}
	};

	HardwareIO hw;
	HwDriverChannelMock drv;
	private long current;

	Domotic dom;
	Switch s;
	Lamp o;
	SwitchBoard ssb;

	class TestConfigurator implements IBoardFactory {
		@Override
		public void configure(List<Board> boards, ChannelMap map) {
			boards.add(new OpalmmBoard(0, 0x380, "First opalmm board."));
			map.add(IO.S_KEUKENLICHT.ch(), new FysCh(0, ChannelType.DigiIn, 0));
			map.add(IO.L_KEUKEN.ch(), new FysCh(0, ChannelType.DigiOut, 0));
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
		dom = new Domotic(hw);
		s = new Switch(IO.S_KEUKENLICHT.name(), IO.S_KEUKENLICHT.desc(),
				IO.S_KEUKENLICHT.ch(), dom);
		o = new Lamp(IO.L_KEUKEN.name(), IO.L_KEUKEN.desc(), IO.L_KEUKEN.ch(),
				dom);
		ssb = new SwitchBoard("SSB", "Lampen aan/uit");
		ssb.add(s, o);
	}

	@Test
	public final void testInitialize() {
		try {
			drv.responseFromDriverToUse0 = "";
			drv.responseFromDriverToUse1 = "";
			dom.loopOnce(current += 10);
			Assert.fail("Not initialized, and therefore Domotic should have thrown RuntimeException.");
		} catch (RuntimeException e) {
			Assert.assertEquals("Domotic not initialized.",e.getMessage());
		}
	}


	@Test
	public void testSwitch() {
		drv.reset("\n","\n");
		dom.initialize();
		log.debug("testSwitch: initialize, sendInputUsed:\n"+drv.sentToDriver0+"---");
		Assert.assertEquals("INIT\nBOARD_INIT O 0x380\n\n", drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x380 O 0\n\n", drv.sentToDriver1);
		
		drv.reset("INP_O 0x380 0\n\n","");
		dom.loopOnce(current += 10);
		log.debug("testSwitch: pushdown, sendInputUsed:\n"+drv.sentToDriver0+"---");
		log.debug("                     sendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals("REQ_INP 0x380 O\n\n", drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x380 O 0\n\n", drv.sentToDriver1);
		Assert.assertEquals(true, hw.readDigitalInput(IO.S_KEUKENLICHT.ch()));

		drv.reset("INP_O 0x380 255\n\n","");
		dom.loopOnce(current += 10);
		log.debug("testSwitch: pushdown, sendInputUsed:\n"+drv.sentToDriver0+"---");
		log.debug("                     sendOutputUsed:\n"+drv.sentToDriver1+"---");
		Assert.assertEquals("REQ_INP 0x380 O\n\n", drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x380 O 1\n\n", drv.sentToDriver1);
		Assert.assertEquals(false, hw.readDigitalInput(IO.S_KEUKENLICHT.ch()));
	}
}
