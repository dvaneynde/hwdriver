package eu.dlvm.domotics;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.OpalmmBoardWithMsg;

public class TestEnd2EndSwitchLamp {
	
	static Logger log = LoggerFactory.getLogger(TestEnd2EndSwitchLamp.class);

	public enum IO {
		S_KEUKENLICHT(0, "Schakelaar Licht Keuken"), L_KEUKEN(0, "Licht Keuken");

		private final int ch;
		private final String desc;

		IO(int channel, String description) {
			this.ch = channel;
			this.desc = description;
		}

		public String ch() {
			return Integer.toString(ch);
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

	class TestConfigurator implements IBoardFactory {
		@Override
		public void configure(List<Board> boards, ChannelMap map) {
			boards.add(new OpalmmBoardWithMsg(0, 0x380, "First opalmm board.",true,true));
			map.add(IO.S_KEUKENLICHT.ch(), new FysCh(0, ChannelType.DigiIn, 0));
			map.add(IO.L_KEUKEN.ch(), new FysCh(0, ChannelType.DigiOut, 0));
		}
	}

	@Before
	public void setup() {
		current = 0L;
		// Hardware
		drv = new HwDriverChannelMock();
		hw = new HardwareIO(new TestConfigurator(), drv);
		// Domotic
		dom = Domotic.createSingleton(hw);
		s = new Switch(IO.S_KEUKENLICHT.name(), IO.S_KEUKENLICHT.desc(),
				IO.S_KEUKENLICHT.ch(), hw, dom);
		o = new Lamp(IO.L_KEUKEN.name(), IO.L_KEUKEN.desc(), false, IO.L_KEUKEN.ch(),
                hw, dom);
		s.registerListener(new Connector(EventType.SINGLE_CLICK, o, EventType.TOGGLE,"switch"));
	}

	@Ignore
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
		dom.initialize(new HashMap<String, RememberedOutput>(0));
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
