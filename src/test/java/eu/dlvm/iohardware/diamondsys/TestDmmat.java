package eu.dlvm.iohardware.diamondsys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TestDmmat {

    @Test
    public void testInit() {
        DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat init().", true, true, true, true);
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut.getValue());

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
        DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat output part.", true, true, true, true);
        b.init();
        assertTrue(b.outputStateHasChanged());
        assertEquals(0, b.digiOut.getValue());

        int val = 0;
        for (int i = 0; i < 8; i++) {
            b.writeDigitalOutput((byte) i, true);
            val++;
            assertTrue(b.outputStateHasChanged());
            assertEquals(val, b.digiOut.getValue());
            b.resetOutputChangedDetection();
            val <<= 1;
        }

        assertFalse(b.outputStateHasChanged());
        b.writeAnalogOutput((byte) 0, 2000);
        assertTrue(b.outputStateHasChanged());
        assertEquals(2000, b.anaOuts[0].getValue());
        assertEquals(0, b.anaOuts[1].getValue());
        assertEquals(255, b.digiOut.getValue());

        b.resetOutputChangedDetection();
        b.writeAnalogOutput((byte) 1, 4000);
        assertTrue(b.outputStateHasChanged());
        assertEquals(2000, b.anaOuts[0].getValue());
        assertEquals(4000, b.anaOuts[1].getValue());
        assertEquals(255, b.digiOut.getValue());

        b.resetOutputChangedDetection();
        b.writeDigitalOutput((byte) 7, true);
        b.writeAnalogOutput((byte) 0, 2000);
        b.writeAnalogOutput((byte) 1, 4000);
        assertFalse(b.outputStateHasChanged());
        assertEquals(2000, b.anaOuts[0].getValue());
        assertEquals(4000, b.anaOuts[1].getValue());
        assertEquals(255, b.digiOut.getValue());

        b.resetOutputChangedDetection();
        b.writeDigitalOutput((byte) 7, false);
        b.writeAnalogOutput((byte) 0, 2000);
        b.writeAnalogOutput((byte) 1, 500);
        assertTrue(b.outputStateHasChanged());
        assertEquals(2000, b.anaOuts[0].getValue());
        assertEquals(500, b.anaOuts[1].getValue());
        assertEquals(127, b.digiOut.getValue());

        b.resetOutputChangedDetection();
        b.writeDigitalOutput((byte) 7, false);
        b.writeAnalogOutput((byte) 0, DmmatBoard.ANALOG_RESOLUTION - 1);
        b.writeAnalogOutput((byte) 1, 500);
        assertTrue(b.outputStateHasChanged());
        assertEquals(DmmatBoard.ANALOG_RESOLUTION - 1, b.anaOuts[0].getValue());
        assertEquals(500, b.anaOuts[1].getValue());
        assertEquals(127, b.digiOut.getValue());
    }

    @Test
    public void testInput() {
        DmmatBoard b = new DmmatBoard(0, 0x300, "Test dmmat input part.", true, true, true, true);
        int input = TestUtils.digitalInputAsByte(b);
        assertEquals(0, input);
        assertEquals(0, b.readAnalogInput((byte) 0));
        assertEquals(0, b.readAnalogInput((byte) 1));

        b.digiIn.updateInputFromHardware(255);
        b.anaIns[0].updateInputFromHardware(2000);
        b.anaIns[1].updateInputFromHardware(DmmatBoard.ANALOG_RESOLUTION - 1);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(255, input);
        assertEquals(2000, b.readAnalogInput((byte) 0));
        assertEquals(DmmatBoard.ANALOG_RESOLUTION - 1, b.readAnalogInput((byte) 1));

        b.digiIn.updateInputFromHardware(35);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(35, input);
        assertEquals(2000, b.readAnalogInput((byte) 0));
        assertEquals(DmmatBoard.ANALOG_RESOLUTION - 1, b.readAnalogInput((byte) 1));

        b.anaIns[0].updateInputFromHardware(0);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(35, input);
        assertEquals(0, b.readAnalogInput((byte) 0));
        assertEquals(DmmatBoard.ANALOG_RESOLUTION - 1, b.readAnalogInput((byte) 1));

        b.anaIns[1].updateInputFromHardware(2879);
        input = TestUtils.digitalInputAsByte(b);
        assertEquals(35, input);
        assertEquals(0, b.readAnalogInput((byte) 0));
        assertEquals(2879, b.readAnalogInput((byte) 1));

    }

}
