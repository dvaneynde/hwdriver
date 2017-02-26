package eu.dlvm.domotics.sensors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoOnOff;
import eu.dlvm.domotics.service.uidata.UiInfoOnOffLevel;
import eu.dlvm.iohardware.IHardwareIO;

/**
 * Measures light via analog input (via
 * {@link IHardwareIO#readAnalogInput(String)}) . If this input value is higher
 * than {@link #getHighThreshold()} for {@link #getHighWaitingTime()}
 * milliseconds a {@link States#HIGH} event is sent once. If lower than
 * {@link #getLowThreshold()} for {@link #getThresholdDelayMs()} milliseconds a
 * {@link States#LOW} event is sent once.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor implements IUiCapableBlock {

	private static final int DEFAULT_REPEAT_EVENT_MS = 1000;
	private static final Logger log = LoggerFactory.getLogger(LightSensor.class);
	private static final Logger loglight = LoggerFactory.getLogger("LIGHT");
	private int highThreshold, lowThreshold;
	private long lowToHighDelayMs, highToLowDelayMs;
	private int measuredLevel;
	private States state;
	private long timeCurrentStateStarted, timeSinceLastEventSent;

	// ===================
	// PUBLIC API

	public static enum States {
		LOW, LOW2HIGH_DELAY, HIGH, HIGH2LOW_DELAY,
	};

	public LightSensor(String name, String description, String ui, String channel, IDomoticContext ctx, int lowThreshold, int highThreshold,
			int lowToHighDelaySec, int highToLowDelaySec) throws ConfigurationException {
		super(name, description, ui, channel, ctx);
		if ((highThreshold < lowThreshold) || lowThreshold < 0 || highThreshold < 0) {
			throw new ConfigurationException("Incorrect parameters. Check doc.");
		}
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.lowToHighDelayMs = lowToHighDelaySec * 1000L;
		this.highToLowDelayMs = highToLowDelaySec * 1000L;

		timeCurrentStateStarted = timeSinceLastEventSent = 0L;
		state = States.LOW;
		log.info("LightSensor '" + getName() + "' configured: high=" + getHighThreshold() + ", low=" + getLowThreshold() + ", low to high delay="
				+ getLowToHighDelaySec() + ", high to low delay=" + getHighToLowDelaySec() + " s., channel=" + getChannel());
	}

	/**
	 * Threshold value of input (via
	 * {@link IHardwareIO#readAnalogInput(String)}) for {@link States#HIGH}
	 * state. See also {@link #getHighWaitingTime()}.
	 * 
	 * @return high threshold value
	 */
	public int getHighThreshold() {
		return highThreshold;
	}

	/**
	 * Threshold value of input (via
	 * {@link IHardwareIO#readAnalogInput(String)}) for {@link States#HIGH}
	 * state. See also {@link #getThresholdDelayMs()}.
	 * 
	 * @return low threshold value
	 */
	public int getLowThreshold() {
		return lowThreshold;
	}

	/**
	 * Time that input must remain above {@link #getHighThreshold()} before
	 * actually going {@link States#HIGH}.
	 * 
	 * @return time in seconds
	 */
	public int getLowToHighDelaySec() {
		return (int) lowToHighDelayMs / 1000;
	}

	/**
	 * Time that input must remain below {@link #getLowThreshold()} before
	 * actually going {@link States#LOW}.
	 * 
	 * @return time in seconds
	 */
	public int getHighToLowDelaySec() {
		return (int) lowToHighDelayMs / 1000;
	}

	/**
	 * @return the current state
	 */
	public States getState() {
		return state;
	}

	/**
	 * @return Last level, as given by hardware (thus hardware dependent).
	 */
	public int getLevel() {
		return measuredLevel;
	}

	/**
	 * @return Last level as percentage, where 100 corresponds to
	 *         {@link #getHighThreshold()}, so [0..100..]. So 0..100 or more.
	 */
	public int getLevelAsPct() {
		return measuredLevel * 100 / getHighThreshold();
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOffLevel ui = new UiInfoOnOffLevel(this, getState().toString(), true, getLevel());
		return ui;
	}

	@Override
	public void update(String action) {
	}

	@Override
	public String toString() {
		return "LightSensor [highThreshold=" + highThreshold + ", lowThreshold=" + lowThreshold + ", lowToHighDelayMs=" + lowToHighDelayMs
				+ ", highToLowDelayMs=" + highToLowDelayMs + ", measuredLevel=" + measuredLevel + ", state=" + state + "]";
	}

	// ===================
	// PRIVATE API

	private long lastInfoOnAnalogLevel;

	@Override
	public void loop(long currentTime, long sequence) {
		measuredLevel = getHw().readAnalogInput(getChannel());
		if (currentTime - lastInfoOnAnalogLevel > 1000L) {
			loglight.info("LightSensor " + getName() + ": ana in=" + measuredLevel);
			lastInfoOnAnalogLevel = currentTime;
		}

		switch (state) {
		case LOW:
			if (measuredLevel >= getHighThreshold()) {
				state = States.LOW2HIGH_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case LOW2HIGH_DELAY:
			if (measuredLevel < getHighThreshold()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= lowToHighDelayMs) {
				state = States.HIGH;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("LightSensor -" + getName() + "' notifies HIGH event: light=" + measuredLevel + " > thresholdHigh=" + getHighThreshold());
				notifyListeners(EventType.LIGHT_HIGH);

			}
			break;
		case HIGH:
			if (measuredLevel < getLowThreshold()) {
				state = States.HIGH2LOW_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case HIGH2LOW_DELAY:
			if (measuredLevel > getLowThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= highToLowDelayMs) {
				state = States.LOW;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("LightSensor -" + getName() + "' notifies back to NORMAL event: input=" + measuredLevel + " < thresholdLow=" + getLowThreshold());
				notifyListeners(EventType.LIGHT_LOW);
			}
			break;
		}
		if (currentTime - timeSinceLastEventSent >= DEFAULT_REPEAT_EVENT_MS) {
			notifyListeners((state == States.HIGH || state == States.HIGH2LOW_DELAY) ? EventType.LIGHT_HIGH : EventType.LIGHT_LOW);
			timeSinceLastEventSent = currentTime;
		}
	}
}
