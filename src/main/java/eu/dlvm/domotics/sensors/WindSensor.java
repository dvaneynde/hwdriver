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
 * wordt gaat het 'alarm aan'. Als dan gedurende een langere periode een
 * ondergrens onderschreden wordt gaat het terug af.
 * 
 * TODO moet opgaande en afgaande delta berekenen, niet enkel afgaande ! Dat halveert de benodigde samplingfrequentie.
 * 
 * @author Dirk Vaneynde
 */
public class WindSensor extends Sensor {

	private static final Logger log = Logger.getLogger(WindSensor.class);
	private int highFreqThreshold;
	private int lowFreqThreshold;
	private long highTimeBeforeAlert;
	private long lowTimeToResetAlert;
	// TODO listeners via generic in Sensor basis class
	private Set<IThresholdListener> listeners = new HashSet<>();

	public static enum States {
		NORMAL, HIGH, ALARM, ALARM_BUT_LOW,
	};

	private States state;
	private long timeCurrentStateStarted;
	//private double lastFrequency;
	FrequencyGauge gauge; // package scope for unit tests

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
	public WindSensor(String name, String description, LogCh channel, IDomoticContext ctx, int highFreqThreshold, int lowFreqThreshold, int highTimeBeforeAlert, int lowTimeToResetAlert) {
		super(name, description, channel, ctx);
		if (highFreqThreshold < lowFreqThreshold)	// TODO higfreq must be 'far' below 1/(2 xlooptime)
			throw new RuntimeException("Configuration error: highSpeedThreshold must be lower than lowSpeedThreshold.");
		this.highFreqThreshold = highFreqThreshold;
		this.lowFreqThreshold = lowFreqThreshold;
		this.highTimeBeforeAlert = highTimeBeforeAlert;
		this.lowTimeToResetAlert = lowTimeToResetAlert;

		this.gauge = new FrequencyGauge(25);	// TODO parameter? or calculated from sample time and lowfreq/highfreq?
		this.state = States.NORMAL;
		//this.lastFrequency = 0L;
		log.info("Windsensor '" + getName() + "' configured: highFreq=" + getHighFreqThreshold() + ", highTimeBeforeAlert=" + getHighTimeBeforeAlert() / 1000.0 + "(s), lowFreq="
				+ getLowFreqThreshold() + ", lowTimeToResetAlert=" + getLowTimeToResetAlert() / 1000.0 + "(s).");
	}

	public void registerListener(IThresholdListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners(IThresholdListener.EventType event) {
		for (IThresholdListener l : listeners)
			l.onEvent(this, event);
	}

	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInput = getHw().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		double freq = gauge.getAvgFreq();
		//log.debug("frequency: time=" + (currentTime / 1000) % 1000 + "s. " + currentTime % 1000 + "ms.\tfreq=" + freq + "\tcur.state=" + state);

		switch (state) {
		case NORMAL:
			if (freq >= getHighFreqThreshold()) {
				state = States.HIGH;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': NORMAL to HIGH: freq=" + freq + " >= thresholdHigh=" + getHighFreqThreshold());
			}
			break;
		case HIGH:
			if (freq < getHighFreqThreshold()) {
				state = States.NORMAL;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': HIGH to NORMAL : freq=" + freq + " < thresholdHigh=" + getHighFreqThreshold());
			} else if ((currentTime - timeCurrentStateStarted) > getHighTimeBeforeAlert()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.info("WindSensor '" + getName() + "' notifies HIGH event because in ALARM state: freq=" + freq + " > thresholdHigh=" + getHighFreqThreshold()+" for more than "+getHighTimeBeforeAlert()/1000+"sec.");
				notifyListeners(IThresholdListener.EventType.HIGH);
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
	 * TODO double
	 * 
	 * @return high frequency limit
	 */
	public int getHighFreqThreshold() {
		return highFreqThreshold;
	}

	/**
	 * TODO double
	 * 
	 * @return low frequency limit
	 */
	public int getLowFreqThreshold() {
		return lowFreqThreshold;
	}

	/**
	 * @return the high time after which to raise alarm, in milliseconds
	 */
	public long getHighTimeBeforeAlert() {
		return highTimeBeforeAlert;
	}

	/**
	 * @return the low time to reset alert, in milliseconds
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
