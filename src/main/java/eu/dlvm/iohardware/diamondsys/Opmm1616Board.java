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
     * Digital in channel, or <code>null</code> if not {@link #isDigiInEnabled()}.
     */
    protected DigiIn[] digiIn = null;

    /**
     * Digital out channel, or <code>null</code> if not {@link #isDigiOutEnabled()}.
     */
    protected DigiOut[] digiOut = null;

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
    public Opmm1616Board(int boardNumber, int address, String description, boolean digiInEnabled, boolean digiOutEnabled) {
        super(boardNumber, address, description);
        if (digiInEnabled) {
            digiIn = new DigiIn[2];
            digiIn[0] = new DigiIn();
            digiIn[1] = new DigiIn();
            // TODO kan onderstaande update niet in init()? zoals digiOut?
            digiIn[0].updateInputFromHardware(255);
            digiIn[1].updateInputFromHardware(255);
        }
        if (digiOutEnabled) {
            digiOut = new DigiOut[2];
            digiOut[0] = new DigiOut();
            digiOut[1] = new DigiOut();
        }
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
        if (digiOut != null)
            for (int i = 0; i < digiOut.length; i++)
                digiOut[i].init();
    }

    @Override
    public boolean outputStateHasChanged() {
        if (digiOut != null)
            for (int i = 0; i < digiOut.length; i++)
                if (digiOut[i].outputStateHasChanged())
                    return true;
        return false;
    }

    @Override
    public void resetOutputChangedDetection() {
        if (digiOut != null)
            for (int i = 0; i < digiOut.length; i++)
                digiOut[i].resetOutputChangedDetection();
    }

    @Override
    public boolean readDigitalInput(int channel) {
        return digiIn[channel / 8].getValue(channel % 8);
    }

    @Override
    public void writeDigitalOutput(int channel, boolean value) {
        digiOut[channel / 8].setOutputForChannel(value, channel % 8);
    }

    @Override
    public int readAnalogInput(int channel) {
        throw new RuntimeException("No analog input for this board.");
    }

    @Override
    public void writeAnalogOutput(int channel, int value) throws IllegalArgumentException {
        throw new RuntimeException("No analog input for this board.");
    }

    @Override
    public String toString() {
        String s = "Opmm1616Board " + super.toString() + "\n       " + Util.CHANNEL_STATE_HEADER + "\ninput=\n";
        if (digiIn == null)
            s += "DISABLED";
        else
            for (int i = 0; i < digiIn.length; i++)
                s += Util.prettyByte(digiIn[i].getValue());
        s += "\noutput=\n";
        if (digiOut == null)
            s += "DISABLED";
        else
            for (int i = 0; i < digiOut.length; i++)
                s += Util.prettyByte(digiOut[i].getValue());
        return s;
    }
}
