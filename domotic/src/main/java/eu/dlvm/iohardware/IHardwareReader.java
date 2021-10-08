package eu.dlvm.iohardware;

/**
 * Hardware inputs.
 *
 * @author Dirk Vaneynde
 */

public interface IHardwareReader {

	/**
	 * Reads all hardware inputs, refreshing local buffer.
	 */
	public void refreshInputs();

	/**
	 * Returns the state of a digital input.
	 * 
	 * @param channel
	 * @return On (true) or off.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public boolean readDigitalInput(String channel)
			throws IllegalArgumentException;

	/**
	 * Returns the measured value of given channel.
	 * 
	 * @param channel
	 * @return Value of input channel.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public int readAnalogInput(String channel) throws IllegalArgumentException;

}
