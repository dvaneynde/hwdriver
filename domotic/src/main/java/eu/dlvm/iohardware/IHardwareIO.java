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
 * A channel is identified by {@link String}, essentially an integer number.
 * Channel numbering needs not be consecutive. Input channels and output
 * (logical) channels are different; so channel '0' can be used twice, once for
 * input and once for output.
 * <p>
 * Typical sequence:
 * <ol>
 * <li>{@link #initialize()}</li>
 * <li>{@link #refreshInputs()}, then 0 or more {@link #readAnalogInput(String)}
 * or {@link #readDigitalInput(String)}</li>
 * <li>then 0 or more {@link #writeDigitalOutput(String, boolean)} or
 * {@link #writeAnalogOutput(String, int)}, followed by {@link #refreshOutputs()}
 * </li>
 * <li>back to step 2...</li>
 * </ol>
 * 
 * @author Dirk Vaneynde
 */

public interface IHardwareIO extends IHardwareReader, IHardwareWriter {

	/**
	 * Connect to hardware driver and initialize hardware.
	 */
	public void initialize() throws ChannelFault;

	/**
	 * Stop hardware and disconnect from hardware driver.
	 */
	public void stop();
	
}
