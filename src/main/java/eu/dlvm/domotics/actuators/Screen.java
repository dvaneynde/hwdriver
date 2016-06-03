package eu.dlvm.domotics.actuators;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.service.BlockInfo;
import eu.dlvm.iohardware.LogCh;

/**
 * Represents two Screen relays, one for the motor lifting a screen, the second
 * motor lowering the screen. Both relays are put in one class to ensure that
 * they cannot be activated together, potentially destroying a motor.
 * <p>
 * If a screen is going up and one presses the down button a Screen will stop
 * the up motor, <strong>wait {@link #MOTOR_SWITCH_DELAY_PROTECTION}</strong>
 * and then start the down motor. Again this is to be sure that both motors are
 * active together.
 * <p>
 * To protect against wind the {@link #setProtect(boolean)} protect property,
 * when set to <code>true</code>, will cause the Screen to go up, and all
 * {@link #down()} or {@link #up} methods to be ignored, until the protection is
 * canceled.
 * 
 * @author Dirk Vaneynde
 */
public class Screen extends Actuator {

	private static final Logger log = Logger.getLogger(Screen.class);

	/**
	 * Delay in ms between switching motors, i.e. period that both motors are
	 * off after one was on and before the other goes on.
	 */
	public static final long MOTOR_SWITCH_DELAY_PROTECTION = 500;
	/**
	 * Time in ms that a motor is maximally on - if user does not do anything.
	 */
	public static final long DEFAULT_MOTOR_ON_PERIOD_SEC = 30;

	private LogCh chUp;
	private long motorUpPeriodMs = DEFAULT_MOTOR_ON_PERIOD_SEC * 1000L;
	private long motorDnPeriodMs = DEFAULT_MOTOR_ON_PERIOD_SEC * 1000L;
	private long timeStateStart;
	private boolean gotUp, gotDown, protect;
	private double ratioClosed, ratioClosedAtStateStart;
	private States state = States.REST;

	/*
	 * Public API
	 */
	public Screen(String name, String description, String ui, LogCh chDown, LogCh chUp, IDomoticContext ctx) {
		super(name, description, ui, chDown, ctx);
		this.chUp = chUp;
	}

	// TODO see bug 80
	public enum States {
		REST, UP, DELAY_DOWN_2_UP, DELAY_UP_2_DOWN, DOWN, REST_PROTECT;
	};

	public States getState() {
		return state;
	}

	public void up() {
		if (!protect)
			gotUp = true;
	}

	public void down() {
		if (!protect)
			gotDown = true;
	}

	public void setProtect(boolean enable) {
		protect = enable;
		gotUp = false;
		gotDown = false;
	}

	public boolean getProtect() {
		return protect;
	}

	public double getRatioClosed() {
		return ratioClosed;
	}

	public int getRatioClosedAsPercentage() {
		return ((int) (getRatioClosed() * 100));
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open
	 * or close a screen.
	 */
	public int getMotorUpPeriod() {
		return (int) (motorUpPeriodMs / 1000);
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open
	 * or close a screen.
	 */
	public void setMotorUpPeriod(int motorUpPeriod) {
		this.motorUpPeriodMs = motorUpPeriod * 1000;
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open
	 * or close a screen.
	 */
	public long getMotorDnPeriod() {
		return (int) (motorDnPeriodMs / 1000);
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open
	 * or close a screen.
	 */
	public void setMotorDnPeriod(long motorDnPeriod) {
		this.motorDnPeriodMs = motorDnPeriod * 1000;
	}

	/*
	 * Internal API
	 */

	@Override
	public void initializeOutput(RememberedOutput ro) {
		// Nothing to do;
	}

	@Override
	public void loop(long current, long sequence) {
		switch (state) {
		case REST:
			// TODO safety time, door hier ook te checken op timeStateStart,
			// delay-protection moet verstreken zijn. Overal waar REST
			// gezet wordt moet die tijd ook gezet worden.
			if (protect) {
				setStateAndEnterUp(current);
				log.info("Screen " + getName() + " is going UP (PROTECTION mode), for maximum " + getMotorUpPeriod() + " sec.");
			} else if (gotUp) {
				setStateAndEnterUp(current);
				log.info("Screen " + getName() + " is going UP, for maximum " + getMotorUpPeriod() + " sec.");
			} else if (gotDown) {
				setStateAndEntryDown(current);
				log.info("Screen " + getName() + " is going DOWN, for maximum " + getMotorDnPeriod() + " sec.");
			}
			break;
		case DOWN:
			ratioClosed = Math.min(1.0, ratioClosedAtStateStart + (current - timeStateStart) / (double) motorDnPeriodMs);
			if (gotDown && !protect) {
				exitDown(current);
				state = States.REST;
				log.info("Screen " + getName() + " stopped going down due to DOWN event. Closed for " + getRatioClosedAsPercentage() + "%.");
			} else if (gotUp || protect) {
				exitDown(current);
				state = States.DELAY_DOWN_2_UP;
				log.info("Screen " + getName() + " stopped going down due to UP event" + (protect ? " for PROTECT mode" : "") + ". Will go up after safety time. Closed for "
						+ getRatioClosedAsPercentage() + "%.");
			} else if ((current - timeStateStart) > motorDnPeriodMs) {
				exitDown(current);
				state = States.REST;
				log.info("Screen " + getName() + " stopped going down because motor-on time is reached. Closed for " + getRatioClosedAsPercentage() + "%.");
			}
			break;
		case UP:
			ratioClosed = Math.max(0.0, ratioClosedAtStateStart - (current - timeStateStart) / (double) motorUpPeriodMs);
			if (gotDown && !protect) {
				exitUp(current);
				state = States.DELAY_UP_2_DOWN;
				log.info("Screen " + getName() + " stopped going up due to DOWN event. Will go down after safety time. Closed for " + getRatioClosedAsPercentage() + "%.");
			} else if (gotUp && !protect) {
				exitUp(current);
				state = States.REST;
				log.info("Screen " + getName() + " stopped going up due to UP event. Closed for " + getRatioClosedAsPercentage() + "%.");
			} else if ((current - timeStateStart) > motorUpPeriodMs) {
				exitUp(current);
				if (protect) {
					state = States.REST_PROTECT;
					log.info("Screen " + getName() + " stopped going up because motor-on time is reached, and goes in PROTECTED mode. Closed for " + getRatioClosedAsPercentage() + "%.");
				} else {
					state = States.REST;
					log.info("Screen " + getName() + " stopped going up because motor-on time is reached. Closed for " + getRatioClosedAsPercentage() + "%.");
				}
			}
			break;
		case DELAY_UP_2_DOWN:
			if (gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				log.warn("Screen " + getName() + " got event while switching up to down, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				if (protect) {
					setStateAndEnterUp(current);
					log.info("Screen " + getName() + " going UP after safety time because of PROTECT.");
				} else {
					setStateAndEntryDown(current);
					log.info("Screen " + getName() + " going DOWN after safety time.");
				}
			}
			break;
		case DELAY_DOWN_2_UP:
			if (gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				log.warn("Screen " + getName() + " got event while switching down to up, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				if (protect) {
					setStateAndEnterUp(current);
					log.info("Screen " + getName() + " going UP after safety time because of PROTECT.");
				} else {
					setStateAndEnterUp(current);
					log.info("Screen " + getName() + " going UP after safety time.");
				}
			}
			break;
		case REST_PROTECT:
			if (!protect) {
				timeStateStart = current;
				state = States.REST;
				log.info("Screen " + getName() + " leaves PROTECTED mode.");
			}
			break;
		default:
			break;

		}
		gotUp = gotDown = false;
	}

	private void setStateAndEnterUp(long current) {
		timeStateStart = current;
		ratioClosedAtStateStart = ratioClosed;
		state = States.UP;
		getHw().writeDigitalOutput(chUp, true);
	}

	private void exitUp(long current) {
		timeStateStart = current;
		getHw().writeDigitalOutput(chUp, false);
	}

	private void setStateAndEntryDown(long current) {
		timeStateStart = current;
		ratioClosedAtStateStart = ratioClosed;
		state = States.DOWN;
		getHw().writeDigitalOutput(getChannel(), true);
	}

	private void exitDown(long current) {
		timeStateStart = current;
		getHw().writeDigitalOutput(getChannel(), false);
	}

	@Override
	public BlockInfo getBlockInfo() {
		BlockInfo bi = new BlockInfo(this.getName(), this.getClass().getSimpleName(), getDescription());
		bi.setStatus("" + getRatioClosedAsPercentage() + "% " + asSign() + (protect ? " STORM" : ""));
		return bi;
	}

	private String asSign() {
		switch (getState()) {
		case DOWN:
			return "vvvv";
		case UP:
			return "^^^^";
		case REST:
		case REST_PROTECT:
			if (ratioClosed > 0.90)
				return "TOE ";
			else if (ratioClosed < 0.10)
				return "OPEN";
			else
				return "HALF";
		case DELAY_DOWN_2_UP:
			return "WCHT";
		case DELAY_UP_2_DOWN:
			return "WCHT";
		}
		return "ERROR";
	}

	@Override
	public void update(String action) {
		log.debug("update() action=" + action + ", this=" + this);
		switch (action) {
		case "up":
			up();
			break;
		case "down":
			down();
			break;
		default:
			log.warn("update(): ignored unknown action=" + action + " on screen with name=" + getName() + '.');
		}
	}

	@Override
	public String toString() {
		return "Screen " + super.toString() + ", motorDnPeriod=" + motorDnPeriodMs + ", motorUpPeriod=" + motorUpPeriodMs + ", state=" + state + "]";
	}
}
