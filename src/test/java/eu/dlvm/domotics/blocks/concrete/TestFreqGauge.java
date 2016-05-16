package eu.dlvm.domotics.blocks.concrete;

import javax.validation.constraints.AssertTrue;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.sensors.FrequencyGauge;

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
		double[] freqs = new double[] { 0.00, 0.00, 0.00, 0.00, 33.33, 33.33, 33.33, 33.33, 33.33, 20.00, 20.00, 20.00, 20.00, 20.00, 20.00 };
		Assert.assertEquals(vals.length, freqs.length);

		long time = 1000L;
		for (int i = 0; i < vals.length; i++) {
			fg.sample(time, vals[i]);
			Assert.assertTrue("time=" + time + ", i=" + i, equald2(freqs[i], fg.getFrequency()));
			time += 10;
		}
	}

	@Test
	public void testSteadyFrequency() {
		boolean[] vals = new boolean[100];
		for (int i = 0; i < vals.length; i++)
			vals[i] = i % 2 == 0;
		FrequencyGauge fg = new FrequencyGauge(7);

		for (int i = 0; i < vals.length; i++)
			fg.sample(i * 500, vals[i]);
		Assert.assertEquals(1.0, fg.getFrequency());
		Assert.assertEquals(1.0, fg.getAvgFreq());

		for (int i = 0; i < vals.length; i++)
			fg.sample(i * 50, vals[i]);
		Assert.assertEquals(10.0, fg.getFrequency());
		Assert.assertEquals(10.0, fg.getAvgFreq());

		for (int i = 0; i < vals.length; i++)
			fg.sample(i * 25, vals[i]);
		Assert.assertEquals(20.0, fg.getFrequency());
		Assert.assertEquals(20.0, fg.getAvgFreq());
	}

	private static boolean equald2(double a, double b) {
		return ((long) (a * 100) == (long) (b * 100));
	}
}
