package eu.dlvm.domotics.utils;

import org.junit.Test;

import junit.framework.Assert;

public class TestAverageLong {

	@Test
	public void test0() {
		AverageLong avgInt = new AverageLong(10);
		for (int i = 0; i < 10; i++) {
			avgInt.add(i);
		}
		Assert.assertTrue(avgInt.enoughSamples());
		Assert.assertEquals(4.5, avgInt.avgAndClear());
		Assert.assertFalse(avgInt.enoughSamples());
	}

	@Test
	public void test1() {
		AverageLong avgInt = new AverageLong(10);
		for (int i = 0; i < 5; i++) {
			avgInt.add(i * 2);
		}
		Assert.assertFalse(avgInt.enoughSamples());
		try {
			Assert.assertEquals(2, avgInt.avgAndClear());
			Assert.fail("Should throw exception.");
		} catch (IllegalArgumentException e) {
		}
	}
}
