package eu.dlvm.iohardware.diamondsys;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.HwDriverChannelMock;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;

public class TestXmlHwConfigurator {

	static Logger log = Logger.getLogger(TestXmlHwConfigurator.class);

	@Before
	public void setUp() {
		PropertyConfigurator
				.configure("src/test/resources/log4j-test.properties");
	}

	@Test
	public void testConfigBoardCreation() {
		XmlHwConfigurator xhc = new XmlHwConfigurator();
		xhc.setCfgFilepath("src/test/resources/TestDiamondHwConfig1.xml");
		List<Board> boards = new ArrayList<Board>();
		ChannelMap map = new ChannelMap();
		xhc.configure(boards, map);
		Assert.assertTrue(boards.size() == 2);

		_checkOpalmm(boards.get(0), 0, 0x380, true, true);
		_checkDmmat(boards.get(1), 1, 0x400, false, false, false, false, true,
				false);

		FysCh f;
		f = new FysCh(0, ChannelType.DigiIn, 5);
		Assert.assertEquals(f, map.fysCh(new LogCh(87)));
		f = new FysCh(0, ChannelType.DigiOut, 2);
		Assert.assertEquals(f, map.fysCh(new LogCh(13)));
		f = new FysCh(1, ChannelType.AnlgOut, 0);
		Assert.assertEquals(f, map.fysCh(new LogCh(10)));

	}

	@Test
	public void testComplexConfiguration() {
		XmlHwConfigurator xhc = new XmlHwConfigurator();
		xhc.setCfgFilepath("src/test/resources/TestDiamondHwConfig2.xml");
		List<Board> boards = new ArrayList<Board>();
		ChannelMap map = new ChannelMap();
		xhc.configure(boards, map);
		Assert.assertTrue(boards.size() == 5);

		_checkOpalmm(boards.get(0), 0, 0x300, false, true);
		_checkOpalmm(boards.get(1), 1, 0x310, true, false);
		_checkDmmat(boards.get(2), 2, 0x400, false, false, true, false, true,
				true);
		_checkDmmat(boards.get(3), 3, 0x410, true, true, false, false, false,
				true);
		_checkDmmat(boards.get(4), 4, 0x420, true, true, false, true, true,
				true);

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

	private void _checkMap(int logID, int boardNr, ChannelType channelType,
			int boardChannel, ChannelMap map) {
		FysCh f = new FysCh(boardNr, channelType, boardChannel);
		Assert.assertEquals(f, map.fysCh(new LogCh(logID)));

	}

	private void _checkOpalmm(Board b, int boardNr, int address,
			boolean expDigiIn, boolean expDigiOut) {
		Assert.assertEquals(boardNr, b.boardNumber);
		Assert.assertEquals(address, b.address);
		Assert.assertTrue("OPALMM board expected", b instanceof OpalmmBoard);
		OpalmmBoard ob = (OpalmmBoard) b;
		Assert.assertEquals(expDigiIn, ob.digiIn() != null);
		Assert.assertEquals(expDigiOut, ob.digiOut() != null);
	}

	private void _checkDmmat(Board b, int boardNr, int address,
			boolean expDigiIn, boolean expDigiOut, boolean expAnaIn0,
			boolean expAnaIn1, boolean expAnaOut0, boolean expAnaOut1) {
		Assert.assertEquals(boardNr, b.boardNumber);
		Assert.assertEquals(address, b.address);
		Assert.assertTrue("DMMAT board expected", b instanceof DmmatBoard);
		DmmatBoard db = (DmmatBoard) b;
		Assert.assertEquals(expDigiIn, db.digiIn() != null);
		Assert.assertEquals(expDigiOut, db.digiOut() != null);
		Assert.assertEquals(expAnaIn0, db.anaIn(0) != null);
		Assert.assertEquals(expAnaIn1, db.anaIn(1) != null);
		Assert.assertEquals(expAnaOut0, db.anaOut(0) != null);
		Assert.assertEquals(expAnaOut1, db.anaOut(1) != null);
	}

	@Test
	public void testComplexConfigurationAndMessagesOfInputStates() {
		// Hardware
		HwDriverChannelMock drv = new HwDriverChannelMock();
		XmlHwConfigurator xhc = new XmlHwConfigurator();
		xhc.setCfgFilepath("src/test/resources/TestDiamondHwConfig2.xml");
		IHardwareIO hw = new HardwareIO(xhc, drv);

		drv.responseFromDriverToUse0 = "INP_O 0x310 127\nINP_D 0x400 - 0 -\nINP_D 0x410 46 - -\nINP_D 0x420 255 - 0";
		hw.refreshInputs();
		log.info("testComplexConfigurationAndMessagesOfInputStates, input state messages:\n"+drv.sentToDriver0);
		Assert.assertEquals(
				"REQ_INP 0x310 O\nREQ_INP 0x400 D NYN\nREQ_INP 0x410 D YNN\nREQ_INP 0x420 D YNY\n\n",
				drv.sentToDriver0);
	}
	
	@Test
	public void testComplexConfigurationAndOutputMessages() {
		// Hardware
		HwDriverChannelMock drv = new HwDriverChannelMock();
		XmlHwConfigurator xhc = new XmlHwConfigurator();
		xhc.setCfgFilepath("src/test/resources/TestDiamondHwConfig2.xml");
		IHardwareIO hw = new HardwareIO(xhc, drv);

		drv.responseFromDriverToUse0 = "";
		hw.refreshOutputs();
		log.info("testComplexConfigurationAndOutputMessages, output messages:\n"+drv.sentToDriver0);
		Assert.assertEquals("SET_OUT 0x300 O 0\nSET_OUT 0x400 D - 0 0\nSET_OUT 0x410 D 0 - 0\nSET_OUT 0x420 D 0 0 0\n\n", drv.sentToDriver0);
	}
}
