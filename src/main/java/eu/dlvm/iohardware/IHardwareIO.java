package eu.dlvm.iohardware;

/**
 * Abstraction of the I/O Hardware.
 * It abstracts hardware input and output channels, a channel being digital
 * (0/1) or analog (0..n).
 * <p>
 * Because communication with the real hardware often happens via TCP/IP and C
 * code, inputs and outputs are buffered here. Only when calling
 * {@link #refreshInputs()} and {@link #refreshOutputs()} the inputs and outputs
 * will be in line with the real hardware.
 * <p>
 * A channel is identified by {@link LogCh}, essentially an integer number.
 * Channel numbering needs not be consecutive. Input channels and output
 * (logical) channels are different; so channel '0' can be used twice, once for
 * input and once for output.
 * <p>
 * Typical sequence:
 * <ol>
 * <li>{@link #initialize()}</li>
 * <li>{@link #refreshInputs()}, then 0 or more {@link #readAnalogInput(LogCh)}
 * or {@link #readDigitalInput(LogCh)}</li>
 * <li>then 0 or more {@link #writeDigitalOutput(LogCh, boolean)} or
 * {@link #writeAnalogOutput(LogCh, int)}, followed by {@link #refreshOutputs()}
 * </li>
 * <li>back to step 2...</li>
 * </ol>
 * 
 * @author Dirk Vaneynde
 */

public interface IHardwareIO {

	/**
	 * Connect to hardware driver and initialize hardware.
	 */
	public void initialize();

	/**
	 * Reads all hardware inputs, refreshing local buffer.
	 */
	public void refreshInputs();

	/**
	 * Writes all hardware outputs, getting them in line with local buffer.
	 */
	public void refreshOutputs();

	/**
	 * Stop hardware and disconnect from hardware driver.
	 */
	public void stop();
	
	/**
	 * Returns the state of a digital input.
	 * 
	 * @param channel
	 * @return On (true) or off.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public boolean readDigitalInput(LogCh channel)
			throws IllegalArgumentException;

	/**
	 * Returns the measured value of given channel.
	 * 
	 * @param channel
	 * @return Value of input channel.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public int readAnalogInput(LogCh channel) throws IllegalArgumentException;

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
	public void writeDigitalOutput(LogCh channel, boolean value)
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
	public void writeAnalogOutput(LogCh channel, int value)
			throws IllegalArgumentException;

}
