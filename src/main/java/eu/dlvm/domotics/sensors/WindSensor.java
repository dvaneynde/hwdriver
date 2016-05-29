package eu.dlvm.domotics.sensors;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.LogCh;

/**
 * TODO document
 * 
 * @author Dirk Vaneynde
 */
public class WindSensor extends Sensor {

	private static final Logger log = Logger.getLogger(WindSensor.class);
	private static final Logger logwind = Logger.getLogger("WIND");
	private int highFreqThreshold;
	private int lowFreqThreshold;
	private long highTimeBeforeAlertMs;
	private long lowTimeToResetAlertMs;
	private Set<IAlarmListener> listeners = new HashSet<>();
	private States state;
	private long timeCurrentStateStarted;
	FrequencyGauge gauge; // package scope for unit tests

	// ===================
	// PUBLIC API

	public static enum States {
		NORMAL, HIGH, ALARM, ALARM_BUT_LOW,
	};

	/**
	 * @param name
	 * @param description
	 * @param channel
	 * @param ctx
	 * @param highFreqThreshold
	 * @param lowFreqThreshold
	 * @param highTimeBeforeAlert
	 *            Unit is seconds.
	 * @param lowTimeToResetAlert
	 *            Unit is seconds.
	 */
	public WindSensor(String name, String description, LogCh channel, IDomoticContext ctx, int highFreqThreshold, int lowFreqThreshold, int highTimeBeforeAlert, int lowTimeToResetAlert) {
		super(name, description, channel, ctx);
		if (highFreqThreshold < lowFreqThreshold) // TODO higfreq must be 'far'
													// below 1/(2 xlooptime)
			throw new RuntimeException("Configuration error: highSpeedThreshold must be lower than lowSpeedThreshold.");
		this.highFreqThreshold = highFreqThreshold;
		this.lowFreqThreshold = lowFreqThreshold;
		this.highTimeBeforeAlertMs = highTimeBeforeAlert * 1000L;
		this.lowTimeToResetAlertMs = lowTimeToResetAlert * 1000L;

		this.gauge = new FrequencyGauge(25); // TODO parameter? or calculated
												// from sample time and
												// lowfreq/highfreq?
		this.state = States.NORMAL;
		// this.lastFrequency = 0L;
		log.info("Windsensor '" + getName() + "' configured: highFreq=" + getHighFreqThreshold() + ", highTimeBeforeAlert=" + getHighTimeBeforeAlert() / 1000.0 + "(s), lowFreq="
				+ getLowFreqThreshold() + ", lowTimeToResetAlert=" + getLowTimeToResetAlert() / 1000.0 + "(s).");
	}

	/**
	 * @return high frequency limit
	 */
	public int getHighFreqThreshold() {
		return highFreqThreshold;
	}

	/**
	 * @return low frequency limit
	 */
	public int getLowFreqThreshold() {
		return lowFreqThreshold;
	}

	/**
	 * @return the high time after which to raise alarm, in milliseconds
	 */
	public long getHighTimeBeforeAlert() {
		return highTimeBeforeAlertMs;
	}

	/**
	 * @return the low time to reset alert, in milliseconds
	 */
	public long getLowTimeToResetAlert() {
		return lowTimeToResetAlertMs;
	}

	/**
	 * @return the state
	 */
	public States getState() {
		return state;
	}

	// ===================
	// INTERNAL API

	private int infoCounter = 0;

	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInput = getHw().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		double freq = gauge.getAvgFreq();

		if (infoCounter == 4 && currentTime % 1000 < 250)
			infoCounter = 0;
		if (log.isDebugEnabled())
			logwind.debug(state + "\ttime=\t" + (currentTime / 1000) % 1000 + "s.\t" + currentTime % 1000 + "ms.\tfreq=" + freq);
		else if ((infoCounter < 4) && (currentTime % 1000 >= infoCounter * 250)) {
			logwind.info(state + "\ttime=\t" + (currentTime / 1000) % 1000 + "s.\t" + currentTime % 1000 + "ms.\tfreq=" + freq);
			infoCounter = (infoCounter + 1) % 5;
		}
		switch (state) {
		case NORMAL:
			if (freq >= getHighFreqThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor '" + getName() + "': NORMAL to HIGH: freq=" + freq + " >= thresholdHigh=" + getHighFreqThreshold());
			}
			break;
		case HIGH:
			if (freq < getHighFreqThreshold()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor '" + getName() + "': HIGH to NORMAL : freq=" + freq + " < thresholdHigh=" + getHighFreqThreshold());
			} else if ((currentTime - timeCurrentStateStarted) > getHighTimeBeforeAlert()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor '" + getName() + "' notifies HIGH event because in ALARM state: freq=" + freq + " > thresholdHigh=" + getHighFreqThreshold() + " for more than "
						+ getHighTimeBeforeAlert() / 1000 + "sec.");
				notifyListeners(IAlarmListener.EventType.ALARM);
			}
			break;
		case ALARM:
			if (freq <= getLowFreqThreshold()) {
				state = States.ALARM_BUT_LOW;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM to ALARM_BUT_LOW: freq=" + freq + " <= thresholdLow=" + getLowFreqThreshold());
			}
			break;
		case ALARM_BUT_LOW:
			if (freq >= getHighFreqThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM_BUT_LOW to ALARM: freq=" + freq + " > thresholdHigh=" + getHighFreqThreshold());
			} else if ((currentTime - timeCurrentStateStarted) > getLowTimeToResetAlert()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor '" + getName() + "' notifies LOW event because wind has been low long enough: freq=" + freq + " < thresholdLow=" + getLowFreqThreshold());
				notifyListeners(IAlarmListener.EventType.SAFE);
			}
			break;
		default:
			throw new RuntimeException("Programming Error. Unhandled state.");
		}
	}

	public void registerListener(IAlarmListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners(IAlarmListener.EventType event) {
		for (IAlarmListener l : listeners)
			l.onEvent(this, event);
	}

	@Override
	public String toString() {
		return "WindSensor (" + super.toString() + "NOT IMPLEMENTED YET";
	}
}
