package eu.dlvm.iohardware;

import org.junit.*;

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
	
}
