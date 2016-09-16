package eu.dlvm.domotics.sensors;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measures frequency of a pulse train.
 * <p>
 * Make sure that sampling frequency, see {{@link #sample(long, boolean)}, is at
 * least twice the maximum frequency of the signal.
 * 
 * @author Dirk Vaneynde
 * 
 */
public class FrequencyGauge {

	private static final Logger log = LoggerFactory.getLogger(FrequencyGauge.class);
	private static final Logger dumpFreq = LoggerFactory.getLogger("FREQ");
	private int idxLastSample, nrSamplesPerMeasurement;
	private Sample samples[];

	private int testCtr;

	private static class Sample {
		public Sample() {
			time = -1L;
		}

		long time;
		boolean value;

		@Override
		public String toString() {
			return "Sample [time=" + time % 10000 + ", value=" + value + "]";
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param nrSamplesPerAverage
	 *            Number of samples to store to measure average.
	 * @param nrMeasuresPerAverage
	 */
	public FrequencyGauge(int nrSamplesPerAverage) {
		if (nrSamplesPerAverage < 2)
			throw new IllegalArgumentException("Need at least 2 samples.");
		// TODO aantal cycles zou in verband moeten staan met sample frequentie
		// en thresholds
		this.nrSamplesPerMeasurement = nrSamplesPerAverage;
		idxLastSample = -1;
		samples = new Sample[nrSamplesPerAverage];
		for (int i = 0; i < nrSamplesPerAverage; i++) {
			samples[i] = new Sample();
		}
		log.info("Frequency gauge configured: means calculated from " + nrSamplesPerAverage + " samples.");
	}

	/**
	 * Measured frequency over {@link #getNrSamplesPerAverage()} last samples.
	 * <p>
	 * Note that if less than {@link #getNrSamplesPerAverage()} are measured
	 * since startup, 0.0 is returned.
	 * 
	 * @return frequence or 0.0
	 */
	public double getMeasurement() {
		testCtr++;
		if (samples[nrSamplesPerMeasurement - 1].time == -1L) {
			if (testCtr > 100)
				throw new RuntimeException("Unexpected !" + this.toString());
			return 0.0;
		}
		double avgFreq = 0.0;
		int nrTransitions = 0;

		int idx = (idxLastSample + 1) % nrSamplesPerMeasurement;
		Sample last = samples[idx];
		boolean firstTransitionFound = false;
		do {
			idx = (idx + 1) % nrSamplesPerMeasurement;
			if (samples[idx].value != last.value) {
				/*
				 * if (!firstTransitionFound) { last = samples[idx];
				 * firstTransitionFound = true; } else {
				 */ double freq = 1000.0 / (2.0 * ((double) Math.abs(samples[idx].time - last.time)));
				avgFreq += freq;
				nrTransitions++;
				last = samples[idx];
				//}
			}
		} while (idx != idxLastSample);
		if (nrTransitions == 0)
			avgFreq = 0.0;
		else
			avgFreq = avgFreq / (double) nrTransitions;
		if (dumpFreq.isDebugEnabled())
			dumpFreq.debug(dumpSamples(avgFreq));
		return avgFreq;
	}

	/**
	 * @return Number of last samples stored to calculate average frequency.
	 */
	public int getNrSamplesPerAverage() {
		return nrSamplesPerMeasurement;
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
		idxLastSample = (idxLastSample + 1) % nrSamplesPerMeasurement;
		samples[idxLastSample].time = currentTimeMs;
		samples[idxLastSample].value = inputval;
	}

	public static String timeMsFormat(long timeMs) {
		return "time=" + (timeMs / 1000) % 1000 + "s. " + timeMs % 1000 + "ms.";
	}

	public String dumpSamples(double avgFreq) {
		StringBuffer sb = new StringBuffer(String.format("%5f - ", avgFreq));
		int idx = (idxLastSample + 1) % nrSamplesPerMeasurement;
		do {
			sb.append(String.format("%5d %1d|", samples[idx].time % 10000, samples[idx].value ? 1 : 0));
			idx = (idx + 1) % nrSamplesPerMeasurement;
		} while (idx != idxLastSample);
		return sb.toString();
	}

	@Override
	public String toString() {
		return "FrequencyGauge [ nrSamplesPerAverage=" + nrSamplesPerMeasurement + ", idxLastSample=" + idxLastSample
				+ ", samples=" + Arrays.toString(samples) + ", testCtr=" + testCtr + "]";
	}

}
