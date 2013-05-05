package eu.dlvm.domotica.blocks.concrete;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.iohardware.LogCh;

/**
 * Represents two Relays, one for the motor lifting a screen, one for the 2nd
 * motor lowering the screen. Both are put in one abstraction to avoid 2 motors
 * being activated together.
 * <p>
 * If a screen is going up and one presses the down button a Screen will stop
 * the up motor, <strong>wait 100ms</strong> and then start the down motor. This
 * is to be sure that not both motors are active.
 * 
 * @author Dirk Vaneynde
 */
public class Screen extends Actuator {
	/**
	 * Delay in ms between switching motors, i.e. periode that both motors are
	 * off after one was on and before the other goes on.
	 */
	public static long MOTOR_SWITCH_DELAY_PROTECTION = 100;
	/**
	 * Time in ms that a motor is maximally on - if user does not do anything.
	 */
	public static long MAX_MOTOR_ON_PERIOD = 10000;

	private LogCh chUp;
	private long timeStateStart;
	private boolean gotUp, gotDown;

	public Screen(String name, String description, int chDown, int chUp,
			IDomoContext ctx) {
		this(name, description, new LogCh(chDown), new LogCh(chUp), ctx);
	}

	public Screen(String name, String description, LogCh chDown, LogCh chUp,
			IDomoContext ctx) {
		super(name, description, chDown, ctx);
		this.chUp = chUp;
	}

	public enum States {
		REST, UP, SWITCH_DOWN_2_UP, SWITCH_UP_2_DOWN, DOWN
	};

	private States state = States.REST;

	@Override
	public void initializeOutput() {
		// Nothing to do;
	}

	public States getState() {
		return state;
	}

	public void up() {
		gotUp = true;
	}

	public void down() {
		gotDown = true;
	}

	@Override
	public void loop(long current, long sequence) {
		switch (state) {
		case REST:
			if (gotUp) {
				setStateAndEntryUp(current);
			} else if (gotDown) {
				setStateAndEntryDown(current);
			}
			break;
		case DOWN:
			if (gotDown) {
				exitDown(current);
				state = States.REST;
			} else if (gotUp) {
				exitDown(current);
				state = States.SWITCH_DOWN_2_UP;
			} else if ((current - timeStateStart) > MAX_MOTOR_ON_PERIOD) {
				exitDown(current);
				state = States.REST;
			}
			break;
		case UP:
			if (gotDown) {
				exitUp(current);
				state = States.SWITCH_UP_2_DOWN;
			} else if (gotUp) {
				exitUp(current);
				state = States.REST;
			} else if ((current - timeStateStart) > MAX_MOTOR_ON_PERIOD) {
				exitUp(current);
				state = States.REST;
			}
			break;
		case SWITCH_UP_2_DOWN:
			if (gotDown) {
				// ignore
			} else if (gotUp) {
				setStateAndEntryUp(current);
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				setStateAndEntryDown(current);
			}
			break;
		case SWITCH_DOWN_2_UP:
			if (gotDown) {
				setStateAndEntryDown(current);
			} else if (gotUp) {
				// ignore
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				setStateAndEntryUp(current);
			}
			break;
		default:
			throw new RuntimeException();
		}
		gotUp = gotDown = false;
	}

	private void setStateAndEntryUp(long current) {
		timeStateStart = current;
		state = States.UP;
		hw().writeDigitalOutput(chUp, true);
	}

	private void exitUp(long current) {
		timeStateStart = current;
		hw().writeDigitalOutput(chUp, false);
	}

	private void setStateAndEntryDown(long current) {
		timeStateStart = current;
		state = States.DOWN;
		hw().writeDigitalOutput(getChannel(), true);
	}

	private void exitDown(long current) {
		timeStateStart = current;
		hw().writeDigitalOutput(getChannel(), false);
	}

	@Override
	public String toString() {
		return "Screen (" + super.toString() + ") state=" + getState().name();
	}
}
