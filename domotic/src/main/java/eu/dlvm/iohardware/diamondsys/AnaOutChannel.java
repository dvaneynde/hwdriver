package eu.dlvm.iohardware.diamondsys;

/**
 * Represents one analog output channel.
 * 
 * @author dirk vaneynde
 * 
 */
public class AnaOutChannel {

	private int resolution;
	private int outputValue;
	private int prevOutputValue;

	/**
	 * Constructor. Output is set to '0', and {@link #outputStateHasChanged()}
	 * will return true.
	 * 
	 * @param resolution
	 *            Resolution of output, i.e. values are within
	 *            [0..resolution-1].<br/>
	 * */
	public AnaOutChannel(int resolution) {
		this.resolution = resolution;
		init();
	}

	/**
	 * @param value
	 *            Sets output value.
	 * @throws InvalidArgument
	 *             Value not within [0..resolution-1].
	 */
	public void setOutput(int value) throws IllegalArgumentException {
		if (value < 0 || value >= resolution)
			throw new IllegalArgumentException("Value not in range [0.." + resolution + "-1]");
		this.outputValue = value;
	}

	/**
	 * @return Output value.
	 */
	public int getValue() {
		return outputValue;
	}

	/**
	 * @return Whether output state has changed, since last
	 *         {@link #resetOutputChangedDetection()}.
	 */
	public boolean outputStateHasChanged() {
		return (outputValue != prevOutputValue);
	}

	/**
	 * After this call, all calls to {{@link #outputStateHasChanged()} will
	 * return false, until the output has changed.
	 */
	public void resetOutputChangedDetection() {
		this.prevOutputValue = this.outputValue;
	}

	/**
	 * Initializes output value - being 0 (off). Also,
	 * {@link #outputStateHasChanged()} will return true - so that a real update
	 * will be triggered.
	 */
	public void init() {
		outputValue = 0;
		prevOutputValue = 1; // force changed notification
	}

}
