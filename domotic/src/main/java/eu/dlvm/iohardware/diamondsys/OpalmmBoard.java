package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.Util;

/**
 * Abstraction of a Diamond OPALMM-16 board. It has 8 digital input channels and 8
 * digital output channels.
 * <p>
 * Beware: On OPALMM an "on" input (current) is a 0, an "off" input (no current) is a 1. Users of this see a positive logical state, hence "on" = 1.
 * 
 * @author dirk vaneynde
 */
public class OpalmmBoard extends Board {
    public static final String DEFAULT_DESCRIPTION = "Diamond Systems opal-mm, 8 channel digital i/o.";

    /**
     * Digital in channel, or <code>null</code> if not {@link #isDigiInEnabled()}.
     */
    protected DigiIn digiIn = null;

    /**
     * Digital out channel, or <code>null</code> if not {@link #isDigiOutEnabled()}.
     */
    protected DigiOut digiOut = null;

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
    public OpalmmBoard(int boardNumber, int address, String description, boolean digiInEnabled, boolean digiOutEnabled) {
        super(boardNumber, address, description);
        if (digiInEnabled) {
            digiIn = new DigiIn();
            digiIn.updateInputFromHardware(255);
        }
        if (digiOutEnabled)
            digiOut = new DigiOut();
    }

    @Override
    public void init() {
        if (digiOut != null) {
            digiOut.init();
        }
    }

    @Override
    public boolean outputStateHasChanged() {
        return (digiOut != null) && (digiOut.outputStateHasChanged());
    }

    @Override
    public void resetOutputChangedDetection() {
        if (digiOut != null)
            digiOut.resetOutputChangedDetection();
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
        throw new RuntimeException("No analog input for this board.");
    }

    @Override
    public void writeAnalogOutput(int channel, int value) throws IllegalArgumentException {
        throw new RuntimeException("No analog output for this board.");
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
            nr = 8;
        else if ((ct == ChannelType.DigiIn) && (digiIn != null))
            nr = 8;
        return nr;
    }

    @Override
    public String toString() {
        return "OpalmmBoard " + super.toString() + "\n       " + Util.BYTE_HEADER + "\n input="
                + (digiIn == null ? "DISABLED" : Util.prettyByte(digiIn.getValue())) + "\noutput="
                + (digiOut == null ? "DISABLED" : Util.prettyByte(digiOut.getValue()));
    }
}
