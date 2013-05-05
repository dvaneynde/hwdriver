package eu.dlvm.iohardware.diamondsys.messaging;

/**
 * Abstraction of <strong>one</strong> message sent as a reply to Hardware
 * Driver. A message corresponds to a '\n' terminated line.
 * <p>
 * Each message sent has the following format:<br/>
 * <code>MSG_ID (PARM)*\n</code> </br> where MSG_ID defines what the message is
 * all about.
 * 
 * @author dirk vaneynde
 * 
 */
public abstract class Msg2Hw {

	/**
	 * @return Message string as it appears on the wire.
	 */
	public abstract String convert4Wire();

	/**
	 * @return Same as {#link #conver4Wire}.
	 */
	@Override
	public String toString() {
		return convert4Wire();
	}

}
