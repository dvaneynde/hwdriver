package eu.dlvm.domotics.sensors;

import java.security.acl.LastOwnerException;

import org.apache.log4j.Logger;

/**
 * Measures frequency of a pulse train, both immediate as with
 * {@link #getFrequency()} or over a set of cycles as with {@link #getAvgFreq()}
 * .
 * <p>
 * Immediate frequency is 1 / dT where dT is time of one cycle [off-on-off[.
 * <p>
 * Make sure that sampling frequency, see {{@link #sample(long, boolean)}, is at
 * least twice the maximum frequency of the pulse train.
 * <p>
 * TODO als twee cycles >10% afwijken, een soort alarm geven? Want dat kan
 * waarschijnlijk niet...
 * 
 * @author Dirk Vaneynde
 * 
 */
public class FrequencyGauge {

	private static class Sample {
		public Sample() {
			time = -1L;
		}
		long time;
		boolean value;
		
		@Override
		public String toString() {
			return "Sample [time=" + time + ", value=" + value + "]";
		}
	}

	private static final Logger log = Logger.getLogger(FrequencyGauge.class);
		private int idxLastSample, nrSamplesPerAverage;
	private Sample samples[];

	// private double avgFreq;

	/**
	 * Use for immediate frequency only. No average frequencies are kept.
	 */
	public FrequencyGauge() {
		this(10);
	}

	/**
	 * Use for both immediate and average frequency measurements.
	 * 
	 * @param nrSamplesPerAverage
	 *            Number of cycles to measure per average.
	 */
	public FrequencyGauge(int nrSamplesPerAverage) {
		if (nrSamplesPerAverage<2)
			throw new IllegalArgumentException("Need at least 2 samples.");
		// TODO aantal cycles zou in verband moeten staan met sample frequentie
		// en thresholds
		this.nrSamplesPerAverage = nrSamplesPerAverage;
		idxLastSample = -1;
		samples = new Sample[nrSamplesPerAverage];
		for (int i = 0; i < nrSamplesPerAverage; i++) {
			samples[i] = new Sample();
		}
		log.info("Frequency gauge configured: means calculated from " + nrSamplesPerAverage + " samples.");
	}

	/**
	 * @return Average frequency over number of samples specified in constructor
	 *         {@link #FrequencyGauge(int)}.
	 */
	public double getAvgFreq() {
		if (samples[nrSamplesPerAverage - 1].time == -1L)
			return 0.0;
		double avgFreq = 0.0;
		int nrTransitions = 0;

		int idx = (idxLastSample + 1) % nrSamplesPerAverage;
		Sample last = samples[idx];
		boolean firstTransitionFound = false;
		do {
			idx = (idx + 1) % nrSamplesPerAverage;
			if (samples[idx].value != last.value) {
				if (!firstTransitionFound) {
					last = samples[idx];
					firstTransitionFound = true;
				} else {
					double freq = 1000.0 / (2.0 * ((double) Math.abs(samples[idx].time - last.time)));
					avgFreq += freq;
					nrTransitions++;
					last = samples[idx];
				}
			}
		} while (idx != idxLastSample);
		if (nrTransitions == 0)
			avgFreq = 0.0;
		else
			avgFreq = avgFreq / (double) nrTransitions;
		return avgFreq;
	}

	/**
	 * Takes sample.
	 * 
	 * @param currentTimeMs
	 *            Timestamp in milliseconds.
	 * @param inputval
	 *            Sampled input value.
	 */
	public void sample(long currentTimeMs, boolean inputval) {
		idxLastSample = (++idxLastSample) % nrSamplesPerAverage;
		samples[idxLastSample].time = currentTimeMs;
		samples[idxLastSample].value = inputval;
	}

	public static String timeMsFormat(long timeMs) {
		return "time=" + (timeMs / 1000) % 1000 + "s. " + timeMs % 1000 + "ms.";
	}

}
