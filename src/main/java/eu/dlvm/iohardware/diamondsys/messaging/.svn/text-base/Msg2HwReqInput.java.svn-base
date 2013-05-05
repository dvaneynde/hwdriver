package eu.dlvm.iohardware.diamondsys.messaging;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.DmmatBoard;
import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

/**
 * Request input state of one (1) OPALMM or DMMAT board.
 * <p>
 * For an OPALMM board the request is:
 * <p>
 * <code>REQ_INP 0x300 O</code><br>
 * where 0x300 is the address in hex.
 * <p>
 * For a DMMAT board a request is:
 * <p>
 * <code>REQ_INP 0x300 D YYN</code><br>
 * where 0x3oo is the address in hex, 'D' denotes DMMAT. YYN specifies which
 * input is really requested.
 * <ol>
 * <li>First Y or N indicates if digital input (8 channels) has to be read and
 * returned.</li>
 * <li>Second and third Y or N indicate if analog input channel 0 or 1
 * respectively have to be read and returned.</li>
 * </ol>
 * 
 * @author dirk vaneynde
 */
public class Msg2HwReqInput extends Msg2Hw {
	private String wiretext;

	public Msg2HwReqInput() {
		wiretext = "";
	}

	/**
	 * Message for Hardware Driver to request state of enabled inputs.
	 * 
	 * @param board
	 *            Board to request.
	 * @return <code>this</code> to enable chaining
	 */
	public Msg2HwReqInput construct(Board board) {
		if (board instanceof OpalmmBoard)
			return construct((OpalmmBoard) board);
		else if (board instanceof DmmatBoard)
			return construct((DmmatBoard) board);
		else
			throw new RuntimeException("Unexpected board type.");
	}

	/**
	 * Message for Hardware Driver to request state of enabled inputs.
	 * 
	 * @param board
	 * @return <code>this</code> to enable chaining
	 */
	public Msg2HwReqInput construct(OpalmmBoard board) {
		if (board.digiIn() != null) {
			wiretext = String.format("REQ_INP 0x%x O\n", board.getAddress());
		}
		return this;
	}

	/**
	 * Message for Hardware Driver to request state of enabled inputs.
	 * 
	 * @param board
	 * @return <code>this</code> to enable chaining
	 */
	public Msg2HwReqInput construct(DmmatBoard board) {
		boolean enabled = false;
		char[] requestDetail = { 'N', 'N', 'N' };
		if (board.digiIn() != null) {
			requestDetail[0] = 'Y';
			enabled = true;
		}
		for (int i = 0; i < DmmatBoard.ANALOG_IN_CHANNELS; i++) {
			if (board.anaIn(i) != null) {
				requestDetail[i + 1] = 'Y';
				enabled = true;
			}
		}
		if (enabled) {
			wiretext = String.format("REQ_INP 0x%x D %s\n", board.getAddress(),
					new String(requestDetail));
		}
		return this;
	}

	@Override
	public String convert4Wire() {
		return wiretext;
	}

}
