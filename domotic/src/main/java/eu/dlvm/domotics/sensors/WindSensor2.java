package eu.dlvm.domotics.sensors;

import eu.dlvm.iohardware.IHardwareReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoLevel;

/**
 * As soon as wind speed (actually rotations per second of a gauge) is above
 * {@link #getFreqThreshold()} the state becomes {@link States#ALARM}. If then
 * it stays for at least {@link #getWaittimeToResetAlarmMs()} seconds below
 * {@link #getFreqThreshold()} - during which the state is
 * {@link States#WAIT2SAFE} - state goes back to {@link States#SAFE}.
 * <p>
 * The current state is sent every {@link #DEFAULT_REPEAT_EVENT_MS} ms. resent -
 * for safety.
 * 
 * @author Dirk Vaneynde
 */
public class WindSensor2 extends Sensor implements IUiCapableBlock {

	private static final int DEFAULT_REPEAT_EVENT_MS = 1000;
	private static final Logger log = LoggerFactory.getLogger(WindSensor2.class);
	private static final Logger logwind = LoggerFactory.getLogger("WIND");
	private int freqThreshold;
	private long waittimeToResetAlarmMs, waittimeToRaiseAlarmMs;
	private States state;
	private double freq;
	private long timeCurrentStateStarted, timeSinceLastEventSent;

	FrequencyGauge gauge; // package scope for unit tests

	// ===================
	// PUBLIC API

	public static enum States {
		SAFE, ALARM, WAIT2SAFE, WAIT2ALARM
	};

	/**
	 * @param name
	 * @param description
	 * @param channel
	 * @param builder
	 * @param freqThreshold
	 * @param waitTimeToResetAlarm
	 *            Unit is seconds.
	 */
	public WindSensor2(String name, String description, String channel, IHardwareReader reader, IDomoticBuilder builder, int freqThreshold,
                       int waitTimeToResetAlarm, int waitTimeToRaiseAlarm) {
		this(name, description, null, channel, reader, builder, freqThreshold, waitTimeToResetAlarm, waitTimeToRaiseAlarm);
	}

	public WindSensor2(String name, String description, String ui, String channel, IHardwareReader reader, IDomoticBuilder builder,
			int freqThreshold, int waittimeToResetAlarm, int waittimeToRaiseAlarm) {
		super(name, description, ui,  channel, reader, builder);
		this.freqThreshold = freqThreshold;
		this.waittimeToResetAlarmMs = waittimeToResetAlarm * 1000L;

		// TODO 25 below as parameter?
		this.gauge = new FrequencyGauge(25);

		timeCurrentStateStarted = timeSinceLastEventSent = 0L;
		this.state = States.SAFE;
		// this.lastFrequency = 0L;
		log.info("Windsensor '" + getName() + "' configured: freq.threshold=" + getFreqThreshold()
				+ ", lowTimeToResetAlert=" + getWaittimeToResetAlarmMs() / 1000.0 + "(s).");
	}

	/**
	 * @return frequency threshold
	 */
	public int getFreqThreshold() {
		return freqThreshold;
	}

	/**
	 * @return the wait time to reset alert, in milliseconds
	 */
	public long getWaittimeToResetAlarmMs() {
		return waittimeToResetAlarmMs;
	}

	/**
	 * @return the wait time to raise alarm, in milliseconds
	 */
	public long getWaittimeToRaiseAlarmMs() {
		return waittimeToRaiseAlarmMs;
	}

	/**
	 * @return the state
	 */
	public States getState() {
		return state;
	}

	public int getFreqTimesHundred() {
		return (int) (freq * 100);
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoLevel uiInfo = new UiInfoLevel(this, getState().toString(), getFreqTimesHundred(), 0, 0,
				getFreqThreshold() * 100, 2500);
		return uiInfo;
	}

	@Override
	public void update(String action) {
	}

	@Override
	public String toString() {
		return "WindSensor [freqThreshold=" + freqThreshold + ", waittimeToRaiseAlarmMs=" + waittimeToRaiseAlarmMs
				+ ", waittimeToResetAlarmMs=" + waittimeToResetAlarmMs + ", state=" + state + ", freq=" + freq + "]";
	}

	// ===================
	// INTERNAL API

	// for logging only, 4 times per second
	private int infoCounter = 0;

	@Override
	public void loop(long currentTime) {
		boolean newInput = getHwReader().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		freq = gauge.getMeasurement();

		if (logwind.isDebugEnabled())
			logwind.debug(
					state + "\ttime=\t" + (currentTime / 1000) % 1000 + "." + currentTime % 1000 + "s.\tfreq=" + freq);
		else if ((currentTime % 1000) >= (infoCounter * 250)) {
			if (freq > 0.0)
				logwind.info(state + "\ttime=\t" + (currentTime / 1000) % 1000 + "." + currentTime % 1000 + "s.\tfreq="
						+ freq);
			infoCounter = (infoCounter + 1) % 4;
		}

		switch (state) {
		case SAFE:
			if (freq >= getFreqThreshold()) {
				state = States.WAIT2ALARM;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName() + "' goes from SAFE	to SAFE2ALARM state: freq=" + freq);
			}
			break;
		case WAIT2ALARM:
			if (freq < getFreqThreshold()) {
				state = States.SAFE;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName() + "' goes from SAFE2ALARM to SAFE state: freq=" + freq);
			} else if ((currentTime - timeCurrentStateStarted) > getWaittimeToRaiseAlarmMs()) {
				state = States.ALARM;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName() + "' notifies ALARM event because higher than threshold for long enough.");
				notifyListeners(EventType.ALARM);
			}
			break;
		case ALARM:
			if (freq < getFreqThreshold()) {
				state = States.WAIT2SAFE;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM to ALARM_BUT_LOW: freq=" + freq + " <= threshold="
						+ getFreqThreshold());
			}
			break;
		case WAIT2SAFE:
			if (freq >= getFreqThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM_BUT_LOW to ALARM: freq=" + freq + " > thresholdHigh="
						+ getFreqThreshold());
			} else if ((currentTime - timeCurrentStateStarted) > getWaittimeToResetAlarmMs()) {
				state = States.SAFE;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName()
						+ "' notifies SAFE event because wind has been low long enough: freq=" + freq + " < threshold="
						+ getFreqThreshold());
				notifyListeners(EventType.SAFE);
			}
			break;
		default:
			break;
		}

		if (currentTime - timeSinceLastEventSent >= DEFAULT_REPEAT_EVENT_MS) {
			notifyListeners((state == States.ALARM || state == States.WAIT2SAFE) ? EventType.ALARM : EventType.SAFE);
			timeSinceLastEventSent = currentTime;
		}
	}
}
