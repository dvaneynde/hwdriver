package eu.dlvm.iohardware.diamondsys.messaging;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.DigiOut;
import eu.dlvm.iohardware.diamondsys.DmmatBoard;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

/**
 * Constructs message to set outputs of one given board.
 * <p>
 * Multiple board types are supported, see constructors.
 * <p>
 * In general, if a board has no outputs enabled (and thus is only used to
 * gather inputs), or the outputs have not changed, the wire string (see
 * {@link #convert4Wire()}) is the empty string.
 * 
 * @author dirk vaneynde
 * 
 */
public class Msg2SetOutputs extends Msg2Hw {

	private String wireString;

	public Msg2SetOutputs() {
		wireString = null;
	}

	/**
	 * Accepts any {@link Board} polymorphic. See other construct() methods for
	 * specifics.
	 * 
	 * @param board
	 *            An OPALMM or DMMAT board.
	 * @return itself to allow chaining.
	 */
	public Msg2SetOutputs construct(Board board) {
		if (board instanceof OpalmmBoard) {
			return construct((OpalmmBoard) board);
		} else if (board instanceof DmmatBoard) {
			return construct((DmmatBoard) board);
		} else {
			throw new RuntimeException("Unexpected Board type.");
		}
	}

	/**
	 * Constructs Msg2Hw message to set outputs for an OPALMM board.
	 * <p>
	 * An example is:<br>
	 * <code>SET_OUT 0x300 O 5</code><br>
	 * where 0x300 is the address, the 'O' is fixed and stands for OPALMM, 5
	 * means digital channels 0 and 2 are to be set to ON.
	 * <p>
	 * Only if {@link OpalmmBoard#digiOut()} is enabled and its state has
	 * changed ( {@link DigiOut#outputStateHasChanged()}) a message string is
	 * created; otherwise an empty string is generated.
	 * <p>
	 * Note that {@link DigiOut} is unchanged, including state change detection.
	 * 
	 * @param board
	 *            OPALMM board.
	 * @return itself to allow chaining.
	 */
	public Msg2SetOutputs construct(OpalmmBoard board) {
		if (board.outputStateHasChanged()) {
			wireString = String.format("SET_OUT 0x%x O %d\n",
					board.getAddress(), board.digiOut().getOutput());
		} else {
			wireString = "";
		}
		return this;
	}

	/**
	 * Constructs Msg2Hw message to set outputs for a DMMAT board.
	 * <p>
	 * An example is <br>
	 * <code>SET_OUT 0x300 D 5 - 178</code><br>
	 * meaning:
	 * <ul>
	 * <li>0x300 is the address of the board,</li>
	 * <li>'D' indicates DMMAT,</li>
	 * <li>5 means that digital output channels 0 and 2 are to be set to ON,</li>
	 * <li>'-' means that analog output channel 0 is not used, or <strong>its
	 * state has not changed</strong></li>
	 * <li>178 means that analog output channel 1 is to be set to value 178.</li>
	 * </ul>
	 * <p>
	 * At least one output channel must be enabled (e.g.
	 * {@link DmmatBoard#digiIn()} not null), and must have changed (see
	 * {@link DigiOut#outputStateHasChanged()} and similar for analog);
	 * otherwise the message is an <strong>empty string</strong>.
	 * <p>
	 * Note that state change detection on the digital or analog channels is not
	 * reset.
	 * 
	 * @param board
	 *            OPALMM board.
	 * @return itself to allow chaining.
	 */
	public Msg2SetOutputs construct(DmmatBoard board) {
		boolean outputEnabled = false;
		StringBuffer sb = new StringBuffer();

		if ((board.digiOut() != null)
				&& (board.digiOut().outputStateHasChanged())) {
			sb.append(' ').append(board.digiOut().getOutput());
			outputEnabled = true;
		} else {
			sb.append(" -");
		}
		for (int i = 0; i < DmmatBoard.ANALOG_OUT_CHANNELS; i++) {
			if ((board.anaOut(i) != null)
					&& (board.anaOut(i).outputStateHasChanged())) {
				sb.append(' ').append(board.anaOut(i).getValue());
				outputEnabled = true;
			} else {
				sb.append(" -");
			}
		}

		if (outputEnabled) {
			wireString = String.format("SET_OUT 0x%x D%s\n",
					board.getAddress(), sb.toString());
		} else {
			wireString = "";
		}
		return this;
	}

	@Override
	public String convert4Wire() {
		return wireString;
	}

}
