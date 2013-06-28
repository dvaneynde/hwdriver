package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.TestUtils;

public class TestDmmatWithMsg {

    @Test
    public void testInitBoard() {
        IBoardMessaging b = new DmmatBoardWithMsg(0, 0x300);
        Assert.assertEquals("BOARD_INIT D 0x300\n", b.msgInitBoard());
    }

    @Test
    public void testRequestInput0() {
        IBoardMessaging b = new DmmatBoardWithMsg(0, 0x320);
        Assert.assertEquals("REQ_INP 0x320 D YYY\n", b.msgInputRequest());
    }

    @Test
    public void testRequestInput1() {
        IBoardMessaging b = new DmmatBoardWithMsg(0, 0x320, "test", false, false, true, false);
        Assert.assertEquals("REQ_INP 0x320 D NYY\n", b.msgInputRequest());
    }

    @Test
    public void testRequestInput2() {
        IBoardMessaging b = new DmmatBoardWithMsg(0, 0x330, "test", true, false, false, true);
        Assert.assertEquals("REQ_INP 0x330 D YNN\n", b.msgInputRequest());
    }

    @Test
    public void testInputVals0() {
        try {
            String line = "INP_D 0x320 176 1458 2000\n";
            Board b = new DmmatBoardWithMsg(0, 0x320);
            int address = Utils.parseLine(line, (IBoardMessaging) b);

            Assert.assertEquals(address, b.getAddress());
            int digistate = TestUtils.digitalInputAsByte(b);
            //Assert.assertEquals(~176 & 0xFF, digistate);
            Assert.assertEquals(176, digistate);
            Assert.assertEquals(1458, b.readAnalogInput((byte) 0));
            Assert.assertEquals(2000, b.readAnalogInput((byte) 1));
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testInputVals1() {
        try {
            String line = "INP_D 0x300 176 - -\n";
            Board b = new DmmatBoardWithMsg(0, 0x300);
            int address = Utils.parseLine(line, (IBoardMessaging) b);

            Assert.assertEquals(address, b.getAddress());
            int digistate = TestUtils.digitalInputAsByte(b);
            Assert.assertEquals(176, digistate);
            Assert.assertEquals(0, b.readAnalogInput((byte) 0));
            Assert.assertEquals(0, b.readAnalogInput((byte) 1));
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testInputVals2() {
        try {
            String line = "INP_D 0x300 - - 4000\n";
            Board b = new DmmatBoardWithMsg(0, 0x300);
            int address = Utils.parseLine(line, (IBoardMessaging) b);

            Assert.assertEquals(address, b.getAddress());
            int digistate = TestUtils.digitalInputAsByte(b);
            Assert.assertEquals(0, digistate);
            Assert.assertEquals(0, b.readAnalogInput((byte) 0));
            Assert.assertEquals(4000, b.readAnalogInput((byte) 1));
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testOutput0() {
        DmmatBoardWithMsg b = new DmmatBoardWithMsg(0, 0x300, "test", true, false, true, false);
        Assert.assertEquals("", b.msgOutputRequest());
    }

    @Test
    public void testOutput1() {
        DmmatBoardWithMsg b = new DmmatBoardWithMsg(0, 0x300, "test", false, true, false, true);
        b.init();
        Assert.assertEquals("SET_OUT 0x300 D 0 0 0\n", b.msgOutputRequest());

        b.writeDigitalOutput(2, true);
        b.writeAnalogOutput(0, 127);
        b.writeAnalogOutput(1, 4048);
        Assert.assertEquals("SET_OUT 0x300 D 4 127 4048\n", b.msgOutputRequest());

        b.resetOutputChangedDetection();
        Assert.assertEquals("", b.msgOutputRequest());

        b.resetOutputChangedDetection();
        b.writeDigitalOutput(2, false);
        Assert.assertEquals("SET_OUT 0x300 D 0 - -\n", b.msgOutputRequest());
    }

    @Test
    public void testOutput2() {
        DmmatBoardWithMsg b = new DmmatBoardWithMsg(0, 0x300, "test", false, false, false, true);
        Assert.assertEquals("SET_OUT 0x300 D - 0 0\n", b.msgOutputRequest());
    }

}
