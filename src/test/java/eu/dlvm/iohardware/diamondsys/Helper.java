package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.diamondsys.DigiIn;
import eu.dlvm.iohardware.diamondsys.DigiOut;
import junit.framework.Assert;

class Helper {

	void testDigiOut(DigiOut d) {
		d.setOutputForChannel(true, 4);
		Assert.assertEquals(16, d.getOutput());
		d.setOutputForChannel(false, 4);
		Assert.assertEquals(0, d.getOutput());
	
		int init = 128 + 32 + 1;
		d.setOutputForChannel(true, 0);
		d.setOutputForChannel(true, 5);
		d.setOutputForChannel(true, 7);
		d.setOutputForChannel(false, 1);
		Assert.assertEquals(init, d.getOutput());
		d.setOutputForChannel(false, 7);
		Assert.assertEquals(32 + 1, d.getOutput());
		d.setOutputForChannel(false, 0);
		Assert.assertEquals(32, d.getOutput());
	}

	void testDigiOutChanged(DigiOut d) {
		Assert.assertEquals(0, d.getOutput());
		Assert.assertTrue(d.outputStateHasChanged());
		d.resetOutputChangedDetection();
		Assert.assertEquals(0, d.getOutput());
		Assert.assertFalse(d.outputStateHasChanged());
		d.setOutputForChannel(true, 4);
		Assert.assertEquals(16, d.getOutput());
		Assert.assertTrue(d.outputStateHasChanged());
		d.resetOutputChangedDetection();
		Assert.assertEquals(16, d.getOutput());
		Assert.assertFalse(d.outputStateHasChanged());
	}
	
	void testDigiIn(DigiIn d) {
		d.updateInputFromHardware(~(33 + 128));
		Assert.assertTrue(d.getInput(0));
		Assert.assertFalse(d.getInput(1));
		Assert.assertFalse(d.getInput(2));
		Assert.assertFalse(d.getInput(3));
		Assert.assertFalse(d.getInput(4));
		Assert.assertTrue(d.getInput(5));
		Assert.assertFalse(d.getInput(6));
		Assert.assertTrue(d.getInput(7));
	
		d.updateInputFromHardware(~0);
		for (byte i = 0; i < 8; i++)
			Assert.assertFalse(d.getInput(i));
	
		d.updateInputFromHardware(~255);
		for (byte i = 0; i < 8; i++)
			Assert.assertTrue(d.getInput(i));
	
	}

	void testDigiInChangedDetection(DigiIn d) {
		d.updateInputFromHardware((32 + 1));
		d.updateInputFromHardware((32));
		Assert.assertTrue(d.inputHasChanged(0));
		for (int i = 1; i < 5; i++)
			Assert.assertFalse(d.inputHasChanged(i));
		Assert.assertFalse(d.inputHasChanged(5));
		for (int i = 6; i < 7; i++)
			Assert.assertFalse(d.inputHasChanged(i));
	}

}
