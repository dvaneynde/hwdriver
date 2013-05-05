package eu.dlvm.iohardware.diamondsys;

/**
 * Represents one Analog Input Channel on a {@link Board}.
 * 
 * @author dirk vaneynde
 */
public class AnaInChannel {

	/*
	 * All states are ints, even if they are (unsigned) bytes. Java only has
	 * signed bytes, so to avoid sign promotion we use ints.
	 */
	private int inputValue = 0;

	public AnaInChannel() {
	}
	
	/**
	 * @return Actual input value of the board, as communicated by
	 *         last {@link #updateInputFromHardware(int)}.
	 */
	public int getInput() {
		return inputValue;
	}

	/**
	 * To be called by "hardware" driver, to reflect the current input.
	 * @param inputstate Value as gotten from Hardware Driver.
	 */
	public void updateInputFromHardware(int inputValue) {
		this.inputValue = inputValue;
	}

}
