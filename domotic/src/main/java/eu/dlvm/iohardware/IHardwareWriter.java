package eu.dlvm.iohardware;

/**
 * Hardware outputs.
 *
 * @author Dirk Vaneynde
 */

public interface IHardwareWriter {

	/**
	 * Writes all hardware outputs, getting them in line with local buffer.
	 */
	public void refreshOutputs();

	/**
	 * Outputs given boolean value on given channel.
	 * 
	 * @param channel
	 *            Logical channel, will be properly converted by hardware
	 *            implementation.
	 * @param value
	 *            Well, on or off.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public void writeDigitalOutput(String channel, boolean value)
			throws IllegalArgumentException;

	/**
	 * Outputs given analog value on given channel.
	 * 
	 * @param channel
	 *            Logical channel, will be properly converted by hardware
	 *            implementation.
	 * @param value
	 *            Any integer, should be in limits of specific hardware - see
	 *            exception.
	 * @throws IllegalArgumentException
	 *             Value not within range, or logical channel does not have a
	 *             physical one.
	 */
	public void writeAnalogOutput(String channel, int value)
			throws IllegalArgumentException;

}
