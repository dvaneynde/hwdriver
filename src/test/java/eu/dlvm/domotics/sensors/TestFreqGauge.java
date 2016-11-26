package eu.dlvm.domotics.sensors;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.sensors.FrequencyGauge;

public class TestFreqGauge {

	// BASIC TESTS

	@Test
	public void testTooFewCyclesSampled() {
		new FrequencyGauge(2);
		try {
			new FrequencyGauge(1);
			Assert.fail("Should throw IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
		}
		try {
			new FrequencyGauge(0);
			Assert.fail("Should throw IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
		}
	}

	// SIMPLE FREQ TEST

	@Test
	public void testSteadyFrequency() {
		FrequencyGauge fg = new FrequencyGauge(25);
		long currentTimeMs = 0L;
		long deltaMs = 25;
		// 10 Hz, 25ms sample time --> 1 wave every 100ms so 2 samples low and 2 samples high
		// on then off
		boolean inputval = true;
		for (int i = 0; i < 1; i++) {
			fg.sample(currentTimeMs, inputval);
			currentTimeMs += deltaMs;
			inputval = !inputval;
			fg.sample(currentTimeMs, inputval);
			currentTimeMs += deltaMs;
			fg.sample(currentTimeMs, inputval);
			currentTimeMs += deltaMs;
			inputval = !inputval;
			fg.sample(currentTimeMs, inputval);
			currentTimeMs += deltaMs;
		}
		fg.sample(currentTimeMs, inputval);
		System.out.println("testSteadyFrequency: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(10L, Math.round(fg.getMeasurement()));
	}

	// REAL TESTS

	@Test
	public void testWithOscillator_10Hz_25ms() {
		FrequencyGauge fg = new FrequencyGauge(20);
		oscillateSteady(fg, 10, 25, 1);
		System.out.println("testWithOscillator_10Hz_25ms: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(10L, Math.round(fg.getMeasurement()));
	}

	@Test
	public void testWithOscillator_10Hz_20ms() {
		FrequencyGauge fg = new FrequencyGauge(75);
		oscillateSteady(fg, 10, 20, 2.5);
		System.out.println("testWithOscillator_10Hz_20ms: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(10L, Math.round(fg.getMeasurement()));
	}

	@Test
	public void testWithOscillator_24Hz_20ms_limit() {
		FrequencyGauge fg = new FrequencyGauge(50);
		oscillateSteady(fg, 24, 20, 2);
		System.out.println("testWithOscillator_24Hz_20ms_limit: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(24.0, Math.floor(fg.getMeasurement()));
	}

	@Test
	public void testWithOscillator_1Hz_20ms() {
		FrequencyGauge fg = new FrequencyGauge(50);
		oscillateSteady(fg, 1, 20, 1.5);
		System.out.println("testWithOscillator_1Hz_20ms: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(1L, Math.round(fg.getMeasurement()));
	}

	@Test
	public void testWithOscillator_0point3Hz_20ms() {
		FrequencyGauge fg = new FrequencyGauge(50);
		oscillateSteady(fg, 0.3, 20, 1.5);
		System.out.println("testWithOscillator_0point3Hz_20ms: measured freq=" + fg.getMeasurement());
		Assert.assertEquals(0L, Math.round(fg.getMeasurement()));
	}

	private void oscillateSteady(FrequencyGauge fg, double freq, int samplePeriodMs, double durationSec) {
		double transitionPeriodMs = 1000 / (2.0 * freq);

		int nrTransitions = 1;
		double nextTransitionTime = nrTransitions * transitionPeriodMs;
		boolean val = false;

		long time = 0L;
		while (time <= 1000 * durationSec) {
			if (time >= nextTransitionTime) {
				val = !val;
				nrTransitions++;
				nextTransitionTime = nrTransitions * transitionPeriodMs;
			}
			fg.sample(time, val);
			time += samplePeriodMs;
		}
	}

}
