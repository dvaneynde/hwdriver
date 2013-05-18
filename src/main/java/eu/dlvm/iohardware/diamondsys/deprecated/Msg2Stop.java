package eu.dlvm.iohardware.diamondsys.deprecated;

/**
 * Command to send to Hardware Driver.
 * <p>
 * <code>STOP</code>
 * <p>
 * Hardware Driver must quit.
 * 
 * @author dirk vaneynde
 * 
 */
@Deprecated
public class Msg2Stop extends Msg2Hw {
	@Override
	public String convert4Wire() {
		return "QUIT\n";
	}

}
