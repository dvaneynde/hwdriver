package eu.dlvm.iohardware.diamondsys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TestOpalmm {

    @Test
    public void testInitAndOutput() {
        OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm init().", true, true);
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut.getValue());
       
        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        b.writeDigitalOutput((byte) 0, true);
        assertEquals(1, b.digiOut.getValue());

        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        assertEquals(1, b.digiOut.getValue());

        b.init();
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut.getValue());
    }

    @Test
    public void testOutput() {
        OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm output part.", true, true);
        b.init();
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut.getValue());
        b.resetOutputChangedDetection();
        int val = 0;
        for (int i = 0; i < 8; i++) {
            b.writeDigitalOutput((byte) i, true);
            val++;
            assertTrue(b.outputStateHasChanged());
            assertEquals(val, b.digiOut.getValue());
            b.resetOutputChangedDetection();
            val <<= 1;
        }
    }

    @Test
    public void testOutputChangedDetection() {
        OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm output part.", true, true);
        b.init();
        assertTrue(b.outputStateHasChanged());
        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        b.writeDigitalOutput((byte) 5, false);
        assertFalse(b.outputStateHasChanged());
        b.writeDigitalOutput((byte) 5, true);
        assertTrue(b.outputStateHasChanged());
        assertTrue(b.outputStateHasChanged());
        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        assertFalse(b.outputStateHasChanged());
    }

    @Test
    public void testInput() {
        OpalmmBoard b = new OpalmmBoard(0, 0x300, "Test opalmm input part.", true, true);
        int input = TestUtils.digitalInputAsByte(b);
        assertEquals(0, input);

        b.digiIn.updateInputFromHardware(255);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(0, input);

        b.digiIn.updateInputFromHardware(0);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(255, input);

        b.digiIn.updateInputFromHardware(~35 & 0xff);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(35, input);

        b.digiIn.updateInputFromHardware(~217 & 0xff);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(217, input);
    }

}
