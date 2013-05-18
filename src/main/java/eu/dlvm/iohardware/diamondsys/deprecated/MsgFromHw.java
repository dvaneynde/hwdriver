package eu.dlvm.iohardware.diamondsys.deprecated;

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
@Deprecated
public abstract class MsgFromHw implements IMsgFromHw {

	/* (non-Javadoc)
     * @see eu.dlvm.iohardware.diamondsys.messaging.IMsgFromHw#asWireString()
     */
	@Override
    public abstract String asWireString();

	/* (non-Javadoc)
     * @see eu.dlvm.iohardware.diamondsys.messaging.IMsgFromHw#parse(java.util.StringTokenizer)
     */
	@Override
    public abstract void parse(StringTokenizer st) throws ParseException;

	@Override
	/**
	 * Return wire string format, easy for debugging since it has all information.
	 */
	public String toString() {
		return asWireString();
	}

}
