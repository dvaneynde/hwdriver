package eu.dlvm.iohardware.diamondsys.messaging;

/**
 * Message sent to Hardware Driver, to initialize the hardware.
 * 
 * @author dirk vaneynde
 */
public class Msg2InitHardware extends Msg2Hw {

	/**
	 * Create message line.
	 * 
	 * @param sleepBetweenExchange
	 *            Time in microseconds for Hardware Driver to sleep between
	 *            exchanges.
	 */
	public Msg2InitHardware() {
		super();
	}

	@Override
	public String convert4Wire() {
		return "INIT\n";
	}
}
