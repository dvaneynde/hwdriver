package eu.dlvm.domotica.blocks.concrete;

import org.junit.Assert;
import org.junit.Test;

public class TestFreqGauge {

	private static final boolean T = true;
	private static final boolean F = false;

	@Test
	public void testFreq1() {
		FrequencyGauge fg = new FrequencyGauge();
		boolean[] vals = new boolean[] { F, T, F };
		double[] freqs = new double[] { 0, 0, 50 };
		long time = 1000L;
		for (int i = 0; i < vals.length; i++) {
			fg.sample(time, vals[i]);
			Assert.assertTrue("time=" + time + ", i=" + i, equald2(freqs[i], fg.getFrequency()));
			time += 10;
		}
	}

	@Test
	public void testFreq2() {
		FrequencyGauge fg = new FrequencyGauge();
		boolean[] vals = new boolean[] { T, F, T, T, F, F, T, T, T, F, F, F, T, T, F };
		double[] freqs = new double[] { 0.00, 0.00, 0.00, 0.00, 33.33, 33.33, 33.33, 33.33, 33.33, 20.00, 20.00, 20.00, 20.00,
				20.00, 20.00 };
		Assert.assertEquals(vals.length, freqs.length);

		long time = 1000L;
		for (int i = 0; i < vals.length; i++) {
			fg.sample(time, vals[i]);
			Assert.assertTrue("time=" + time + ", i=" + i, equald2(freqs[i], fg.getFrequency()));
			time += 10;
		}
	}

	private static boolean equald2(double a, double b) {
		return ((long) (a * 100) == (long) (b * 100));
	}
}
