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
 * {@link IHardwareIO#readAnalogInput(LogCh)}) every 200 ms. If this input value
 * is higher than {@link #getHighThreshold()} for {@link #getHighWaitingTime()}
 * milliseconds a {@link States#HIGH} event is sent once. If lower than
 * {@link #getLowThreshold()} for {@link #thresholdWaitingTimeMs()} milliseconds a
 * {@link States#LOW} event is sent once.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor {

	private static Logger log = Logger.getLogger(LightSensor.class);
	private int highThreshold;
	private int lowThreshold;
	private long thresholdWaitingTimeMs;
	private long sampleIntervalTimeMs;
	// TODO listeners via generic in Sensor basis class
	private Set<IThresholdListener> listeners = new HashSet<>();

	public static enum States {
		LOW, LOW2HIGH_WAITING, HIGH, HIGH2LOW_WAITING,
	};

	private States state;
	private long timeCurrentStateStarted;
	private long timeOfLastSample;

	public LightSensor(String name, String description, LogCh channel, IDomoticContext ctx, int lowThreshold, int highThreshold,
			long thresholdWaitingTimeMs) throws IllegalConfigurationException {
		super(name, description, channel, ctx);
		timeOfLastSample = 0L;
		timeCurrentStateStarted = 0L;
		state = States.LOW;
		sampleIntervalTimeMs = 200L;
		if ((highThreshold < lowThreshold) || lowThreshold < 0 || highThreshold < 0) {
			throw new IllegalConfigurationException("Incorrect parameters. Check doc.");
		}
		this.lowThreshold = lowThreshold;
		this.highThreshold = highThreshold;
		this.thresholdWaitingTimeMs = thresholdWaitingTimeMs;
		log.info("LightSensor '" + getName() + "' configured: high=" + getHighThreshold() + ", low="+getLowThreshold()+", time before effective="+thresholdWaitingTimeMs()+", channel="+getChannel());
	}

	public void registerListener(IThresholdListener listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners(IThresholdListener.EventType event) {
		for (IThresholdListener l:listeners)
			l.onEvent(this, event);
	}

	@Override
	public void loop(long currentTime, long sequence) {
		if ((currentTime - timeOfLastSample) < sampleIntervalTimeMs)
			return;

		int newInput = getHw().readAnalogInput(getChannel());
		timeOfLastSample = currentTime;
		
		log.debug("LightSensor "+getName()+": ana in="+newInput);

		switch (state) {
		case LOW:
			if (newInput >= getHighThreshold()) {
				state = States.LOW2HIGH_WAITING;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case LOW2HIGH_WAITING:
			if (newInput < getHighThreshold()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > thresholdWaitingTimeMs()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
				log.info("LightSensor -" + getName() + "' notifies HIGH event: light=" + newInput + " > thresholdHigh="
						+ getHighThreshold());
				notifyListeners(IThresholdListener.EventType.HIGH);
			}
			break;
		case HIGH:
			if (newInput < getLowThreshold()) {
				state = States.HIGH2LOW_WAITING;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case HIGH2LOW_WAITING:
			if (newInput > getLowThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > thresholdWaitingTimeMs()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies back to NORMAL event: freq=" + newInput + " < thresholdLow="
						+ getLowThreshold());
				notifyListeners(IThresholdListener.EventType.LOW);
			}
			break;
		default:
			throw new RuntimeException("Programming Error. Unhandled state.");
		}
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
	 * for {@link States#HIGH} state. See also {@link #thresholdWaitingTimeMs()}.
	 * 
	 * @return low threshold value
	 */
	public int getLowThreshold() {
		return lowThreshold;
	}

	/**
	 * Time in ms that a threshold crossing must remain until the new state is effective.
	 * 
	 * @return waiting time in milliseconds
	 */
	public long thresholdWaitingTimeMs() {
		return thresholdWaitingTimeMs;
	}

	/**
	 * @return the current state
	 */
	public States getState() {
		return state;
	}

	@Override
	public String toString() {
		return "LightSensor (" + super.toString() + "NOT IMPLEMENTED YET";
	}
}
