package eu.dlvm.iohardware.diamondsys.factories;

import java.util.List;

import eu.dlvm.iohardware.LogCh;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.ChannelType;
import eu.dlvm.iohardware.diamondsys.DmmatBoard;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

/**
 * TODO moet naar subpackage factories.
 * 
 * @author dirk
 * 
 */
public class HwConfigurator implements IBoardFactory {

	@Override
	public void configure(List<Board> boards, ChannelMap map) {
		boards.add(new OpalmmBoard(0, 0x380, "First opalmm board."));
		for (int i = 0; i < 8; i++) {
			map.add(new LogCh(i), new FysCh(0, ChannelType.DigiOut, i));
			map.add(new LogCh(i + 8), new FysCh(0, ChannelType.DigiIn, i));
		}
		boards.add(new DmmatBoard(1, 0x300, "First dmmat board."));
		for (int i = 0; i < 8; i++) {
			map.add(new LogCh(i + 16), new FysCh(1, ChannelType.DigiOut, i));
			map.add(new LogCh(i + 24), new FysCh(1, ChannelType.DigiIn, i));
		}
	}
}
