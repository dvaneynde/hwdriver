package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanStatemachine {

	static Logger logger = LoggerFactory.getLogger(FanStatemachine.class);

	/**
	 * ON_DELAY is in combination with OFF_DELAY2ON and _OFF.
	 * <p>
	 * <img src="doc-files/FanStatechart.jpg"/>
	 * 
	 * @author dirk
	 */
	public enum States {
		OFF, ON, OFF_DELAY2ON, ON_DELAY, ON_DELAY2OFF
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
		return (state == States.ON || state == States.ON_DELAY || state == States.ON_DELAY2OFF);
	}

	// ===== Updates =====

	private void logEvent(States oldState, String event, long time) {
		logger.info("Fan %s - event %s [ %s -> %s ] - maximum %i seconds.", fan.getName(), oldState.name(), getState().name(), time);
	}

	private void logEvent(States oldState, String event) {
		logger.info("Fan %s - event %s [ %s -> %sgoes ON ].", fan.getName(), oldState.name(), getState().name());
	}

	/**
	 * Toggle between immediately turning and stopping. If started, it runs for
	 * {@link #getDelayPeriodSec()} seconds.
	 */
	public boolean toggle() {
		States oldState = state;
		boolean fanOn;
		switch (oldState) {
		case OFF:
			fanOn = changeState(States.ON);
			//logger.info("Fan '" + fan.getName() + "' goes ON because of toggle (OFF->ON).");
			logEvent(oldState, "toggle", fan.getOnDurationSec());
			break;
		case OFF_DELAY2ON:
			fanOn = changeState(States.ON_DELAY);
			//logger.info("Fan '" + fan.getName() + "' goes ON for " + fan.getOnDurationSec() + " sec. or until switched off (state=" + getState() + ").");
			logEvent(oldState, "toggle");
			break;
		case ON_DELAY:
			fanOn = changeState(States.OFF_DELAY2ON);
			//logger.info("Fan '" + fan.getName() + "' goes off and into OFF_DELAY2ON because of toggle.");
			logEvent(oldState, "toggle", fan.getDelayOn2OffSec());
			break;
		case ON:
		case ON_DELAY2OFF:
		default:
			fanOn = changeState(States.OFF);
			//logger.info("Fan '" + fan.getName() + "' goes OFF because toggled.");
			logEvent(oldState, "toggle");
			break;
		}
		return fanOn;
	}

	/**
	 * Only effective when in state ON_DELAY, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		States oldState = state;
		changeState(States.OFF);
		//logger.info("Fan '" + fan.getName() + "' goes OFF (really-off) (state=" + getState() + ").");
		logEvent(oldState, "reallyOff");

	}

	public void delayOn() {
		States oldState = state;
		switch (oldState) {
		case OFF:
			changeState(States.OFF_DELAY2ON);
			//logger.info("Fan '" + fan.getName() + "' in delay OFF to ON for " + fan.getDelayOff2OnSec() + " sec.");
			logEvent(oldState, "delayOn", fan.getDelayOff2OnSec());
			break;
		case ON:
			changeState(States.ON_DELAY);
			//logger.info("Fan '" + fan.getName() + "' back to ON because delayed-on received.");
			logEvent(oldState, "delayOn");
			break;
		case ON_DELAY2OFF:
			changeState(States.ON_DELAY);
			//logger.info("Fan '" + fan.getName() + "' back to ON because delayed-on received.");
			logEvent(oldState, "delayOn");
			break;
		case ON_DELAY:
		default:
			changeState(States.OFF);
			logger.warn("Unexpected event 'delayOn', going OFF (%s -> %s).", oldState.name(), state.name());
		}
	}

	public void delayOff() {
		States oldState = state;
		switch (oldState) {
		case ON_DELAY:
		case ON:
			changeState(States.ON_DELAY2OFF);
			//logger.info("Fan '" + fan.getName() + "' received delay-off, keep running for " + fan.getDelayOn2OffSec() + " sec.");
			logEvent(oldState, "delayOff", fan.getDelayOn2OffSec());
			break;
		case OFF_DELAY2ON:
			changeState(States.OFF);
			//logger.info("Lamp goes off before delay-to-on period has expired. No fanning.");
			logEvent(oldState, "delayOff");
			break;
		default:
			changeState(States.OFF);
			logger.warn("Unexpected event 'delayOff', going OFF (%s -> %s).", oldState.name(), state.name());
		}
	}

	private void logLoop(States oldState, long time) {
		logger.info("Fan %s - time of %i sec. passed [ %s -> %s ].", fan.getName(), time, oldState.name(), getState().name());
	}

	public void loop(long current, long sequence) {
		States oldState = state;
		if (timeStateEntered == -1L)
			timeStateEntered = current;
		switch (state) {
		case OFF:
			break;
		case ON:
			if ((current - timeStateEntered) > fan.getOnDurationMs()) {
				changeState(States.OFF, current);
				//logger.info("Fan '" + fan.getName() + "' goes off because it has run for " + fan.getOnDurationSec() + " sec (ON->OFF).");
				logLoop(oldState, fan.getOnDurationSec());
			}
			break;
		case OFF_DELAY2ON:
			if ((current - timeStateEntered) > fan.getDelayToOnDurationMs()) {
				changeState(States.ON_DELAY, current);
				//logger.info("Fan '" + fan.getName() + "' stays ON for until delay-off, timeout or manual off (OFF_DELAY2ON->ON_DELAY).");
				logLoop(oldState, fan.getDelayToOnDurationMs());
			}
			break;
		case ON_DELAY:
			//  if running too long, without delay-off, then stop too
			long timeOut = 2 * fan.getOnDurationMs() + fan.getDelayToOffDurationMs();
			if ((current - timeStateEntered) > timeOut) {
				changeState(States.OFF, current);
				//logger.info("Fan '" + fan.getName() + "' goes off because it has run for " + timeOut / 1000 + " sec. (ON_DELAY->OFF)");
				logLoop(oldState, timeOut);
			}
			break;
		case ON_DELAY2OFF:
			if ((current - timeStateEntered) > fan.getDelayToOffDurationMs()) {
				changeState(States.OFF, current);
//				logger.info("Fan '" + fan.getName() + "' goes off because delayed OFF period of " + fan.getOnDurationSec()
//						+ " sec. has passed (ON_DELAY2OFF->OFF).");
				logLoop(oldState, fan.getDelayToOffDurationSec());
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
