package eu.dlvm.iohardware.diamondsys;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

public class TestOpalmm {

	Helper h;

	@Before
	public void init() {
		h = new Helper();
	}

	@Test
	public void testOutput() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm output part.");
		// System.out.println(Util.CHANNEL_STATE_HEADER);
		// System.out.println(Util.channelStateAsString(b.getOutputstate()));
		h.testDigiOut(b.digiOut());
	}

	@Test
	public void testOutputChangedDetection() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm output part.");
		h.testDigiOutChanged(b.digiOut());
	}

	@Test
	public void testInput() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm input part.");
		h.testDigiIn(b.digiIn());

	}

	@Test
	public void testInputChangedDetection() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm input part, change listener.");
		h.testDigiInChangedDetection(b.digiIn());
	}
	
	@Test
	public void testInit() {
		OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm init().");
		Assert.assertTrue(b.digiOut().outputStateHasChanged());
		Assert.assertEquals(0, b.digiOut().getOutput());
		b.digiOut().setOutputForChannel(true, 0);
		Assert.assertEquals(1,b.digiOut().getOutput());

		b.digiOut().resetOutputChangedDetection();
		Assert.assertFalse(b.digiOut().outputStateHasChanged());
		Assert.assertEquals(1,b.digiOut().getOutput());

		b.init();
		Assert.assertTrue(b.digiOut().outputStateHasChanged());
		Assert.assertEquals(0, b.digiOut().getOutput());
	}
}
