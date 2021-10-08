package eu.dlvm.domotics.sensors;

import eu.dlvm.iohardware.IHardwareReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoLevel;
import eu.dlvm.iohardware.IHardwareIO;

/**
 * Measures light via analog input (via
 * {@link IHardwareIO#readAnalogInput(String)}) .
 * <p>
 * If this input value is higher than {@link #getThreshold()} for
 * {@link #getLowToHighDelaySec()} milliseconds a {@link EventType#LIGHT_HIGH}
 * event is sent once. If lower than {@link #getThreshold()} for
 * {@link #getHighToLowDelaySec()()} seconds a {@link EventType#LIGHT_LOW} event
 * is sent once.
 * <p>
 * Initially if light is low we send {@link EventType#LIGHT_LOW} event
 * immediately, otherwise {@link EventType#LIGHT_HIGH}. Of course this might be
 * wrong at that moment, but only problem after startup and temporary problem -
 * after a while correct state is found.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor implements IUiCapableBlock {

	private static final Logger log = LoggerFactory.getLogger(LightSensor.class);
	private static final Logger loglight = LoggerFactory.getLogger("LIGHT");
	private int threshold;
	private long lowToHighDelayMs, highToLowDelayMs;
	private int measuredLevel;
	private States state;
	private long timeCurrentStateStarted, timeSinceLastLightLog;

	// ===================
	// PUBLIC API

	public static enum States {
		LOW, LOW2HIGH_DELAY, HIGH, HIGH2LOW_DELAY,
	};

	public LightSensor(String name, String description, String ui, String channel, IHardwareReader reader, IDomoticBuilder builder, int threshold, int lowToHighDelaySec,
                       int highToLowDelaySec) throws ConfigurationException {
		super(name, description, ui, channel, reader, builder);
		if (threshold < 0 || lowToHighDelaySec < 0 || highToLowDelaySec < 0) {
			throw new ConfigurationException("Threshold and/or delays cannot be negative.");
		}
		this.threshold = threshold;
		this.lowToHighDelayMs = lowToHighDelaySec * 1000L;
		this.highToLowDelayMs = highToLowDelaySec * 1000L;

		timeCurrentStateStarted = timeSinceLastLightLog = -1L;
		state = States.LOW;
		log.info("LightSensor '" + getName() + "' configured: high=" + getThreshold() + ", low to high delay=" + getLowToHighDelaySec()
				+ "sec., high to low delay=" + getHighToLowDelaySec() + " sec., channel=" + getChannel());
	}

	/**
	 * Preset threshold value on input values (via
	 * {@link IHardwareIO#readAnalogInput(String)}) .
	 * 
	 * @return threshold value
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Time that input must remain above {@link #getThreshold()} before actually
	 * going {@link States#HIGH}.
	 * 
	 * @return time in seconds
	 */
	public int getLowToHighDelaySec() {
		return (int) lowToHighDelayMs / 1000;
	}

	/**
	 * Time that input must remain below {@link #getThreshold()} before
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
	 *         {@link #getThreshold()}, so [0..100..]. So 0..100 or more.
	 */
	public int getLevelAsPct() {
		return measuredLevel * 100 / getThreshold();
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoLevel uiInfo = new UiInfoLevel(this, getState().toString(), getLevel(), 0, getThreshold(), getThreshold(), 4000);
		return uiInfo;
	}

	@Override
	public void update(String action) {
	}

	@Override
	public String toString() {
		return "LightSensor [threshold=" + threshold + ", lowToHighDelayMs=" + lowToHighDelayMs + ", highToLowDelayMs=" + highToLowDelayMs + ", measuredLevel="
				+ measuredLevel + ", state=" + state + "]";
	}

	// ===================
	// PRIVATE API

	@Override
	public void loop(long currentTime) {
		measuredLevel = getHwReader().readAnalogInput(getChannel());

		if (timeCurrentStateStarted < 0L) {
			// initialization
			if (measuredLevel < getThreshold()) {
				state = States.LOW;
				notifyLow();
			} else {
				state = States.HIGH;
				notifyHigh();
			}
			timeCurrentStateStarted = currentTime;
		}

		switch (state) {
		case LOW:
			if (measuredLevel >= getThreshold()) {
				state = States.LOW2HIGH_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case LOW2HIGH_DELAY:
			if (measuredLevel < getThreshold()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= lowToHighDelayMs) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
				notifyHigh();

			}
			break;
		case HIGH:
			if (measuredLevel < getThreshold()) {
				state = States.HIGH2LOW_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case HIGH2LOW_DELAY:
			if (measuredLevel > getThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= highToLowDelayMs) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
				notifyLow();
			}
			break;
		}
		if (currentTime - timeSinceLastLightLog >= 60* 1000L) {
			loglight.info("LightSensor " + getName() + ": ana in=" + measuredLevel);
			timeSinceLastLightLog = currentTime;
		}

	}

	private void notifyLow() {
		log.info("LightSensor - '" + getName() + "' notifies LOW event: input=" + measuredLevel + " < threshold=" + getThreshold());
		notifyListeners(EventType.LIGHT_LOW);
	}

	private void notifyHigh() {
		log.info("LightSensor - '" + getName() + "' notifies HIGH event: light=" + measuredLevel + " ≥ threshold=" + getThreshold());
		notifyListeners(EventType.LIGHT_HIGH);
	}
}
