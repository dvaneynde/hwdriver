package eu.dlvm.domotica;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.iohardware.LogCh;

public class TestLogCh {
	@Test
	public void TestEquals() {
		for (int i=0; i<255; i++) {
			LogCh c1 = new LogCh(i);
			LogCh c2 = new LogCh(i);
			Assert.assertEquals(c1, c2);
			Assert.assertTrue(c1.hashCode() == c2.hashCode());
		}
	}
	
	@Test
	public void TestNotEquals() {
		LogCh c1 = new LogCh(23);
		LogCh c2 = new LogCh(15);
		Assert.assertFalse(c1.equals(c2));
	}

}
