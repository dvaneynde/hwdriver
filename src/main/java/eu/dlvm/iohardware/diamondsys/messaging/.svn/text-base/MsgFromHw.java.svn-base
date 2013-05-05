package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Abstraction of <strong>one</strong> message received from Hardware Driver. A
 * message corresponds to a '\n' terminated line.
 * <p>
 * Each message received has the following format:<br/>
 * <code>MSG_ID (PARM)*\n</code> </br> where MSG_ID defines what the message is
 * all about.
 * 
 * @author dirk vaneynde
 */
public abstract class MsgFromHw {

	/**
	 * @return Message as it appears on the wire.
	 */
	public abstract String asWireString();

	/**
	 * Takes a tokenizer on the received message, which already processed the
	 * initial MSG_ID, and parses the rest if any, and sets message specific
	 * properties.
	 * 
	 * @param st
	 *            tokenizer on wire string.
	 * @throws ParseException
	 *             if wire string has error; error positions is not always set.
	 */
	protected abstract void parse(StringTokenizer st) throws ParseException;

	@Override
	/**
	 * Return wire string format, easy for debugging since it has all information.
	 */
	public String toString() {
		return asWireString();
	}

}
