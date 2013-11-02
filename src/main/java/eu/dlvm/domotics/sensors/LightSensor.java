package eu.dlvm.domotics.sensors;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.IllegalConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

/**
 * Measures light via analog input (via
 * {@link IHardwareIO#readAnalogInput(LogCh)}) every 200 ms. If this input value
 * is higher than {@link #getHighThreshold()} for {@link #getHighWaitingTime()}
 * milliseconds a {@link States#HIGH} event is sent once. If lower than
 * {@link #getLowThreshold()} for {@link #getLowWaitingTime()} milliseconds a
 * {@link States#LOW} event is sent once.
 * 
 * @author dirk vaneynde
 * 
 */
public class LightSensor extends Sensor {

	private static Logger log = Logger.getLogger(LightSensor.class);
	private int highThreshold;
	private int lowThreshold;
	private long lowWaitingTime;
	private long highWaitingTime;
	private long sampleIntervalTimeMs;

	public static enum States {
		LOW, LOW2HIGH_WAITING, HIGH, HIGH2LOW_WAITING,
	};

	private States state;
	private long timeCurrentStateStarted;
	private long timeOfLastSample;

	/**
	 * 
	 * @param name
	 * @param description
	 * @param channel
	 * @param ctx
	 * @param lowThreshold
	 * @param highThreshold
	 * @param low2highWaitTime
	 * @param high2lowWaitTime
	 * @throws IllegalConfigurationException
	 */
	public LightSensor(String name, String description, LogCh channel, IHardwareAccess ctx, int lowThreshold, int highThreshold,
			long low2highWaitTime, long high2lowWaitTime) throws IllegalConfigurationException {
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
		this.lowWaitingTime = low2highWaitTime;
		this.highWaitingTime = high2lowWaitTime;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		if ((currentTime - timeOfLastSample) < sampleIntervalTimeMs)
			return;

		int newInput = getHw().readAnalogInput(getChannel());
		timeOfLastSample = currentTime;

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
			} else if ((currentTime - timeCurrentStateStarted) > getLowWaitingTime()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
				log.info("LightSensor -" + getName() + "' notifies HIGH event: light=" + newInput + " > thresholdHigh="
						+ getHighThreshold());
				notifyListenersDeprecated(new SensorEvent(this, States.HIGH));
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
			} else if ((currentTime - timeCurrentStateStarted) > getHighWaitingTime()) {
				state = States.LOW;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies back to NORMAL event: freq=" + newInput + " < thresholdLow="
						+ getLowThreshold());
				notifyListenersDeprecated(new SensorEvent(this, States.LOW));
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
	 * for {@link States#HIGH} state. See also {@link #getLowWaitingTime()}.
	 * 
	 * @return low threshold value
	 */
	public int getLowThreshold() {
		return lowThreshold;
	}

	/**
	 * While in state {@link States#HIGH}, time that input must be below
	 * {@link #getLowThreshold()} for state to become {@link States#LOW}
	 * 
	 * @return waiting time in milliseconds
	 */
	public long getLowWaitingTime() {
		return lowWaitingTime;
	}

	/**
	 * While in state {@link States#LOW}, time that input must be below
	 * {@link #getHighThreshold()} for state to become {@link States#HIGH}
	 * 
	 * @return waiting time in milliseconds
	 */
	public long getHighWaitingTime() {
		return highWaitingTime;
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
