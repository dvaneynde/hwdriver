package eu.dlvm.iohardware.diamondsys;

/**
 * Base abstraction of 8 digital input channels. To be used by delegation in any of the {@link OpalmmBoard} subtypes.
 * 
 * @author dirk vaneynde
 */
class DigiIn {

	/*
	 * All states are ints, even if they are (unsigned) bytes. Java only has
	 * signed bytes, so to avoid sign promotion we use ints.
	 */
	private int inputState = 0;
	private int prevInputState = 0;

	/**
	 * Check whether given input channel is on.
	 * 
	 * @param channel
	 *            Range [0..7], 0 is least significant bit in byte read from
	 *            board.
	 * @return true = on, false = off
	 */
	public boolean getInput(int channel) throws IllegalArgumentException {
		if (channel < 0 || channel > 7)
			throw new IllegalArgumentException("Channel must be in [0..7].");
		int mask = 1 << channel;
		int result = inputState & mask;
		return (result > 0);
	}

	/**
	 * @return Actual input state of the board, i.e. state as communicated by
	 *         last {@link #updateInputFromHardware(int)}. Note that though
	 *         return type is an int, it never is more than one byte !
	 */
	public int getInput() {
		return inputState;
	}

	/**
	 * Indicates whether the board's input state has changed, i.e.
	 * {@link #updateInputFromHardware(int)} has changed the input state, and
	 * {@link #resetInputChangedDetection()} has not yet been called.
	 * 
	 * @param channel
	 * @return input has changed on channel
	 * @throws IllegalArgumentException
	 */
	public boolean inputHasChanged(int channel) throws IllegalArgumentException {
		if (channel < 0 || channel > 7)
			throw new IllegalArgumentException("Channel must be in [0..7].");
		int mask = 1 << channel;
		int xor = prevInputState ^ inputState;
		xor &= mask;
		return (xor != 0);
	}

	/**
	 * After this call, all calls to {{@link #inputHasChanged(int)} will return
	 * false.
	 * <p>
	 * In other words, the 'previous' input state is made equal to the actual
	 * one.
	 */
	public void resetInputChangedDetection() {
		this.prevInputState = inputState;
	}

	/**
	 * To be called by "hardware" driver, to reflect the current input.
	 * <p>
	 * Note that the hardware says "1" for "off", but we reverse that here. So
	 * if parameter "inputstate" has 255, then afterwards { {@link #getInput()}
	 * will return 0.
	 * 
	 * @param inputstate
	 *            state as communicated by hardware. Note that type is an int,
	 *            yet all channels are encoded in just one byte, i.e. only 8
	 *            channels.
	 */
	public void updateInputFromHardware(int inputstate) {
		this.prevInputState = this.inputState;
		this.inputState = ~inputstate;
	}

}
