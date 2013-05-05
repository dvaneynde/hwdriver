package eu.dlvm.iohardware.diamondsys;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.AnaOutChannel;
import eu.dlvm.iohardware.diamondsys.DmmatBoard;

public class TestDmmat {

	Helper h;

	@Before
	public void init() {
		h = new Helper();
	}

	@Test
	public void testDigitalOutput() {
		DmmatBoard b = new DmmatBoard(0,0x300, "Test dmmat output part.");
		h.testDigiOut(b.digiOut());
	}

	@Test
	public void testDigitalInput() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat input part.");
		h.testDigiIn(b.digiIn());
	}

	@Test
	public void testDigitalInputChanged() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat input part, change listener.");
		h.testDigiInChangedDetection(b.digiIn());
	}

	@Test
	public void testAnalogOutput() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat analog output.");
		Assert.assertNotNull(b.anaOut((byte) 0));
		Assert.assertNotNull(b.anaOut((byte) 1));
		try {
			Assert.assertNotNull(b.anaOut((byte) 2));
			Assert.fail("Need an exception here.");
		} catch (IllegalArgumentException e) {
		}
		AnaOutChannel a1 = b.anaOut((byte) 0);
		AnaOutChannel a2 = b.anaOut((byte) 1);
		Assert.assertEquals(0, a1.getValue());
		Assert.assertEquals(0, a2.getValue());
		a1.setOutput(4000);
		a2.setOutput(DmmatBoard.ANALOG_RESOLUTION - 1);
		Assert.assertEquals(4000, a1.getValue());
		Assert.assertEquals(4095, a2.getValue());
		try {
			a1.setOutput(DmmatBoard.ANALOG_RESOLUTION);
			Assert.fail("Need an exception here.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testAnalogOutputChangedDetection() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat analog output.");
		AnaOutChannel a1 = b.anaOut((byte) 0);
		AnaOutChannel a2 = b.anaOut((byte) 1);
		Assert.assertTrue(a1.outputStateHasChanged());
		Assert.assertTrue(a2.outputStateHasChanged());
		Assert.assertEquals(0, a1.getValue());
		Assert.assertEquals(0, a2.getValue());

		a1.resetOutputChangedDetection();
		Assert.assertFalse(a1.outputStateHasChanged());
		Assert.assertTrue(a2.outputStateHasChanged());
		Assert.assertEquals(0, a1.getValue());
		Assert.assertEquals(0, a2.getValue());

		a1.setOutput(4000);
		a2.setOutput(DmmatBoard.ANALOG_RESOLUTION - 1);
		Assert.assertTrue(a1.outputStateHasChanged());
		Assert.assertTrue(a2.outputStateHasChanged());
		Assert.assertEquals(4000, a1.getValue());
		Assert.assertEquals(4095, a2.getValue());
		
		b.init();
		Assert.assertTrue(a1.outputStateHasChanged());
		Assert.assertTrue(a2.outputStateHasChanged());
		Assert.assertEquals(0, a1.getValue());
		Assert.assertEquals(0, a2.getValue());
	}
	
	@Test
	public void testInit() {
		DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat init().");
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
