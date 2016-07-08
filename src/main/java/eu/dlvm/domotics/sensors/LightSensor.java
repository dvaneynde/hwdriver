package eu.dlvm.domotics.sensors;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IllegalConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

/**
 * Measures light via analog input (via
 * {@link IHardwareIO#readAnalogInput(LogCh)}) . If this input value is higher
 * than {@link #getHighThreshold()} for {@link #getHighWaitingTime()}
 * milliseconds a {@link States#HIGH} event is sent once. If lower than
 * {@link #getLowThreshold()} for {@link #getThresholdDelayMs()} milliseconds a
 * {@link States#LOW} event is sent once.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor {

	private static final int DEFAULT_REPEAT_EVENT_MS = 1000;
	private static final Logger log = Logger.getLogger(LightSensor.class);
	private static final Logger loglight = Logger.getLogger("LIGHT");
	private int highThreshold, lowThreshold;
	private long lowToHighDelayMs, highToLowDelayMs;
	private long lastInfoOnAnalogLevel;
	private States state;
	private long timeCurrentStateStarted, timeSinceLastEventSent;

	// TODO listeners via generic in Sensor basis class
	private Set<IThresholdListener> listeners = new HashSet<>();

	// ===================
	// PUBLIC API

	public static enum States {
		LOW, LOW2HIGH_DELAY, HIGH, HIGH2LOW_DELAY,
	};

	public LightSensor(String name, String description, LogCh channel, IDomoticContext ctx, int lowThreshold, int highThreshold, int lowToHighDelaySec, int highToLowDelaySec)
			throws IllegalConfigurationException {
		super(name, description, channel, ctx);
		if ((highThreshold < lowThreshold) || lowThreshold < 0 || highThreshold < 0) {
			throw new IllegalConfigurationException("Incorrect parameters. Check doc.");
		}
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.lowToHighDelayMs = lowToHighDelaySec * 1000L;
		this.highToLowDelayMs = highToLowDelaySec * 1000L;

		timeCurrentStateStarted = timeSinceLastEventSent = 0L;
		state = States.LOW;
		log.info("LightSensor '" + getName() + "' configured: high=" + getHighThreshold() + ", low=" + getLowThreshold() + ", low to high delay=" + getLowToHighDelaySec()
				+ ", high to low delay=" + getHighToLowDelaySec() + " s., channel=" + getChannel());
	}

	/**
	 * Threshold value of input (via {@link IHardwareIO#readAnalogInput(LogCh)})
	 * for {@link States#HIGH} state. See also {@link #getHighWaitingTime()}.
	 * 
	 * @return high threshold value
	 */
	public int getHighThreshold() {
		return highThreshold;
	}

	/**
	 * Threshold value of input (via {@link IHardwareIO#readAnalogInput(LogCh)})
	 * for {@link States#HIGH} state. See also {@link #getThresholdDelayMs()}.
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

	// ===================
	// PRIVATE API

	@Override
	public void loop(long currentTime, long sequence) {
		int newInput = getHw().readAnalogInput(getChannel());
		if (currentTime - lastInfoOnAnalogLevel > 1000L) {
			loglight.info("LightSensor " + getName() + ": ana in=" + newInput);
			lastInfoOnAnalogLevel = currentTime;
		}

		switch (state) {
		case LOW:
			if (newInput >= getHighThreshold()) {
				state = States.LOW2HIGH_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case LOW2HIGH_DELAY:
			if (newInput < getHighThreshold()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= lowToHighDelayMs) {
				state = States.HIGH;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("LightSensor -" + getName() + "' notifies HIGH event: light=" + newInput + " > thresholdHigh=" + getHighThreshold());
				notifyListeners(IThresholdListener.EventType.HIGH);
			}
			break;
		case HIGH:
			if (newInput < getLowThreshold()) {
				state = States.HIGH2LOW_DELAY;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case HIGH2LOW_DELAY:
			if (newInput > getLowThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) >= highToLowDelayMs) {
				state = States.LOW;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("LightSensor -" + getName() + "' notifies back to NORMAL event: input=" + newInput + " < thresholdLow=" + getLowThreshold());
				notifyListeners(IThresholdListener.EventType.LOW);
			}
			break;
		}
		if (currentTime - timeSinceLastEventSent >= DEFAULT_REPEAT_EVENT_MS) {
			notifyListeners((state == States.HIGH || state == States.HIGH2LOW_DELAY) ? IThresholdListener.EventType.HIGH : IThresholdListener.EventType.LOW);
			timeSinceLastEventSent = currentTime;
		}
	}

	public void registerListener(IThresholdListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners(IThresholdListener.EventType event) {
		for (IThresholdListener l : listeners)
			l.onEvent(this, event);
	}

	@Override
	public String toString() {
		return "LightSensor (" + super.toString() + "NOT IMPLEMENTED YET";
	}
}
