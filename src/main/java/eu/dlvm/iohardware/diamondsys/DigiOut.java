package eu.dlvm.iohardware.diamondsys;

/**
 * Base abstraction of 8 digital output channels.
 * 
 * @author dirk vaneynde
 */
public class DigiOut {

	/*
	 * All states are ints, even if they are (unsigned) bytes. Java only has
	 * signed bytes, so to avoid sign promotion we use ints.
	 */
	private int outputState;
	private int prevOutputState;

	/**
	 * Constructor. Output is set to all channels 'false', and
	 * {@link #outputStateHasChanged()} will return true.
	 */
	public DigiOut() {
		init();
	}

	/**
	 * Sets all output channels used to initial value - being 0 (off). Also,
	 * {@link #outputStateHasChanged()} will return true - so that a real update
	 * will be triggered.
	 */
	public void init() {
		outputState = 0;
		prevOutputState = -1;
	}
	
	/**
	 * Set output channel.
	 * 
	 * @param val
	 *            true is on, false is off
	 * @param channel
	 *            Range [0..7], 0 is least significant bit in byte set on board.
	 */
	public void setOutputForChannel(boolean val, int channel) {
		int mask = 1 << channel;
		if (val)
			outputState |= mask;
		else
			outputState &= (~mask);
	}

	/**
	 * @return Encoded output state of 8 digital channels in one byte. Return
	 *         type is an int, but it only contains a byte.
	 */
	public int getOutput() {
		return outputState;
	}

	/**
	 * @return Whether output state has changed, since last
	 *         {@link #resetOutputChangedDetection()}.
	 */
	public boolean outputStateHasChanged() {
		return (outputState != prevOutputState);
	}

	/**
	 * After this call, all calls to {{@link #outputStateHasChanged()} will
	 * return false, until the output state has changed for at least one
	 * channel.
	 */
	public void resetOutputChangedDetection() {
		this.prevOutputState = this.outputState;
	}

	/**
	 * Unit Test only. Sets output state as if coming from board. Limit yourself
	 * to one byte !
	 */
	void unitTestOnly_setOutputState(int state) {
		this.outputState = state;
	}

}
