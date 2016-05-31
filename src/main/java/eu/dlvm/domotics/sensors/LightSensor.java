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
 * {@link IHardwareIO#readAnalogInput(LogCh)}) . If this input value
 * is higher than {@link #getHighThreshold()} for {@link #getHighWaitingTime()}
 * milliseconds a {@link States#HIGH} event is sent once. If lower than
 * {@link #getLowThreshold()} for {@link #getThresholdDelayMs()} milliseconds
 * a {@link States#LOW} event is sent once.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor {

	private static final Logger log = Logger.getLogger(LightSensor.class);
	private static final Logger loglight = Logger.getLogger("LIGHT");
	private int highThreshold;
	private int lowThreshold;
	private long thresholdDelayMs;
	private States state;
	private long timeCurrentStateStarted;

	// TODO listeners via generic in Sensor basis class
	private Set<IThresholdListener> listeners = new HashSet<>();

	// ==========
	// PUBLIC API
	
	public static enum States {
		LOW, LOW2HIGH_DELAY, HIGH, HIGH2LOW_DELAY,
	};

	public LightSensor(String name, String description, LogCh channel, IDomoticContext ctx, int lowThreshold, int highThreshold, long thresholdDelayMs)
			throws IllegalConfigurationException {
		super(name, description, channel, ctx);
		timeCurrentStateStarted = 0L;
		state = States.LOW;
		if ((highThreshold < lowThreshold) || lowThreshold < 0 || highThreshold < 0) {
			throw new IllegalConfigurationException("Incorrect parameters. Check doc.");
		}
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.thresholdDelayMs = thresholdDelayMs;
		log.info("LightSensor '" + getName() + "' configured: high=" + getHighThreshold() + ", low=" + getLowThreshold() + ", time before effective=" + getThresholdDelayMs() + ", channel="
				+ getChannel());
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
	 * for {@link States#HIGH} state. See also {@link #getThresholdDelayMs()}
	 * .
	 * 
	 * @return low threshold value
	 */
	public int getLowThreshold() {
		return lowThreshold;
	}

	/**
	 * Time in ms that a threshold crossing must remain until the new state is
	 * effective.
	 * 
	 * @return waiting time in milliseconds
	 */
	public long getThresholdDelayMs() {
		return thresholdDelayMs;
	}

	/**
	 * @return the current state
	 */
	public States getState() {
		return state;
	}

	
	// ===========
	// PRIVATE API
	
	@Override
	public void loop(long currentTime, long sequence) {
		int newInput = getHw().readAnalogInput(getChannel());

		loglight.info("LightSensor " + getName() + ": ana in=" + newInput);

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
			} else if ((currentTime - timeCurrentStateStarted) > getThresholdDelayMs()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
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
			} else if ((currentTime - timeCurrentStateStarted) > getThresholdDelayMs()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies back to NORMAL event: freq=" + newInput + " < thresholdLow=" + getLowThreshold());
				notifyListeners(IThresholdListener.EventType.LOW);
			}
			break;
		default:
			throw new RuntimeException("Programming Error. Unhandled state.");
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
