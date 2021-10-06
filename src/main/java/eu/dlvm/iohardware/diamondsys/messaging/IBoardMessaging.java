package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

public interface IBoardMessaging {

    /**
     * Message sent to driver to initialize one board.
     * 
     * @return line to be sent to driver
     */
    public String msgInitBoard();

    /**
     * Message sent to driver to request input values.
     * 
     * @return line to be sent to driver
     */
    public String msgInputRequest();

    /**
     * Parses response received after {@link #msgInputRequest()}.
     * 
     * @param sCmd
     *            command, i.e. first token in line received
     * @param address
     *            address of board
     * @param stringtokenizer
     *            on lines received from driver; the first token, the command, is already stripped
     * @throws ParseException
     *             if wire string has error; error positions is not always set.
     */
    /**
     * @param sCmd
     * @param address
     * @param st
     * @throws ParseException
     */
    public void parseInputResult(String sCmd, int address, StringTokenizer st) throws ParseException;

    /**
     * Message sent to driver to set outputs.
     * 
     * @return line to be sent to driver
     */
    public String msgOutputRequest();
}
