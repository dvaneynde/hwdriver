package eu.dlvm.iohardware.diamondsys.messaging;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;

public class TestFindBoardByAddress {

    class HwDriverChannelMock implements IHwDriverChannel {
        @Override
        public void connect() {
        }

        @Override
        public List<String> sendAndRecv(String stringToSend, Reason reason) {
            return null;
        }

        @Override
        public void disconnect() {
        }
    }

    class BoardsFactory implements IBoardFactory {
        List<Board> boards = new ArrayList<Board>();
        ChannelMap map = new ChannelMap();

        @Override
        public void configure(List<Board> boards, ChannelMap map) {
            boards.add(new OpalmmBoardWithMsg(0, 0x380));
            boards.add(new DmmatBoardWithMsg(1, 0x300));
            boards.add(new Opmm1616BoardWithMsg(2, 0x310));
            boards.add(new Opmm1616BoardWithMsg(3, 0x270));
        }

    }

    @Test
    public void test() {
        BoardsFactory bf = new BoardsFactory();
        HardwareIO hw = new HardwareIO(bf, new HwDriverChannelMock());

        assertEquals(null, hw.findBoardBy(0x26f));
        assertEquals(0x270, hw.findBoardBy(0x270).getAddress());
        assertEquals(0x270, hw.findBoardBy(0x271).getAddress());
        assertEquals(0x270, hw.findBoardBy(0x2ff).getAddress());
        assertEquals(0x300, hw.findBoardBy(0x300).getAddress());
        assertEquals(0x300, hw.findBoardBy(0x300).getAddress());
        assertEquals(0x300, hw.findBoardBy(0x30f).getAddress());
        assertEquals(0x310, hw.findBoardBy(0x310).getAddress());
        assertEquals(0x310, hw.findBoardBy(0x311).getAddress());
        assertEquals(0x310, hw.findBoardBy(0x320).getAddress());
        assertEquals(0x380, hw.findBoardBy(0x400).getAddress());

    }
}
