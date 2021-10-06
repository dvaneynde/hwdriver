package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.TestUtils;

public class TestOpmm1616WithMsg {

    @Test
    public void testInitBoard() {
        IBoardMessaging b = new Opmm1616BoardWithMsg(0, 0x310);
        Assert.assertEquals("BOARD_INIT O 0x310\n", b.msgInitBoard());
    }

    @Test
    public void testRequestInput0() {
        IBoardMessaging b = new Opmm1616BoardWithMsg(0, 0x310, "test");
        Assert.assertEquals("REQ_INP 0x312 O\nREQ_INP 0x313 O\n", b.msgInputRequest());
    }

    @Test
    public void testRequestInput1() {
        IBoardMessaging b = new Opmm1616BoardWithMsg(0, 0x310, "test", false, false);
        Assert.assertEquals("", b.msgInputRequest());
    }

    @Test
    public void testInputVals() {
        try {
            Board b = new Opmm1616BoardWithMsg(0, 0x300);
            int state;

            String line = "INP_O 0x302 200\n";
            int address = Utils.parseLine(line, (IBoardMessaging) b);
            Assert.assertEquals(address, b.getAddress()+2);
            state = TestUtils.digitalInputAsByte(b);
            Assert.assertEquals(200, state);
            state = TestUtils.digitalInputAsByte(b, 8, 15);
            Assert.assertEquals(0, state);

            line = "INP_O 0x303 127\n";
            Utils.parseLine(line, (IBoardMessaging) b);
            state = TestUtils.digitalInputAsByte(b);
            Assert.assertEquals(200, state);
            state = TestUtils.digitalInputAsByte(b, 8, 15);
            Assert.assertEquals(127, state);

        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testOutput0() {
        IBoardMessaging b = new Opmm1616BoardWithMsg(0, 0x300, "", true, false);
        Assert.assertEquals("", b.msgOutputRequest());
    }

    @Test
    public void testOutput1() {
        Opmm1616BoardWithMsg b = new Opmm1616BoardWithMsg(0, 0x300);
        b.init();
        Assert.assertEquals("SET_OUT 0x300 O 0\nSET_OUT 0x301 O 0\n", b.msgOutputRequest());

        b.writeDigitalOutput(2, true);
        Assert.assertEquals("SET_OUT 0x300 O 4\nSET_OUT 0x301 O 0\n", b.msgOutputRequest());

        b.writeDigitalOutput(14, true);
        Assert.assertEquals("SET_OUT 0x300 O 4\nSET_OUT 0x301 O 64\n", b.msgOutputRequest());

        for (int i = 0; i < 16; i++)
            b.writeDigitalOutput(i, true);
        Assert.assertEquals("SET_OUT 0x300 O 255\nSET_OUT 0x301 O 255\n", b.msgOutputRequest());

        b.resetOutputChangedDetection();
        Assert.assertEquals("", b.msgOutputRequest());

        b.writeDigitalOutput(6, false);
        b.writeDigitalOutput(15, false);
        Assert.assertEquals("SET_OUT 0x300 O 191\nSET_OUT 0x301 O 127\n", b.msgOutputRequest());

        b.resetOutputChangedDetection();
        for (int i = 0; i < 16; i++)
            b.writeDigitalOutput(i, false);
        Assert.assertEquals("SET_OUT 0x300 O 0\nSET_OUT 0x301 O 0\n", b.msgOutputRequest());
    }

}
