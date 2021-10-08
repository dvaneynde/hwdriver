package eu.dlvm.domotics.actuators;

import eu.dlvm.iohardware.IHardwareWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

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
 * {@link #toggleDown()} or {@link #up} methods to be ignored, until the
 * protection is canceled.
 * 
 * @author Dirk Vaneynde
 */
public class Screen extends Actuator implements IEventListener, IUiCapableBlock {

	private static final Logger logger = LoggerFactory.getLogger(Screen.class);

	/**
	 * Delay in ms between switching motors, i.e. period that both motors are
	 * off after one was on and before the other goes on.
	 */
	public static final long MOTOR_SWITCH_DELAY_PROTECTION = 500;
	/**
	 * Time in ms that a motor is maximally on - if user does not do anything.
	 */
	public static final long DEFAULT_MOTOR_ON_PERIOD_SEC = 30;

	private String chUp;
	private long motorUpPeriodMs = DEFAULT_MOTOR_ON_PERIOD_SEC * 1000L;
	private long motorDnPeriodMs = DEFAULT_MOTOR_ON_PERIOD_SEC * 1000L;
	private long timeStateStart;
	private boolean gotToggleUp, gotToggleDown, gotUp, gotDown, protect;
	private double ratioClosed, ratioClosedAtStateStart;
	private States state = States.REST;

	/*
	 * Public API
	 */
	public Screen(String name, String description, String ui, String chDown, String chUp, IHardwareWriter writer, IDomoticBuilder builder) {
		super(name, description, ui, chDown, writer, builder);
		this.chUp = chUp;
	}

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

	public void toggleUp() {
		if (!protect)
			gotToggleUp = true;
	}

	public void toggleDown() {
		if (!protect)
			gotToggleDown = true;
	}

	public void setProtect(boolean enable) {
		protect = enable;
		gotToggleUp = false;
		gotToggleDown = false;
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

	public long getMotorUpPeriodMs() {
		return motorUpPeriodMs;
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
	public int getMotorDnPeriod() {
		return (int) (motorDnPeriodMs / 1000);
	}

	public long getMotorDnPeriodMs() {
		return motorDnPeriodMs;
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open
	 * or close a screen.
	 */
	public void setMotorDnPeriod(long motorDnPeriod) {
		this.motorDnPeriodMs = motorDnPeriod * 1000;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ALARM:
			setProtect(true);
			break;
		case SAFE:
			setProtect(false);
			break;
		case TOGGLE_UP:
			toggleUp();
			break;
		case TOGGLE_DOWN:
			toggleDown();
			break;
		case UP:
			up();
			break;
		case DOWN:
			down();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	// ===== UI =====

	@Override
	public UiInfo getUiInfo() {
		String status = "" + getRatioClosedAsPercentage() + "% " + asSign() + (protect ? " STORM" : "");
		UiInfo uiInfo = new UiInfo(this, status);
		return uiInfo;
	}

	private String asSign() {
		// TODO hieronder moeten toevoegen omdat tijdens constructie dit wordt opgeroepen en - raar maar waar - nog niet alles geinitialiseerd is (omdat via super/getBlockInfo dit wordt opgeroepen)
		if (getState() == null)
			return "";
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
		logger.debug("update() action=" + action + ", this=" + this);
		switch (action) {
		case "up":
			toggleUp();
			break;
		case "down":
			toggleDown();
			break;
		default:
			logger.warn("update(): ignored unknown action=" + action + " on screen with name=" + getName() + '.');
		}
	}

	// ===== Internal =====
	@Override
	public void initializeOutput(RememberedOutput ro) {
		// Nothing to do;
	}

	@Override
	public void loop(long current) {
		switch (state) {
		case REST:
			// TODO safety time, door hier ook te checken op timeStateStart,
			// delay-protection moet verstreken zijn. Overal waar REST
			// gezet wordt moet die tijd ook gezet worden.
			if (protect) {
				setStateAndEnterUp(current);
				logger.info("Screen " + getName() + " REST-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got PROTECT message; time="
						+ getMotorUpPeriod() + " sec.");
			} else if (gotToggleUp || gotUp) {
				setStateAndEnterUp(current);
				logger.info("Screen " + getName() + " REST-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got '"
						+ (gotToggleUp ? "toggle-up" : "up") + "' messaage; time=" + getMotorUpPeriod() + "sec.");
			} else if (gotToggleDown || gotDown) {
				setStateAndEntryDown(current);
				logger.info("Screen " + getName() + " REST-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got '"
						+ (gotToggleDown ? "toggle-down" : "down") + "' message; time=" + getMotorDnPeriod() + "sec.");
			}
			break;
		case DOWN:
			ratioClosed = Math.min(1.0, ratioClosedAtStateStart + (current - timeStateStart) / (double) motorDnPeriodMs);
			if (gotToggleDown && !protect) {
				exitDown(current);
				state = States.REST;
				logger.info("Screen " + getName() + " DOWN-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got toggle-down message; "
						+ getRatioClosedAsPercentage() + "% closed.");
			} else if (gotToggleUp || gotUp || protect) {
				exitDown(current);
				state = States.DELAY_DOWN_2_UP;
				logger.info("Screen " + getName() + " DOWN -->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got '"
						+ (protect ? "PROTECT" : (gotUp ? "up" : "toggle-up")) + "'; " + getRatioClosedAsPercentage() + "% closed.");
			} else if ((current - timeStateStart) > motorDnPeriodMs) {
				exitDown(current);
				state = States.REST;
				logger.info("Screen " + getName() + " DOWN-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: motor-on time is reached; "
						+ getRatioClosedAsPercentage() + "% closed.");
			}
			break;
		case UP:
			ratioClosed = Math.max(0.0, ratioClosedAtStateStart - (current - timeStateStart) / (double) motorUpPeriodMs);
			if (gotToggleUp && !protect) {
				exitUp(current);
				state = States.REST;
				logger.info("Screen " + getName() + " UP-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got toggle-UP message; "
						+ getRatioClosedAsPercentage() + "% closed.");
			} else if ((gotToggleDown || gotDown) && !protect) {
				exitUp(current);
				state = States.DELAY_UP_2_DOWN;
				logger.info("Screen " + getName() + " UP-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: got '"
						+ (gotToggleDown ? "toggle-down" : "down") + "' message; " + getRatioClosedAsPercentage() + "% closed.");
			} else if ((current - timeStateStart) > motorUpPeriodMs) {
				exitUp(current);
				if (protect) {
					state = States.REST_PROTECT;
				} else {
					state = States.REST;
				}
				logger.info("Screen " + getName() + " UP-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: motor-on time is reached; "
						+ getRatioClosedAsPercentage() + "% closed.");
			}
			break;
		case DELAY_UP_2_DOWN:
			if (gotToggleDown || gotToggleUp || gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				logger.warn("Screen " + getName() + " DELAY_UP_2_DOWN-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE")
						+ " mode: got event while switching up to down, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				if (protect) {
					setStateAndEnterUp(current);
				} else {
					setStateAndEntryDown(current);
				}
				logger.info("Screen " + getName() + " DELAY_UP_2_DOWN-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: delay has passed.");
			}
			break;
		case DELAY_DOWN_2_UP:
			if (gotToggleDown || gotToggleUp || gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				logger.warn("Screen " + getName() + " DELAY_DOWN_2_UP-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE")
						+ " mode: got event while switching up to down, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				if (protect) {
					setStateAndEnterUp(current);
				} else {
					setStateAndEnterUp(current);
				}
				logger.info("Screen " + getName() + " DELAY_DOWN_2_UP-->" + getState() + ", " + (protect ? "PROTECT" : "SAFE") + " mode: delay has passed.");
			}
			break;
		case REST_PROTECT:
			if (!protect) {
				timeStateStart = current;
				state = States.REST;
				logger.info("Screen " + getName() + " REST_PROTECT-->" + getState() + ": leaves PROTECTED mode.");
			}
			break;
		default:
			break;

		}
		gotToggleUp = gotToggleDown = gotUp = gotDown = false;
	}

	private void setStateAndEnterUp(long current) {
		timeStateStart = current;
		ratioClosedAtStateStart = ratioClosed;
		state = States.UP;
		getHwWriter().writeDigitalOutput(chUp, true);
	}

	private void exitUp(long current) {
		timeStateStart = current;
		getHwWriter().writeDigitalOutput(chUp, false);
	}

	private void setStateAndEntryDown(long current) {
		timeStateStart = current;
		ratioClosedAtStateStart = ratioClosed;
		state = States.DOWN;
		getHwWriter().writeDigitalOutput(getChannel(), true);
	}

	private void exitDown(long current) {
		timeStateStart = current;
		getHwWriter().writeDigitalOutput(getChannel(), false);
	}

	@Override
	public String toString() {
		return "Screen " + super.toString() + ", motorDnPeriod=" + motorDnPeriodMs + ", motorUpPeriod=" + motorUpPeriodMs + ", state=" + state + "]";
	}
}
