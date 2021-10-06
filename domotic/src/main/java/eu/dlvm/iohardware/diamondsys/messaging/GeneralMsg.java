package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.StringTokenizer;

/**
 * Non board-type specific messages for Hardware Driver, and non-board specific messages.
 */
public class GeneralMsg {

    public static final String MSGPREFIX_ERROR = "ERROR";

    /**
     * @return Message sent to Hardware Driver, to initialize the hardware.
     */
    public static String constructHardwareInit() {
        return "INIT\n";
    }

    /**
     * Message sent to Hardware Driver, to initialize one board. A board is of a
     * given type and is identified by a memory address.
     * <p>
     * <code>BOARD_INIT boardtype:char address:int \n</code>
     * <p>
     * For example,<br/>
     * <code>BOARD_INIT D 0x380</code> initializes a board of type 'D' on address 0x300.
     * 
     * @param boardtype
     *            character specifying type of DiamondSys board
     * @param address
     *            i/o address of physical board, in hex format with '0x' prefix
     * @return string sent to hardware driver
     */
    public static String constructBoardInit(char boardtype, int address) {
        StringBuffer sb = new StringBuffer("BOARD_INIT ").append(boardtype).append(' ').append(String.format("0x%x", address));
        sb.append('\n');
        return sb.toString();
    }

    /**
     * Command sent to Hardware Driver, to quit.
     * <p>
     * <code>QUIT</code>
     */
    public static String constructQUIT() {
        return "QUIT\n";
    }

    /**
     * Testing purposes only.
     * <p>
     * Construct error message, as it would be received from a driver.
     * 
     * @param detail
     *            Detail of the error
     * @return message as sent to driver
     */
    public static String constructERROR(String detail) {
        return MSGPREFIX_ERROR + " " + detail + '\n';
    }

    /**
     * Parses error message from driver.
     * 
     * @param st
     *            StringTokenizer, ERROR already stripped from command line.
     * @return Detail of error.
     */
    public static String parseERROR(StringTokenizer st) {
        return st.nextToken("\n").substring(1);
    }

}
