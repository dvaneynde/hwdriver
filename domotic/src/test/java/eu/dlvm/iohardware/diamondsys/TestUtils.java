package eu.dlvm.iohardware.diamondsys;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.Util;

public class TestUtils {

	@Test
	public void testChannelStateAsString() {
		byte state = (byte) (128 + 32 + 1);
		String s = Util.prettyByte(state);
		System.out.println(s);
		Assert.assertEquals("1 0 1 0 0 0 0 1 ", s);

		state = (byte) (32 + 1);
		s = Util.prettyByte(state);
		System.out.println(s);
		Assert.assertEquals("0 0 1 0 0 0 0 1 ", s);
	}

	@Test
	public void testUnsigned() {
		byte b;
		int i;

		b = 0x20;
		i = Util.unsign(b);
		Assert.assertEquals(0x20, i);
		
		b=0x0;
		i = Util.unsign(b);
		Assert.assertEquals(0, i);
		
		b=-128;
		i = Util.unsign(b);
		Assert.assertEquals(128, i);
		
		b=-0x80;
		i = Util.unsign(b);
		Assert.assertEquals(128, i);
		
		b=-100;
		i = Util.unsign(b);
		Assert.assertEquals(156, i);
		
	}

    public static boolean valueOf(int channel, int stateAsByte) throws IllegalArgumentException {
        if (channel < 0 || channel > 7)
            throw new IllegalArgumentException("Channel must be in [0..7].");
        int mask = 1 << channel;
        int result = stateAsByte & mask;
        return (result > 0);
    }

    public static int digitalInputAsByte(Board b) {
        return digitalInputAsByte(b,0,7);
    }
    
    public static int digitalInputAsByte(Board b, int lowerChannel, int higherChannel) {
        int boardstate = 0;
        for (int i = higherChannel; i >= lowerChannel; i--) {
            if (b.readDigitalInput((byte) i))
                boardstate += 1;
            boardstate <<= 1;
        }
        boardstate >>= 1;
        return boardstate;
    }
	
}
