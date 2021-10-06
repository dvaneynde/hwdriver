package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;
import eu.dlvm.iohardware.diamondsys.TestUtils;

public class TestOpalmmWithMsg {

    @Test
    public void testInitBoard() {
        IBoardMessaging b = new OpalmmBoardWithMsg(0, 0x310);
        Assert.assertEquals("BOARD_INIT O 0x310\n", b.msgInitBoard());
    }

    @Test
    public void testRequestInput0() {
        IBoardMessaging b = new OpalmmBoardWithMsg(0, 0x300);
        Assert.assertEquals("REQ_INP 0x300 O\n", b.msgInputRequest());
    }

    @Test
    public void testRequestInput1() {
        IBoardMessaging b = new OpalmmBoardWithMsg(0, 0x310, "test", false, false);
        Assert.assertEquals("", b.msgInputRequest());
    }

    @Test
    public void testInputValsOpalmm() {
        try {
            String line = "INP_O 0x300 200\n";
            Board b = new OpalmmBoardWithMsg(0, 0x300);
            int address = Utils.parseLine(line, (IBoardMessaging) b);

            Assert.assertEquals(address, b.getAddress());
            int boardstate = TestUtils.digitalInputAsByte(b);
            Assert.assertEquals(~200 & 0xFF, boardstate);
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testOutput0() {
        IBoardMessaging b = new OpalmmBoardWithMsg(0, 0x300, "", true, false);
        Assert.assertEquals("", b.msgOutputRequest());
    }

    @Test
    public void testOutput1() {
        OpalmmBoardWithMsg b = new OpalmmBoardWithMsg(0, 0x300, OpalmmBoard.DEFAULT_DESCRIPTION, true, true);
        b.init();
        Assert.assertEquals("SET_OUT 0x300 O 0\n", b.msgOutputRequest());
    
        b.writeDigitalOutput( 2, true);
        Assert.assertEquals("SET_OUT 0x300 O 4\n", b.msgOutputRequest());
    
        for (int i = 0; i < 8; i++)
            b.writeDigitalOutput( i, true);
        Assert.assertEquals("SET_OUT 0x300 O 255\n", b.msgOutputRequest());
    
        b.resetOutputChangedDetection();
        Assert.assertEquals("", b.msgOutputRequest());
    
        b.resetOutputChangedDetection();
        b.writeDigitalOutput( 6, false);
        Assert.assertEquals("SET_OUT 0x300 O 191\n", b.msgOutputRequest());
    
        b.resetOutputChangedDetection();
        for (int i = 0; i < 8; i++)
            b.writeDigitalOutput( i, false);
        Assert.assertEquals("SET_OUT 0x300 O 0\n", b.msgOutputRequest());
    }

}
