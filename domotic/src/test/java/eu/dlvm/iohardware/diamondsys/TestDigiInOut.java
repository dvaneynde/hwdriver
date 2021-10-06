package eu.dlvm.iohardware.diamondsys;

import org.junit.Test;

import junit.framework.Assert;

public class TestDigiInOut {

    @Test
	public void testDigiOut() {
        DigiOut d = new DigiOut();
		d.setOutputForChannel(true, 4);
		Assert.assertEquals(16, d.getValue());
		d.setOutputForChannel(false, 4);
		Assert.assertEquals(0, d.getValue());
	
		int init = 128 + 32 + 1;
		d.setOutputForChannel(true, 0);
		d.setOutputForChannel(true, 5);
		d.setOutputForChannel(true, 7);
		d.setOutputForChannel(false, 1);
		Assert.assertEquals(init, d.getValue());
		d.setOutputForChannel(false, 7);
		Assert.assertEquals(32 + 1, d.getValue());
		d.setOutputForChannel(false, 0);
		Assert.assertEquals(32, d.getValue());
	}

    @Test
	public void testDigiOutChanged() {
        DigiOut d = new DigiOut();
		Assert.assertEquals(0, d.getValue());
		Assert.assertTrue(d.outputStateHasChanged());
		d.resetOutputChangedDetection();
		Assert.assertEquals(0, d.getValue());
		Assert.assertFalse(d.outputStateHasChanged());
		d.setOutputForChannel(true, 4);
		Assert.assertEquals(16, d.getValue());
		Assert.assertTrue(d.outputStateHasChanged());
		d.resetOutputChangedDetection();
		Assert.assertEquals(16, d.getValue());
		Assert.assertFalse(d.outputStateHasChanged());
	}
	
    @Test
	public void testDigiIn() {
        DigiIn d = new DigiIn();
		d.updateInputFromHardware(~(33 + 128));
		Assert.assertTrue(d.getValue(0));
		Assert.assertFalse(d.getValue(1));
		Assert.assertFalse(d.getValue(2));
		Assert.assertFalse(d.getValue(3));
		Assert.assertFalse(d.getValue(4));
		Assert.assertTrue(d.getValue(5));
		Assert.assertFalse(d.getValue(6));
		Assert.assertTrue(d.getValue(7));
	
		d.updateInputFromHardware(~0);
		for (byte i = 0; i < 8; i++)
			Assert.assertFalse(d.getValue(i));
	
		d.updateInputFromHardware(~255);
		for (byte i = 0; i < 8; i++)
			Assert.assertTrue(d.getValue(i));
	
	}

    @Test
	public void testDigiInChangedDetection() {
        DigiIn d = new DigiIn();
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
