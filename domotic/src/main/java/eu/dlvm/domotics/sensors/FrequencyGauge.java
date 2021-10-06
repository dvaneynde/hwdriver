package eu.dlvm.domotics.sensors;

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
	private int nrSamplesPerMeasurement;
	private RingBuffer<Sample> samples;

	private static class Sample {
		public Sample(long time, boolean value) {
			this.time = time;
			this.value = value;
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
		samples = new RingBuffer<Sample>(nrSamplesPerAverage);
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
		double avgFreq = 0.0;
		int nrTransitions = 0;

		Sample last = null;
		boolean gotFirst = false;
		for (Sample sample : samples) {
			if (last == null)
				last = sample;
			else if (sample.value != last.value) {
				if (gotFirst) {
					double freq = 1000.0 / (2.0 * ((double) Math.abs(sample.time - last.time)));
					avgFreq += freq;
					nrTransitions++;
				} else {
					gotFirst = true;
				}
				last = sample;
			}
		}

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
		Sample sample = new Sample(currentTimeMs, inputval);
		samples.add(sample);
	}

	public static String timeMsFormat(long timeMs) {
		return "time=" + (timeMs / 1000) % 1000 + "s. " + timeMs % 1000 + "ms.";
	}

	public String dumpSamples(double avgFreq) {
		StringBuffer sb = new StringBuffer(String.format("%5f - ", avgFreq));
		for (Sample sample : samples) {
			sb.append(String.format("%5d %1d | ", sample.time % 10000, sample.value ? 1 : 0));
		}
		return sb.toString();
	}

}
