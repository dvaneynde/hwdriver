package eu.dlvm.iohardware.diamondsys.deprecated;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

/**
 * Message sent to Hardware Driver, to initialize one board. A board is of a
 * given type and is identified by a memory address.
 * <p>
 * <code>BOARD_INIT boardtype:char address:int \n</code>
 * <p>
 * <dl>
 * <dt>boardtype
 * <dd>character specifying type of DiamondSys board
 * <dt>address
 * <dd>i/o address of physical board, in hex format with '0x' prefix.
 * </dl>
 * <p>
 * For example,<br/>
 * <code>BOARD_INIT D 0x380</code> initializes a board of type 'D' on address
 * 0x300.
 * 
 * @author dirk vaneynde
 * 
 */
@Deprecated
public class Msg2InitBoard extends Msg2Hw {

	private char boardtype;
	private int address;

	/**
	 * Create one message line to intialize a board, for input and/or output.
	 */
	public Msg2InitBoard(Board board) {
		if (board instanceof OpalmmBoard)
			this.boardtype = 'O';
		else
			this.boardtype = 'D';
		this.address = board.getAddress();
	}

	@Override
	public String convert4Wire() {
		StringBuffer sb = new StringBuffer("BOARD_INIT ").append(boardtype)
				.append(' ').append(String.format("0x%x", address));
		sb.append('\n');
		return sb.toString();
	}
}
