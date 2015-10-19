package eu.dlvm.domotics.sensors;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.LogCh;

/**
 * Niet simpel. Moet de frequentie meten van het windmolentje aan/uit singaal.
 * En dan als ze gedurende een bepaalde tijd een kritische grens overschreden
 * wordt gaat het 'alarm aan'.
 * Als dan gedurende een langere periode een ondergrens onderschreden wordt gaat
 * het terug af.
 * 
 * @author Dirk Vaneynde
 */
public class WindSensor extends Sensor {

	private static Logger log = Logger.getLogger(WindSensor.class);
	private int highFreqThreshold;
	private int lowFreqThreshold;
	private long highTimeBeforeAlert;
	private long lowTimeToResetAlert;
	// TODO listeners via generic in Sensor basis class
	private Set<IThresholdListener> listeners = new HashSet<>();

	public static enum States {
		NORMAL, TOO_HIGH, ALARM, ALARM_BUT_LOW,
	};

	private States state;
	private long timeCurrentStateStarted;
	private double lastFrequency;
	FrequencyGauge gauge;	// package scope for unit tests

	/**
	 * @param name
	 * @param description
	 * @param channel
	 * @param ctx
	 * @param highFreqThreshold
	 * @param lowFreqThreshold
	 * @param highTimeBeforeAlert
	 *            Unit is milliseconds.
	 * @param lowTimeToResetAlert
	 *            Unit is milliseconds.
	 */
	public WindSensor(String name, String description, LogCh channel, IDomoticContext ctx, int highFreqThreshold,
			int lowFreqThreshold, int highTimeBeforeAlert, int lowTimeToResetAlert) {
		super(name, description, channel, ctx);
		if (highFreqThreshold < lowFreqThreshold)
			throw new RuntimeException("Configuration error: highSpeedThreshold must be lower than lowSpeedThreshold.");
		this.highFreqThreshold = highFreqThreshold;
		this.lowFreqThreshold = lowFreqThreshold;
		this.highTimeBeforeAlert = highTimeBeforeAlert;
		this.lowTimeToResetAlert = lowTimeToResetAlert;

		this.gauge = new FrequencyGauge();
		this.state = States.NORMAL;
		this.lastFrequency = 0L;
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
		boolean newInput = getHw().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		double freq = gauge.getFrequency();
System.err.println("freq="+freq);
		if (freq == lastFrequency)
			return;
		else
			lastFrequency = freq;
		
		switch (state) {
		case NORMAL:
			if (freq >= getHighFreqThreshold()) {
				state = States.TOO_HIGH;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case TOO_HIGH:
			if (freq < getHighFreqThreshold()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > getHighTimeBeforeAlert()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies ALARM event: freq=" + freq + " > thresholdHigh="
						+ getHighFreqThreshold());
				notifyListeners(IThresholdListener.EventType.HIGH);
			}
			break;
		case ALARM:
			if (freq <= getFreqSpeedThreshold()) {
				state = States.ALARM_BUT_LOW;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case ALARM_BUT_LOW:
			if (freq > getFreqSpeedThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > getLowTimeToResetAlert()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies back to NORMAL event: freq=" + freq + " < thresholdLow="
						+ getFreqSpeedThreshold());
				notifyListeners(IThresholdListener.EventType.LOW);
			}
			break;
		default:
			throw new RuntimeException("Programming Error. Unhandled state.");
		}
	}

	@Override
	public String toString() {
		return "WindSensor (" + super.toString() + "NOT IMPLEMENTED YET";
	}

	/**
	 * @return the highSpeedThreshold
	 */
	public int getHighFreqThreshold() {
		return highFreqThreshold;
	}

	/**
	 * @return the lowSpeedThreshold
	 */
	public int getFreqSpeedThreshold() {
		return lowFreqThreshold;
	}

	/**
	 * @return the highTimeBeforeAlert
	 */
	public long getHighTimeBeforeAlert() {
		return highTimeBeforeAlert;
	}

	/**
	 * @return the lowTimeToResetAlert
	 */
	public long getLowTimeToResetAlert() {
		return lowTimeToResetAlert;
	}

	/**
	 * @return the state
	 */
	public States getState() {
		return state;
	}

}
