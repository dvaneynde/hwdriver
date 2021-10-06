package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.diamondsys.DmmatBoard;

/**
 * Digital and Analog input measurements received from Hardware Driver, from one
 * DMMAT board.
 * <p>
 * <code>INP_D address:int valD:int valA1:int valA2:int</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address in hex format with '0x' prefix.
 * <dt>valD
 * <dd>digital input value; if digital input not used then contains '-'
 * <dt>valA1, valA2
 * <dd>analog sample value, in decimal format, of first and second analog input channel respectively; if analog input not used then contains '-'
 * </dl>
 * <p>
 * For example:<br>
 * <code>INP_D 0x300 6 - 240</code><br>
 * means that board at address 0x300 digital input channels 1 and 2 are on (hence 6), analog channel 0 was not requested and analog channel1 measures 240.
 * 
 * @author dirk vaneynde
 * 
 */
public class DmmatBoardWithMsg extends DmmatBoard implements IBoardMessaging {

    static Logger log = LoggerFactory.getLogger(DmmatBoardWithMsg.class);

    // For driver
    private final static char BOARDTYPE_DMMAT = 'D';

    /**
     * Constructor.
     * 
     * @see DmmatBoard#DmmatBoard(int, int, String, boolean, boolean, boolean, boolean)
     */
    public DmmatBoardWithMsg(int boardNr, int address, String description) {
        this(boardNr, address, description, true, true, true, true);
    }

    /**
     * Constructor.
     * 
     * @see DmmatBoard#DmmatBoard(int, int, String, boolean, boolean, boolean, boolean)
     */
    public DmmatBoardWithMsg(int boardNr, int address) {
        this(boardNr, address, DEFAULT_DESCRIPTION, true, true, true, true);
    }

    /**
     * Constructor.
     * 
     * @see DmmatBoard#DmmatBoard(int, int, String, boolean, boolean, boolean[], boolean[])
     */
    public DmmatBoardWithMsg(int boardNr, int address, String description, boolean digiInEnabled, boolean digiOutEnabled, boolean anaInEnabled,
            boolean anaOutEnabled) throws IllegalArgumentException {
        super(boardNr, address, description, digiInEnabled, digiOutEnabled, anaInEnabled, anaOutEnabled);
    }

    @Override
    public String msgInitBoard() {
        return GeneralMsg.constructBoardInit(BOARDTYPE_DMMAT, getAddress());
    }

    /**
     * Request input state of one DMMAT board. The request sent is:
     * <p>
     * <code>REQ_INP 0x300 D YYN</code><br>
     * where 0x3oo is the address in hex, 'D' denotes DMMAT. YYN specifies which input is really requested.
     * <ol>
     * <li>First Y or N indicates if digital input (8 channels) has to be read and returned.</li>
     * <li>Second and third Y or N indicate if analog input channel 0 or 1 respectively have to be read and returned.</li>
     * </ol>
     * 
     * @return line to be sent to hardware driver.
     */
    @Override
    public String msgInputRequest() {
        boolean enabled = false;
        char[] requestDetail = { 'N', 'N', 'N' };
        if (digiIn != null) {
            requestDetail[0] = 'Y';
            enabled = true;
        }
        if (anaIns != null) {
            for (int i = 0; i < anaIns.length; i++) {
                requestDetail[i + 1] = 'Y';
                enabled = true;
            }
        }
        if (enabled)
            return String.format("REQ_INP 0x%x D %s\n", getAddress(), new String(requestDetail));
        else
            return "";
    }

    @Override
    public void parseInputResult(String sCmd, int address, StringTokenizer st) throws ParseException {
        if (!sCmd.equals("INP_D")) {
            log.warn("Unexpected line received from driver, command=" + sCmd + ", address=" + getAddress() + ". remaining tokens=" + st.toString());
            return;
        }
        String s = st.nextToken();
        if (!s.equals("-"))
            digiIn.updateInputFromHardware(Integer.parseInt(s));
        for (int i = 0; i < DmmatBoard.NR_ANALOG_IN_CHANNELS; i++) {
            s = st.nextToken();
            if (!s.equals("-"))
                anaIns[i].updateInputFromHardware(Integer.parseInt(s));
        }
    }

    /**
     * Message sent to Hardware Driver, to set a digital output and/or zero or more analog output values for
     * a given address (=board).
     * <p>
     * For digital output, see {@link GeneralMsg#constructSetDO(int, char, int)}.
     * <p>
     * <code>SET_AO address:int (channel:byte value:int)+\n</code>
     * <p>
     * <dl>
     * <dt>address
     * <dd>i/o address of physical board, in hex format with '0x' prefix.
     * <dt>channel
     * <dd>output channel on that board, typically in range [0..7] or [0..15]
     * <dt>value
     * <dd>analog value to set. Note that Java has 4 bytes here, sufficient for all needs.
     * </dl>
     * <p>
     * Note that the board type does not have to be sent, this should already have been done via {@link Msg2InitBoard} and must be retained by the Hardware
     * Driver.
     */
    @Override
    public String msgOutputRequest() {
        if (!outputStateHasChanged())
            return "";
        boolean outputEnabled = false;
        StringBuffer sb = new StringBuffer();

        if ((digiOut != null) && (digiOut.outputStateHasChanged())) {
            sb.append(' ').append(digiOut.getValue());
            outputEnabled = true;
        } else {
            sb.append(" -");
        }
        if (anaOuts != null) {
            for (int i = 0; i < anaOuts.length; i++) {
                if (anaOuts[i].outputStateHasChanged()) {
                    sb.append(' ').append(anaOuts[i].getValue());
                    outputEnabled = true;
                } else {
                    sb.append(" -");
                }
            }
        }

        if (outputEnabled)
            return String.format("SET_OUT 0x%x D%s\n", getAddress(), sb.toString());
        else
            return "";
    }
}
