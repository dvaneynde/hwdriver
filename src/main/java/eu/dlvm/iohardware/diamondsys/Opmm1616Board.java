package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.ChannelType;
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
            digiIn[0] = new DigiIn(false);
            digiIn[1] = new DigiIn(false);
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
    public boolean isEnabled(ChannelType ct) {
        switch (ct) {
        case DigiIn:
            return digiIn != null;
        case DigiOut:
            return digiOut != null;
        default:
            return false;
        }
    }

    @Override
    public int nrOfChannels(ChannelType ct) {
        int nr = 0;
        if ((ct == ChannelType.DigiOut) && (digiOut != null))
            nr = 16;
        else if ((ct == ChannelType.DigiIn) && (digiIn != null))
            nr = 16;
        return nr;
    }

    @Override
    public String toString() {
        String s = "Opmm1616Board " + super.toString() + "\n       " + Util.WORD_HEADER + "\n input=";
        if (digiIn == null)
            s += "DISABLED";
        else {
            s += Util.prettyByte(digiIn[1].getValue());
            s += Util.prettyByte(digiIn[0].getValue());
        }
        s += "\noutput=";
        if (digiOut == null)
            s += "DISABLED";
        else {
            s += Util.prettyByte(digiOut[1].getValue());
            s += Util.prettyByte(digiOut[0].getValue());
        }
        return s;
    }
}
