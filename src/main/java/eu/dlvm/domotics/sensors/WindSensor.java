package eu.dlvm.domotics.sensors;

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
	private int highSpeedThreshold;
	private int lowSpeedThreshold;
	private long highTimeBeforeAlert;
	private long lowTimeToResetAlert;

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
	 * @param highSpeedThreshold
	 * @param lowSpeedThreshold
	 * @param highTimeBeforeAlert
	 *            Unit is milliseconds.
	 * @param lowTimeToResetAlert
	 *            Unit is milliseconds.
	 */
	public WindSensor(String name, String description, LogCh channel, IDomoticContext ctx, int highSpeedThreshold,
			int lowSpeedThreshold, int highTimeBeforeAlert, int lowTimeToResetAlert) {
		super(name, description, channel, ctx);
		if (highSpeedThreshold < lowSpeedThreshold)
			throw new RuntimeException("Configuration error: highSpeedThreshold must be lower than lowSpeedThreshold.");
		this.highSpeedThreshold = highSpeedThreshold;
		this.lowSpeedThreshold = lowSpeedThreshold;
		this.highTimeBeforeAlert = highTimeBeforeAlert;
		this.lowTimeToResetAlert = lowTimeToResetAlert;

		this.gauge = new FrequencyGauge();
		this.state = States.NORMAL;
		this.lastFrequency = 0L;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInput = getHw().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		double freq = gauge.getFrequency();

		if (freq == lastFrequency)
			return;
		else
			lastFrequency = freq;
		
		switch (state) {
		case NORMAL:
			if (freq >= getHighSpeedThreshold()) {
				state = States.TOO_HIGH;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case TOO_HIGH:
			if (freq < getHighSpeedThreshold()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > getHighTimeBeforeAlert()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies ALARM event: freq=" + freq + " > thresholdHigh="
						+ getHighSpeedThreshold());
				// FIXME notifyListenersDeprecated(new SensorEvent(this, States.ALARM));
			}
			break;
		case ALARM:
			if (freq <= getLowSpeedThreshold()) {
				state = States.ALARM_BUT_LOW;
				timeCurrentStateStarted = currentTime;
			}
			break;
		case ALARM_BUT_LOW:
			if (freq > getLowSpeedThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
			} else if ((currentTime - timeCurrentStateStarted) > getLowTimeToResetAlert()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor -" + getName() + "' notifies back to NORMAL event: freq=" + freq + " < thresholdLow="
						+ getLowSpeedThreshold());
				// FIXME notifyListenersDeprecated(new SensorEvent(this, States.ALARM));
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
	public int getHighSpeedThreshold() {
		return highSpeedThreshold;
	}

	/**
	 * @return the lowSpeedThreshold
	 */
	public int getLowSpeedThreshold() {
		return lowSpeedThreshold;
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
