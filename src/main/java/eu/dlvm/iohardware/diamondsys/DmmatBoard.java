package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.Util;

/**
 * Abstraction of a Diamond DMMAT board. It has 2 analog input channels (12
 * bit), 2 analog output channels (12 bit), 8 digital input channels and 8
 * digital output channels.
 * 
 * @author dirk vaneynde
 * 
 */
public class DmmatBoard extends Board {

	/**
	 * Code identifying DMMAT boards.
	 */
	public final static char BOARDTYPE_DMMAT = 'D';

	public static final String DEFAULT_DESCRIPTION = "Diamond Systems dmmat board: 2 analog i/o, 8 channel digital i/o.";

	/**
	 * Number of analog output channels this board supports.
	 */
	public static final int ANALOG_OUT_CHANNELS = 2;

	/**
	 * Number of analog input channels this board supports.
	 */
	public static final int ANALOG_IN_CHANNELS = 2;

	/**
	 * Resolution of each analog channel. Note that the range to use must be
	 * from 0 to ANALOG_RESOLUTION-1.
	 */
	public static final int ANALOG_RESOLUTION = 4096;

	private DigiIn digiIn;
	private DigiOut digiOut;
	private AnaInChannel[] anaIns = new AnaInChannel[ANALOG_IN_CHANNELS];
	private AnaOutChannel[] anaOuts = new AnaOutChannel[ANALOG_OUT_CHANNELS];

	/**
	 * Constructor.
	 * 
	 * @see DmmatBoard#DmmatBoard(int, int, String, boolean, boolean, boolean[],
	 *      boolean[])
	 */
	public DmmatBoard(int boardNr, int address, String description) {
		this(boardNr, address, description, true, true, new boolean[] { true,
				true }, new boolean[] { true, true });
	}

	/**
	 * Constructor.
	 * 
	 * @see DmmatBoard#DmmatBoard(int, int, String, boolean, boolean, boolean[],
	 *      boolean[])
	 */
	public DmmatBoard(int boardNr, int address) {
		this(boardNr, address, DEFAULT_DESCRIPTION, true, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	/**
	 * Constructor.
	 * 
	 * @param address
	 *            See boardNr.
	 * @param address
	 *            See superclass.
	 * @param description
	 *            See superclass.
	 * @param digiInEnabled
	 *            Whether digital in is enabled.
	 * @param digiOutEnabled
	 *            Whether digital out is enabled.
	 * @param anaInEnabled
	 *            Array must be of size 2, whether analog input channels 0 and
	 *            1 enabled or not.
	 * @param anaOutEnabled
	 *            Array must be of size 2, whether analog output channels 0 and
	 *            1 enabled or not.
	 * @throws IllegalArgumentException
	 *             anaOutEnabled is not of size 2.
	 */
	public DmmatBoard(int boardNr, int address, String description,
			boolean digiInEnabled, boolean digiOutEnabled,
			boolean[] anaInEnabled, boolean[] anaOutEnabled)
			throws IllegalArgumentException {
		super(boardNr, address, description);
		if (digiInEnabled)
			digiIn = new DigiIn();
		if (digiOutEnabled)
			digiOut = new DigiOut();
		for (int i = 0; i < ANALOG_IN_CHANNELS; i++)
			if (anaInEnabled[i])
				this.anaIns[i] = new AnaInChannel();
		for (int i = 0; i < ANALOG_OUT_CHANNELS; i++)
			if (anaOutEnabled[i])
				this.anaOuts[i] = new AnaOutChannel(ANALOG_RESOLUTION);
	}

	@Override
	public void init() {
		if (digiOut() != null) {
			digiOut().init();
		}
		for (int i = 0; i < ANALOG_OUT_CHANNELS; i++) {
			if (anaOuts[i] != null) {
				anaOuts[i].init();
			}
		}
	}

	@Override
	public boolean outputStateHasChanged() {
		return (digiOut != null && digiOut().outputStateHasChanged())
				|| (anaOut(0) != null && anaOut(0).outputStateHasChanged())
				|| (anaOut(1) != null && anaOut(0).outputStateHasChanged());
	}

	@Override
	public void resetOutputChangedDetection() {
		if (digiOut() != null)
			digiOut().resetOutputChangedDetection();
		for (int i = 0; i < ANALOG_OUT_CHANNELS; i++)
			if (anaOut(i) != null)
				anaOut(i).resetOutputChangedDetection();
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

	/**
	 * Access an analog input channel.
	 * 
	 * @param channel
	 *            Selects analog input channel.
	 * @return Analog input channel, or <code>null</code> if this channel is
	 *         not enabled (see constructor).
	 * @throws IllegalArgumentException
	 *             If channel is not 0 or 1.
	 */
	public AnaInChannel anaIn(int channel) {
		checkChannelBounds(channel, ANALOG_IN_CHANNELS);
		return anaIns[channel];
	}

	/**
	 * Access an analog output channel.
	 * 
	 * @param channel
	 *            Selects analog output channel.
	 * @return Analog output channel, or <code>null</code> if this channel is
	 *         not enabled (see constructor).
	 * @throws IllegalArgumentException
	 *             If channel is not 0 or 1.
	 */
	public AnaOutChannel anaOut(int channel) {
		checkChannelBounds(channel, ANALOG_OUT_CHANNELS);
		return anaOuts[channel];
	}

	private void checkChannelBounds(int channel, int numberOfChannels)
			throws IllegalArgumentException {
		if (channel < 0 || channel >= numberOfChannels)
			throw new IllegalArgumentException("channel must be in [0.."
					+ numberOfChannels + "], but was: " + channel);
	}

	@Override
	public String toString() {
		return "DmmatBoard "
				+ super.toString()
				+ "\n       "
				+ Util.CHANNEL_STATE_HEADER
				+ "\n input="
				+ (digiIn != null ? Util.prettyByte(digiIn.getInput())
						: "D I S A B L E D")
				+ "\noutput="
				+ (digiOut != null ? Util.prettyByte(digiOut.getOutput())
						: "D I S A B L E D") + "\n anaIn [0]="
				+ toStringAnaIn(0) + "\t anaIn[1]=" + toStringAnaIn(1)
				+ "\nanaOut [0]=" + toStringAnaOut(0) + "\tanaOut[1]="
				+ toStringAnaOut(1)

		;
	}

	private String toStringAnaIn(int ch) {
		return (anaIn(ch) == null) ? "DIS." : "" + anaIn(ch).getInput();
	}

	private String toStringAnaOut(int ch) {
		return (anaOut(ch) == null) ? "DIS." : "" + anaOut(ch).getValue();
	}

}
