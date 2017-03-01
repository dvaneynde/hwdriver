package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanStatemachine {

	static Logger logger = LoggerFactory.getLogger(FanStatemachine.class);

	/**
	 * ON_AFTER_DELAY is in combination with OFF_DELAY2ON and _OFF.
	 * <p>
	 * <img src="doc-files/FanStatechart.jpg"/>
	 * 
	 * @author dirk
	 */
	public enum States {
		OFF, ON, OFF_DELAY2ON, ON_AFTER_DELAY, ON_DELAY2OFF
	};

	private Fan fan;
	private States state;
	private long timeStateEntered = -1L;

	public FanStatemachine(Fan fan) {
		this.fan = fan;
		state = States.OFF;
	}

	// ===== Queries =====

	public States getState() {
		return state;
	}

	public boolean isFanning() {
		return (state == States.ON || state == States.ON_AFTER_DELAY || state == States.ON_DELAY2OFF);
	}

	// ===== Updates =====

	/**
	 * Toggle between immediately turning and stopping. If started, it runs for
	 * {@link #getDelayPeriodSec()} seconds.
	 */
	public boolean toggle() {
		boolean fanOn;
		switch (state) {
		case OFF:
			fanOn = changeState(States.ON);
			logger.info("Fan '" + fan.getName() + "' goes ON (state=" + getState() + ").");
			break;
		case OFF_DELAY2ON:
			fanOn = changeState(States.ON_AFTER_DELAY);
			logger.info("Fan '" + fan.getName() + "' goes ON for " + fan.getOnDurationSec() + " sec. or until switched off (state=" + getState() + ").");
			break;
		case ON_AFTER_DELAY:
			fanOn = changeState(States.OFF_DELAY2ON);
			logger.info("Fan '" + fan.getName() + "' goes off and into OFF_DELAY2ON because of toggle.");
			break;
		case ON:
		case ON_DELAY2OFF:
		default:
			fanOn = changeState(States.OFF);
			logger.info("Fan '" + fan.getName() + "' goes OFF because toggled.");
			break;
		}
		return fanOn;
	}

	/**
	 * Only effective when in state ON_AFTER_DELAY, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		changeState(States.OFF);
		logger.info("Fan '" + fan.getName() + "' goes OFF (really-off) (state=" + getState() + ").");
	}

	public void delayOn() {
		switch (state) {
		case OFF:
			changeState(States.OFF_DELAY2ON);
			logger.info("Fan '" + fan.getName() + "' in delay OFF to ON for " + fan.getDelayOff2OnSec() + " sec.");
			break;
		case ON:
			// ignore event, we remain in ON
			break;
		case ON_DELAY2OFF:
			changeState(States.ON_AFTER_DELAY);
			logger.info("Fan '" + fan.getName() + "' back to ON because delayed-on received.");
			break;
		case ON_AFTER_DELAY:
		default:
			changeState(States.OFF);
			logger.warn("delayOn event not expected, going OFF.");
		}
	}

	public void delayOff() {
		switch (state) {
		case ON_AFTER_DELAY:
			changeState(States.ON_DELAY2OFF);
			logger.info("Fan '" + fan.getName() + "' received delay-off, keep running for " + fan.getDelayOn2OffSec() + " sec.");
			break;
		case OFF_DELAY2ON:
			changeState(States.OFF);
			logger.info("Lamp goes off before delay-to-on period has expired. No fanning.");
			break;
		default:
			changeState(States.OFF);
			logger.warn("delayOff event not expected, going OFF.");
		}
	}

	public void loop(long current, long sequence) {
		if (timeStateEntered == -1L)
			timeStateEntered = current;
		switch (state) {
		case OFF:
			break;
		case ON:
			if ((current - timeStateEntered) > fan.getOnDurationMs()) {
				changeState(States.OFF, current);
				logger.info("Fan '" + fan.getName() + "' goes off because it has run for " + fan.getOnDurationSec() + " sec (seq=" + sequence + ").");
			}
			break;
		case OFF_DELAY2ON:
			if ((current - timeStateEntered) > fan.getDelayToOnDurationMs()) {
				changeState(States.ON_AFTER_DELAY, current);
				logger.info("Fan '" + fan.getName() + "' stays ON for as long as no delay-off is received, or manual off.");
			}
			break;
		case ON_AFTER_DELAY:
			//  if running too long, without delay-off, then stop too
			if ((current - timeStateEntered) > fan.getOnDurationMs()) {
				changeState(States.OFF, current);
				logger.info("Fan '" + fan.getName() + "' goes off (while on because lamp triggered this) because it has run for " + fan.getOnDurationSec()
						+ " sec (seq=" + sequence + ").");
			}
			break;
		case ON_DELAY2OFF:
			if ((current - timeStateEntered) > fan.getDelayToOffDurationMs()) {
				changeState(States.OFF, current);
				logger.info("Fan '" + fan.getName() + "' goes off because delayed OFF has been busy for " + fan.getOnDurationSec() + " sec.");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	// ===== Internal =====

	private boolean changeState(States target) {
		return changeState(target, -1L);
	}

	private boolean changeState(States target, long currentTime) {
		state = target;
		timeStateEntered = currentTime;
		boolean fanning = isFanning();
		fan.writeOutput(fanning);
		return fanning;
	}

	@Override
	public String toString() {
		return "FanStatemachine [state=" + state + ", timeStateEntered=" + timeStateEntered + "]";
	}

}
