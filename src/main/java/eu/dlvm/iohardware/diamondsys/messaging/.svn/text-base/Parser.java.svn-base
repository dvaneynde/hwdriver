package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;
/**
 * Parses a message line received from Hardware Driver.
 * @author dirk vaneynde
 *
 */
public class Parser {

	/**
	 * Parses messages received from Hardware Driver.
	 * @param line One received line.
	 * @return Subclass of {@link MsgFromHw} corresponding to MSG_ID, i.e. the first token in the line.
	 * @throws ParseException Empty line, or MSG_ID not recognized.
	 */
	public static MsgFromHw parseFromWire(String line) throws ParseException {
		StringTokenizer st = new StringTokenizer(line);
		MsgFromHw result = null;
		if (st.hasMoreTokens()) {
			String cmd = st.nextToken();
			if (cmd.equals(MsgInputsOpalmm.NAME)) {
				result = new MsgInputsOpalmm();
			} else if (cmd.equals(MsgInputsDmmat.NAME)) {
				result = new MsgInputsDmmat();
			} else if (cmd.equals(MsgError.NAME)) {
				result = new MsgError();
			}
		}
		if (result == null)
			throw new ParseException("Could not parse line from hardware driver: '" + line + "'", 0);
		result.parse(st);
		return result;
	}

}
