package eu.dlvm.domotics.sensors;

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

	private static Logger log = Logger.getLogger(FrequencyGauge.class);

	private double frequency = 0.0;
	private boolean lastInputval; // last measured input value
	private long timeLastOffInput; // last time that input value read was 0

	private boolean doMeans;
	private int idxLastMeasure, cyclesToMeasure;
	private long measures[];

	// private double avgFreq;

	/**
	 * Use for immediate frequency only. No average frequencies are kept.
	 */
	public FrequencyGauge() {
		this(0);
	}

	/**
	 * Use for both immediate and average frequency measurements.
	 * 
	 * @param cyclesPerAveragePeriod
	 *            Number of cycles to measure per average.
	 */
	public FrequencyGauge(int cyclesPerAveragePeriod) {
		super();
		lastInputval = false;
		timeLastOffInput = 0L;
		doMeans = (cyclesPerAveragePeriod != 0);
		if (doMeans) {
			cyclesToMeasure = cyclesPerAveragePeriod;
			idxLastMeasure = -1;
			measures = new long[cyclesToMeasure];
		}
		log.info("Frequency gauge configured: means calculated from " + cyclesPerAveragePeriod + " cycles per clock tick (if 0 then immediate frequency");
	}

	/**
	 * Frequency measured; will be zero before first full cycle has been
	 * measured.
	 * <p>
	 * Throws exception if {{@link #isInitialized()} = false.
	 * 
	 * @return the frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @return Average frequency over number of cycles specified in constructor
	 *         {@link #FrequencyGauge(int)}.
	 */
	public double getAvgFreq() {
		if (!doMeans)
			return frequency;
		// throw new
		// RuntimeException("Gauge was not configured to calculate average freauencies.");
		return calcMeans();
	}

	/**
	 * As long as this gauge is not 'ready' the output of
	 * {@link #getFrequency()} or {@link #getAvgFreq()} are meaningless.
	 * <p>
	 * At least one cycle off-on-off must have been read; also, if the very
	 * first input value is a 'on' this is ignored, the gauge waits for the
	 * first 'off'.
	 * 
	 * @return whether gauge is ready and has meaningful frequencies
	 */
	public boolean isReady() {
		return (timeLastOffInput != 0L);
	}

	/**
	 * Samples inputval to determine frequency. Frequency is the inverse of the
	 * period of one 'cycle'. A cycle is measured as an inputval that changed
	 * from off-on-off, or false-true-false.
	 * 
	 * @param currentTimeMs
	 *            Timestamp in milliseconds.
	 * @param inputval
	 *            Measured input value.
	 */
	public void sample(long currentTimeMs, boolean inputval) {
		if (timeLastOffInput == 0) {
			// initialization takes as long as we do not read an 'off' input value
			if (!inputval) {
				lastInputval = inputval;
				timeLastOffInput = currentTimeMs;
				log.debug("FrequencyGauge initialized, time=" + timeMsFormat(currentTimeMs));
			}
		}
		else if (inputval != lastInputval) {
			lastInputval = inputval;
			if (!inputval) {
				long delta = currentTimeMs - timeLastOffInput;
				// freq = 1 / dt(s) = 1 / (dtInMs / 1000) = 1000 / dtInMs
				frequency = 1000.00 / (delta);
				if (doMeans) {
					idxLastMeasure = (++idxLastMeasure) % cyclesToMeasure;
					measures[idxLastMeasure] = delta;
				}
				timeLastOffInput = currentTimeMs;
				log.debug("FrequencyGauge sampled, currentTime=" + timeMsFormat(currentTimeMs) + ", delta=" + delta + "ms., freq=" + frequency + ", mean freq=" + calcMeans());
			}
		}
	}

	private double calcMeans() {
		if (!doMeans)
			return -1;
		long sum = 0L;
		for (int i = 0; i < cyclesToMeasure; i++)
			sum += measures[i];
		double avgFreq = 1000.0 / (sum / cyclesToMeasure);
		return avgFreq;
	}

	public static String timeMsFormat(long timeMs) {
		return "time=" + (timeMs / 1000) % 1000 + "s. " + timeMs % 1000 + "ms.";
	}

	/*
	 * Alternatieve berekening voor calcMeans, sneller bij grote arrays.
	 */
	// private long avgTotal = 0L;
	//
	// private void calcMeans2(long delta) {
	// idxLastMeasure = (++idxLastMeasure) % cyclesToMeasure;
	// avgTotal -= measures[idxLastMeasure];
	// measures[idxLastMeasure] = delta;
	// avgTotal += delta;
	// avgFreq = 1000.0 / (avgTotal / cyclesToMeasure);
	// }

}
