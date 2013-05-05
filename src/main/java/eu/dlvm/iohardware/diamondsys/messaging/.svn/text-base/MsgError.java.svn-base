package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Error message received from Hardware Driver.
 * <p>
 * <code>ERROR text:string </code>
 * <p>
 * <code>text</code> contains the error message.
 * 
 * @author dirk vaneynde
 * 
 */
public class MsgError extends MsgFromHw {

	public static final String NAME = "ERROR";
	private String detail;

	/**
	 * Only exists for testing purposes at current.
	 */
	public MsgError() {
	}

	/**
	 * Only exists for testing purposes at current.
	 */

	public MsgError(String detail) {
		this.detail = detail;
	}

	/**
	 * @return Error message received.
	 */
	public String getDetail() {
		return detail;
	}

	@Override
	public String asWireString() {
		return "ERROR " + detail + '\n';
	}

	@Override
	protected void parse(StringTokenizer st) throws ParseException {
		detail = st.nextToken("\n").substring(1);
	}

}
