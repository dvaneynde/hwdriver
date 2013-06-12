package eu.dlvm.iohardware.diamondsys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TestOpmm1616 {

    @Test
    public void testInitAndOutputAndChangeDetection() {
        Opmm1616Board b = new Opmm1616Board(0, 0x300, "Test opalmm init().", true, true);
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut[0].getValue());
        assertEquals(0, b.digiOut[1].getValue());

        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        b.writeDigitalOutput((byte) 0, true);
        assertTrue(b.outputStateHasChanged());
        assertEquals(1, b.digiOut[0].getValue());
        assertEquals(0, b.digiOut[1].getValue());

        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        assertEquals(1, b.digiOut[0].getValue());
        assertEquals(0, b.digiOut[1].getValue());

        b.init();
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut[0].getValue());
        assertEquals(0, b.digiOut[1].getValue());
    }

    @Test
    public void testOutput2bytes() {
        Opmm1616Board b = new Opmm1616Board(0, 0x300, "Test opalmm output part.", true, true);
        b.init();
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut[0].getValue());
        assertEquals(0, b.digiOut[1].getValue());
        b.resetOutputChangedDetection();
        assertFalse(b.outputStateHasChanged());
        int val = 0;
        for (int i = 0; i < 16; i++) {
            b.writeDigitalOutput((byte) i, true);
            val++;
            assertTrue(b.outputStateHasChanged());
            assertEquals(val, b.digiOut[0].getValue() + (b.digiOut[1].getValue() << 8));
            b.resetOutputChangedDetection();
            val <<= 1;
        }
    }

    @Test
    public void testInput() {
        Opmm1616Board b = new Opmm1616Board(0, 0x300, "Test opalmm input part.", true, true);
        int input = TestUtils.digitalInputAsByte(b);
        assertEquals(0, input);

        b.digiIn[0].updateInputFromHardware(0);
        b.digiIn[1].updateInputFromHardware(75 & 0xff);
        input = TestUtils.digitalInputAsByte(b, 0, 7);
        assertEquals(0, input);
        input = TestUtils.digitalInputAsByte(b, 8, 15);
        assertEquals(75, input);
    }
}
