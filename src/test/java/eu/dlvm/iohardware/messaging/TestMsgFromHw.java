package eu.dlvm.iohardware.messaging;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.messaging.MsgFromHw;
import eu.dlvm.iohardware.diamondsys.messaging.MsgInputsDmmat;
import eu.dlvm.iohardware.diamondsys.messaging.MsgInputsOpalmm;
import eu.dlvm.iohardware.diamondsys.messaging.Parser;

public class TestMsgFromHw {

	@Test
	public void testInputValsOpalmm() {
		try {
			String line = "INP_O 0x300 176\n";
			MsgFromHw msg = Parser.parseFromWire(line);
			Assert.assertEquals(line, msg.asWireString());
			Assert.assertTrue(msg instanceof MsgInputsOpalmm);
			MsgInputsOpalmm ivo = (MsgInputsOpalmm) msg;
			Assert.assertEquals(0x300, ivo.getAddress());
			Assert.assertEquals(176, ivo.getValue());
		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testInputValsDmmat0() {
		try {
			String line = "INP_D 0x320 176 1458 2000\n";
			MsgFromHw msg = Parser.parseFromWire(line);
			Assert.assertEquals(line, msg.asWireString());
			Assert.assertTrue(msg instanceof MsgInputsDmmat);
			MsgInputsDmmat ivo = (MsgInputsDmmat) msg;
			Assert.assertEquals(0x320, ivo.getAddress());
			Assert.assertEquals(176, ivo.getDigitalInValue());
			Assert.assertEquals(1458, ivo.getAnalogInValue(0));
			Assert.assertEquals(2000, ivo.getAnalogInValue(1));
		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testInputValsDmmat1() {
		try {
			String line = "INP_D 0x300 176 - -\n";
			MsgFromHw msg = Parser.parseFromWire(line);
			Assert.assertEquals(line, msg.asWireString());
			Assert.assertTrue(msg instanceof MsgInputsDmmat);
			MsgInputsDmmat mid = (MsgInputsDmmat) msg;
			Assert.assertEquals(0x300, mid.getAddress());
			Assert.assertEquals(176, mid.getDigitalInValue());
			Assert.assertEquals(-1, mid.getAnalogInValue(0));
			Assert.assertEquals(-1, mid.getAnalogInValue(1));
		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testInputValsDmmat2() {
		try {
			String line = "INP_D 0x300 - - 4000\n";
			MsgFromHw msg = Parser.parseFromWire(line);
			Assert.assertEquals(line, msg.asWireString());
			Assert.assertTrue(msg instanceof MsgInputsDmmat);
			MsgInputsDmmat mid = (MsgInputsDmmat) msg;
			Assert.assertEquals(0x300, mid.getAddress());
			Assert.assertEquals(-1, mid.getDigitalInValue());
			Assert.assertEquals(-1, mid.getAnalogInValue(0));
			Assert.assertEquals(4000, mid.getAnalogInValue(1));
		} catch (ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Ignore
	@Test
	public void testError() {
		Assert.fail("Test not implemented yet.");
	}
}
