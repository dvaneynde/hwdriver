package eu.dlvm.iohardware.messaging;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.DmmatBoard;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2HwReqInput;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2InitBoard;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2InitHardware;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2SetOutputs;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2Stop;

public class TestMsg2Hw {

	@Test
	public void testInit() {
		Msg2InitHardware msg2InitHardware = new Msg2InitHardware();
		Assert.assertEquals("INIT\n", msg2InitHardware.convert4Wire());
		Assert.assertEquals("INIT\n", msg2InitHardware.toString());
	}

	@Test
	public void testInitBoard() {
		Msg2InitBoard m;
		OpalmmBoard ob = new OpalmmBoard(0, 0x310);
		m = new Msg2InitBoard(ob);
		Assert.assertEquals("BOARD_INIT O 0x310\n", m.convert4Wire());
		
		DmmatBoard db = new DmmatBoard(0, 0x300);
		m = new Msg2InitBoard(db);
		Assert.assertEquals("BOARD_INIT D 0x300\n", m.convert4Wire());
		Assert.assertEquals("BOARD_INIT D 0x300\n", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputOpalmm0() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("REQ_INP 0x300 O\n", m.convert4Wire());
		Assert.assertEquals("REQ_INP 0x300 O\n", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputOpalmm1() {
		OpalmmBoard b = new OpalmmBoard(0, 0x310, "test", false, false);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("", m.convert4Wire());
		Assert.assertEquals("", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputDmmat0() {
		DmmatBoard b = new DmmatBoard(0, 0x320);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("REQ_INP 0x320 D YYY\n", m.convert4Wire());
		Assert.assertEquals("REQ_INP 0x320 D YYY\n", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputDmmat1() {
		DmmatBoard b = new DmmatBoard(0, 0x320, "test", true, false,
				new boolean[2], new boolean[2]);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("REQ_INP 0x320 D YNN\n", m.convert4Wire());
		Assert.assertEquals("REQ_INP 0x320 D YNN\n", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputDmmat2() {
		DmmatBoard b = new DmmatBoard(0, 0x320, "test", false, false,
				new boolean[] { true, false }, new boolean[2]);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("REQ_INP 0x320 D NYN\n", m.convert4Wire());
		Assert.assertEquals("REQ_INP 0x320 D NYN\n", m.toString());
	}

	@Test
	public void testMsg2Hw_RequestInputDmmat3() {
		DmmatBoard b = new DmmatBoard(0, 0x330, "test", true, false,
				new boolean[] { false, true }, new boolean[2]);
		Msg2HwReqInput m = new Msg2HwReqInput().construct(b);
		Assert.assertEquals("REQ_INP 0x330 D YNY\n", m.convert4Wire());
		Assert.assertEquals("REQ_INP 0x330 D YNY\n", m.toString());
	}

	@Test
	public void test_OutputOpalmm0() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "", true, false);
		Msg2SetOutputs m = new Msg2SetOutputs().construct(b);
		Assert.assertEquals("", m.convert4Wire());
	}

	@Test
	public void test_OutputOpalmm1() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300);
		b.init();
		Msg2SetOutputs m = new Msg2SetOutputs().construct(b);
		Assert.assertEquals("SET_OUT 0x300 O 0\n", m.convert4Wire());

		b.digiOut().setOutputForChannel(true, 2);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 O 4\n", m.convert4Wire());

		for (int i = 0; i < 8; i++)
			b.digiOut().setOutputForChannel(true, i);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 O 255\n", m.convert4Wire());

		b.resetOutputChangedDetection();
		m.construct(b);
		Assert.assertEquals("", m.convert4Wire());
		
		b.resetOutputChangedDetection();
		b.digiOut().setOutputForChannel(false, 6);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 O 191\n", m.convert4Wire());

		b.resetOutputChangedDetection();
		for (int i = 0; i < 8; i++)
			b.digiOut().setOutputForChannel(false, i);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 O 0\n", m.convert4Wire());
	}

	@Test
	public void test_OutputDmmat0() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "test", true, false,
				new boolean[] { true, true }, new boolean[2]);
		Msg2SetOutputs m = new Msg2SetOutputs().construct(b);
		Assert.assertEquals("", m.convert4Wire());
	}

	@Test
	public void test_OutputDmmat1() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "test", false, true,
				new boolean[2], new boolean[] { true, true });
		b.init();
		Msg2SetOutputs m = new Msg2SetOutputs().construct(b);
		Assert.assertEquals("SET_OUT 0x300 D 0 0 0\n", m.convert4Wire());
		
		b.digiOut().setOutputForChannel(true, 2);
		b.anaOut(0).setOutput(127);
		b.anaOut(1).setOutput(4048);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 D 4 127 4048\n", m.convert4Wire());
		
		b.resetOutputChangedDetection();
		m.construct(b);
		Assert.assertEquals("", m.convert4Wire());
		
		b.resetOutputChangedDetection();
		b.digiOut().setOutputForChannel(false, 2);
		m.construct(b);
		Assert.assertEquals("SET_OUT 0x300 D 0 - -\n", m.convert4Wire());
		
	}

	@Test
	public void test_OutputDmmat2() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "test", false, false,
				new boolean[2], new boolean[] { true, false });
		Msg2SetOutputs m = new Msg2SetOutputs().construct(b);
		Assert.assertEquals("SET_OUT 0x300 D - 0 -\n", m.convert4Wire());
	}

	@Test
	public void testQuit() {
		Msg2Stop s = new Msg2Stop();
		Assert.assertEquals("QUIT\n", s.convert4Wire());
	}
}
