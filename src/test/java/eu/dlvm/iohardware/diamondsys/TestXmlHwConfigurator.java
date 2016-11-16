package eu.dlvm.iohardware.diamondsys;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.HwDriverChannelMock;
import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;

public class TestXmlHwConfigurator {

	static Logger log = LoggerFactory.getLogger(TestXmlHwConfigurator.class);

	@Test
	public void testConfigBoardCreation() {
		XmlHwConfigurator xhc = new XmlHwConfigurator("src/test/resources/TestDiamondHwConfig1.xml");
		List<Board> boards = new ArrayList<Board>();
		ChannelMap map = new ChannelMap();
		xhc.configure(boards, map);
		Assert.assertTrue(boards.size() == 3);

		_checkOpalmm(boards.get(0), 0, 0x380, true, true);
		_checkDmmat(boards.get(1), 1, 0x400, false, false, false, true);
		_checkOpmm1616(boards.get(2), 2, 0x410, true, true);

		FysCh f;
		f = new FysCh(0, ChannelType.DigiIn, 5);
		Assert.assertEquals(f, map.fysCh(Integer.toString(87)));
		f = new FysCh(0, ChannelType.DigiOut, 2);
		Assert.assertEquals(f, map.fysCh(Integer.toString(13)));

		f = new FysCh(1, ChannelType.AnlgOut, 0);
		Assert.assertEquals(f, map.fysCh(Integer.toString(10)));

		f = new FysCh(2, ChannelType.DigiIn, 15);
		Assert.assertEquals(f, map.fysCh(Integer.toString(111)));
		f = new FysCh(2, ChannelType.DigiIn, 0);
		Assert.assertEquals(f, map.fysCh(Integer.toString(110)));
		f = new FysCh(2, ChannelType.DigiIn, 5);
		Assert.assertEquals(f, map.fysCh(Integer.toString(100)));
		f = new FysCh(2, ChannelType.DigiOut, 2);
		Assert.assertEquals(f, map.fysCh(Integer.toString(120)));

	}

	@Test
	public void testComplexConfiguration() {
		XmlHwConfigurator xhc = new XmlHwConfigurator("src/test/resources/TestDiamondHwConfig2.xml");
		List<Board> boards = new ArrayList<Board>();
		ChannelMap map = new ChannelMap();
		xhc.configure(boards, map);
		Assert.assertTrue(boards.size() == 5);

		_checkOpalmm(boards.get(0), 0, 0x300, false, true);
		_checkOpalmm(boards.get(1), 1, 0x310, true, false);
		_checkDmmat(boards.get(2), 2, 0x400, false, false, true, true);
		_checkDmmat(boards.get(3), 3, 0x410, true, true, false, true);
		_checkDmmat(boards.get(4), 4, 0x420, true, true, true, true);

		_checkMap(0, 0, ChannelType.DigiOut, 2, map);
		_checkMap(1, 1, ChannelType.DigiIn, 5, map);
		_checkMap(10, 2, ChannelType.AnlgIn, 0, map);
		_checkMap(11, 2, ChannelType.AnlgOut, 0, map);
		_checkMap(12, 2, ChannelType.AnlgOut, 1, map);
		_checkMap(30, 3, ChannelType.AnlgOut, 1, map);
		_checkMap(31, 3, ChannelType.DigiIn, 0, map);
		_checkMap(32, 3, ChannelType.DigiOut, 0, map);
		_checkMap(33, 3, ChannelType.DigiOut, 7, map);
		_checkMap(20, 4, ChannelType.AnlgIn, 1, map);
		_checkMap(21, 4, ChannelType.AnlgOut, 0, map);
		_checkMap(22, 4, ChannelType.AnlgOut, 1, map);
		_checkMap(23, 4, ChannelType.DigiIn, 0, map);
		_checkMap(24, 4, ChannelType.DigiOut, 0, map);
		_checkMap(25, 4, ChannelType.DigiOut, 5, map);
	}

	private void _checkMap(int logID, int boardNr, ChannelType channelType, int boardChannel, ChannelMap map) {
		FysCh f = new FysCh(boardNr, channelType, boardChannel);
		Assert.assertEquals(f, map.fysCh(Integer.toString(logID)));

	}

	private void _checkOpalmm(Board b, int boardNr, int address, boolean expDigiIn, boolean expDigiOut) {
		Assert.assertEquals(boardNr, b.boardNumber);
		Assert.assertEquals(address, b.address);
		Assert.assertTrue("OPALMM board expected", b instanceof OpalmmBoard);
		OpalmmBoard ob = (OpalmmBoard) b;
		Assert.assertEquals(expDigiIn, ob.digiIn != null);
		Assert.assertEquals(expDigiOut, ob.digiOut != null);
	}

	private void _checkOpmm1616(Board b, int boardNr, int address, boolean expDigiIn, boolean expDigiOut) {
		Assert.assertEquals(boardNr, b.boardNumber);
		Assert.assertEquals(address, b.address);
		Assert.assertTrue("OPMM1616 board expected", b instanceof Opmm1616Board);
		Opmm1616Board ob = (Opmm1616Board) b;
		Assert.assertEquals(expDigiIn, ob.digiIn != null);
		Assert.assertEquals(expDigiOut, ob.digiOut != null);
	}

	private void _checkDmmat(Board b, int boardNr, int address, boolean expDigiIn, boolean expDigiOut, boolean expAnaIn,
			boolean expAnaOut) {
		Assert.assertEquals(boardNr, b.boardNumber);
		Assert.assertEquals(address, b.address);
		Assert.assertTrue("DMMAT board expected", b instanceof DmmatBoard);
		DmmatBoard db = (DmmatBoard) b;
		Assert.assertEquals(expDigiIn, db.digiIn != null);
		Assert.assertEquals(expDigiOut, db.digiOut != null);
		Assert.assertEquals(expAnaIn, db.anaIns != null);
		Assert.assertEquals(expAnaOut, db.anaOuts != null);
	}

	@Test
	public void testComplexConfigurationAndMessagesOfInputStates() {
		// Hardware
		HwDriverChannelMock drv = new HwDriverChannelMock();
		XmlHwConfigurator xhc = new XmlHwConfigurator("src/test/resources/TestDiamondHwConfig2.xml");
		IHardwareIO hw = new HardwareIO(xhc, drv);

		drv.responseFromDriverToUse0 = "INP_O 0x310 127\nINP_D 0x400 - 0 0\nINP_D 0x410 46 - -\nINP_D 0x420 255 0 0";
		hw.refreshInputs();
		log.info("testComplexConfigurationAndMessagesOfInputStates, input state messages:\n" + drv.sentToDriver0);
		Assert.assertEquals("REQ_INP 0x310 O\nREQ_INP 0x400 D NYY\nREQ_INP 0x410 D YNN\nREQ_INP 0x420 D YYY\n\n",
				drv.sentToDriver0);
	}

	@Test
	public void testComplexConfigurationAndOutputMessages() {
		// Hardware
		HwDriverChannelMock drv = new HwDriverChannelMock();
		XmlHwConfigurator xhc = new XmlHwConfigurator("src/test/resources/TestDiamondHwConfig2.xml");
		IHardwareIO hw = new HardwareIO(xhc, drv);

		drv.responseFromDriverToUse0 = "";
		hw.refreshOutputs();
		log.info("testComplexConfigurationAndOutputMessages, output messages:\n" + drv.sentToDriver0);
		Assert.assertEquals(
				"SET_OUT 0x300 O 0\nSET_OUT 0x400 D - 0 0\nSET_OUT 0x410 D 0 0 0\nSET_OUT 0x420 D 0 0 0\n\n",
				drv.sentToDriver0);
	}

}
