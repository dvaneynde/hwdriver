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

    protected DigiIn digiIn;
    protected DigiOut digiOut;
    protected AnaInChannel[] anaIns = new AnaInChannel[ANALOG_IN_CHANNELS];
    protected AnaOutChannel[] anaOuts = new AnaOutChannel[ANALOG_OUT_CHANNELS];

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
    public DmmatBoard(int boardNr, int address, String description, boolean digiInEnabled, boolean digiOutEnabled, boolean[] anaInEnabled,
            boolean[] anaOutEnabled) throws IllegalArgumentException {
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
        if (digiOut != null)
            digiOut.init();
        for (int i = 0; i < ANALOG_OUT_CHANNELS; i++) {
            if (anaOuts[i] != null)
                anaOuts[i].init();
        }
    }

    @Override
    public boolean outputStateHasChanged() {
        if (digiOut != null && digiOut.outputStateHasChanged())
            return true;
        for (int i = 0; i < ANALOG_OUT_CHANNELS; i++)
            if (anaOuts[i] != null && anaOuts[i].outputStateHasChanged())
                return true;
        return false;
    }

    @Override
    public void resetOutputChangedDetection() {
        if (digiOut != null)
            digiOut.resetOutputChangedDetection();
        for (int i = 0; i < ANALOG_OUT_CHANNELS; i++)
            if (anaOuts[i] != null)
                anaOuts[i].resetOutputChangedDetection();
    }

    private String toStringAnaIn(int ch) {
        return (anaIns[ch] == null) ? "DIS." : "" + anaIns[ch].getInput();
    }

    private String toStringAnaOut(int ch) {
        return (anaOuts[ch] == null) ? "DIS." : "" + anaOuts[ch].getValue();
    }

    @Override
    public boolean readDigitalInput(int channel) {
        return digiIn.getValue(channel);
    }

    @Override
    public void writeDigitalOutput(int channel, boolean value) {
        digiOut.setOutputForChannel(value, channel);
    }

    @Override
    public int readAnalogInput(int channel) {
        return anaIns[channel].getInput();
    }

    @Override
    public void writeAnalogOutput(int channel, int value) throws IllegalArgumentException {
        anaOuts[channel].setOutput(value);
    }

    @Override
    public String toString() {
        return "DmmatBoard " + super.toString() + "\n       " + Util.CHANNEL_STATE_HEADER + "\n input="
                + (digiIn != null ? Util.prettyByte(digiIn.getValue()) : "D I S A B L E D") + "\noutput="
                + (digiOut != null ? Util.prettyByte(digiOut.getValue()) : "D I S A B L E D") + "\n anaIn [0]=" + toStringAnaIn(0) + "\t anaIn[1]="
                + toStringAnaIn(1) + "\nanaOut [0]=" + toStringAnaOut(0) + "\tanaOut[1]=" + toStringAnaOut(1);
    }

}
