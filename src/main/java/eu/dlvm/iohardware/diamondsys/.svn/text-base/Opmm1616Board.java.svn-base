package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.Util;

/**
 * Abstraction of a Diamond OPALMM-1616 board. It has 16 digital input channels and 16
 * digital output channels.
 * 
 * @author dirk vaneynde
 */
public class Opmm1616Board extends Board {
	public static final String DEFAULT_DESCRIPTION = "Diamond Systems opal-mm, 8 channel digital i/o.";

	/**
	 * Code identifying OPALMM boards.
	 * TODO Code should just be indication for driver, like 'A'=i/o via direct memory access, 'B' is via driver... ?
	 */
	public final static char BOARDTYPE_OPALMM = 'O';
	
	/**
	 * Digital in channel, or <code>null</code> if not
	 * {@link #isDigiInEnabled()}.
	 */
	private DigiIn digiIn = null;
	
	/**
	 * Digital out channel, or <code>null</code> if not
	 * {@link #isDigiOutEnabled()}.
	 */
	private DigiOut digiOut = null;

	/**
	 * Constructor.
	 * 
	 * @param boardNumber
	 *            See superclass.
	 * @param address
	 *            See superclass.
	 * @param description
	 *            See superclass.
	 * @param digiInEnabled
	 *            Whether digital in is enabled.
	 * @param digiOutEnabled
	 *            Whether digital out is enabled.
	 */
	public Opmm1616Board(int boardNumber, int address, String description,
			boolean digiInEnabled, boolean digiOutEnabled) {
		super(boardNumber, address, description);
		if (digiInEnabled) {
			digiIn = new DigiIn();
			digiIn.updateInputFromHardware(255);
		}
		if (digiOutEnabled)
			digiOut = new DigiOut();
	}

	/**
	 * Constructor. All inputs and outputs enabled.
	 * 
	 * @see OpalmmBoard#OpalmmBoard(int, int, String, boolean, boolean)
	 */
	public Opmm1616Board(int boardNumber, int address, String description) {
		this(boardNumber, address, description, true, true);
	}

	/**
	 * Constructor. All inputs and outputs enabled, default description.
	 * 
	 * @see OpalmmBoard#OpalmmBoard(int, int, String, boolean, boolean)
	 */
	public Opmm1616Board(int boardNumber, int address) {
		this(boardNumber, address, DEFAULT_DESCRIPTION, true, true);
	}

	@Override
	public void init() {
		if (digiOut() != null) {
			digiOut().init();
		}
	}

	@Override
	public boolean outputStateHasChanged() {
		return (digiOut() != null) && (digiOut().outputStateHasChanged());
	}

	@Override
	public void resetOutputChangedDetection() {
		if (digiOut() != null)
			digiOut().resetOutputChangedDetection();
	}

	/**
	 * @return Digital input channels, or <code>null</code> if digital input is
	 *         not used at all. Note that you cannot specify which one of the
	 *         individual channels is to be used.
	 */
	public DigiIn digiIn() {
		return digiIn;
	}

	/**
	 * @return Digital output channels, or <code>null</code> if digital output
	 *         is not used at all. Note that you cannot specify which one of the
	 *         individual channels is to be used.
	 */
	public DigiOut digiOut() {
		return digiOut;
	}

	@Override
	public String toString() {
		return "OpalmmBoard "
				+ super.toString()
				+ "\n       "
				+ Util.CHANNEL_STATE_HEADER
				+ "\n input="
				+ (digiIn == null ? "DISABLED" : Util.prettyByte(digiIn
						.getInput()))
				+ "\noutput="
				+ (digiOut == null ? "DISABLED" : Util.prettyByte(digiOut
						.getOutput()));
	}

}
