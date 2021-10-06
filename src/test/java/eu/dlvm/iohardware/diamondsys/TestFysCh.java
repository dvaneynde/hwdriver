package eu.dlvm.iohardware.diamondsys;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.ChannelType;

public class TestFysCh {
	@Test
	public final void testEquals() {
		FysCh f1 = new FysCh(5,ChannelType.AnlgOut,3);
		FysCh f2 = new FysCh(5,ChannelType.AnlgOut,3);
		Assert.assertTrue (f1 != f2);
		Assert.assertEquals(f1, f2);
	}

}
