package eu.dlvm.domotics.actuators;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.iohardware.LogCh;

/**
 * Represents two Relays, one for the motor lifting a screen, one for the 2nd motor lowering the screen. Both are put in one
 * abstraction to avoid 2 motors being activated together.
 * <p>
 * If a screen is going up and one presses the down button a Screen will stop the up motor, <strong>wait
 * {@link #MOTOR_SWITCH_DELAY_PROTECTION}</strong> and then start the down motor. This is to be sure that not both motors are
 * active.
 * 
 * @author Dirk Vaneynde
 */
public class Screen extends Actuator {

	static Logger log = Logger.getLogger(Screen.class);

	/**
	 * Delay in ms between switching motors, i.e. periode that both motors are off after one was on and before the other goes on.
	 */
	public static final long MOTOR_SWITCH_DELAY_PROTECTION = 500;
	/**
	 * Time in ms that a motor is maximally on - if user does not do anything.
	 */
	public static final long DEFAULT_MOTOR_ON_PERIOD_SEC = 30;

	private LogCh chUp;
	public long motorOnPeriodMs = DEFAULT_MOTOR_ON_PERIOD_SEC * 1000L;

	private long timeStateStart;
	private boolean gotUp, gotDown;

	public Screen(String name, String description, LogCh chDown, LogCh chUp,
			IDomoticContext ctx) {
		super(name, description, chDown, ctx);
		this.chUp = chUp;
	}

	// TODO see bug 80
	public enum States {
		REST, UP, SWITCH_DOWN_2_UP, SWITCH_UP_2_DOWN, DOWN
	};

	private States state = States.REST;

	@Override
	public void initializeOutput(RememberedOutput ro) {
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
			// TODO safety time, door hier ook te checken op timeStateStart, delay-protection moet verstreken zijn. Overal waar REST
			// gezet wordt moet die tijd ook gezet worden.
			if (gotUp) {
				setStateAndEntryUp(current);
				log.info("Screen " + getName() + " is going UP, for maximum "
						+ getMotorOnPeriod() + " sec.");
			} else if (gotDown) {
				setStateAndEntryDown(current);
				log.info("Screen " + getName() + " is going DOWN, for maximum "
						+ getMotorOnPeriod() + " sec.");
			}
			break;
		case DOWN:
			if (gotDown) {
				exitDown(current);
				state = States.REST;
				log.info("Screen " + getName()
						+ " stopped going down due to DOWN event.");
			} else if (gotUp) {
				exitDown(current);
				state = States.SWITCH_DOWN_2_UP;
				log.info("Screen "
						+ getName()
						+ " stopped going down due to UP event. Will go up after safety time.");
			} else if ((current - timeStateStart) > motorOnPeriodMs) {
				exitDown(current);
				state = States.REST;
				log.info("Screen "
						+ getName()
						+ " stopped going down because motor-on time is reached.");
			}
			break;
		case UP:
			if (gotDown) {
				exitUp(current);
				state = States.SWITCH_UP_2_DOWN;
				log.info("Screen "
						+ getName()
						+ " stopped going up due to DOWN event. Will go down after safety time.");
			} else if (gotUp) {
				exitUp(current);
				state = States.REST;
				log.info("Screen " + getName()
						+ " stopped going up due to UP event.");
			} else if ((current - timeStateStart) > motorOnPeriodMs) {
				exitUp(current);
				state = States.REST;
				log.info("Screen " + getName()
						+ " stopped going up because motor-on time is reached.");
			}
			break;
		case SWITCH_UP_2_DOWN:
			if (gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				log.info("Screen "
						+ getName()
						+ " got event while switching up to down, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				setStateAndEntryDown(current);
				log.info("Screen " + getName() + " going UP after safety time.");
			}
			break;
		case SWITCH_DOWN_2_UP:
			if (gotDown || gotUp) {
				timeStateStart = current;
				state = States.REST;
				log.info("Screen "
						+ getName()
						+ " got event while switching down to up, strange. Therefore stop screen.");
			} else if ((current - timeStateStart) > MOTOR_SWITCH_DELAY_PROTECTION) {
				setStateAndEntryUp(current);
				log.info("Screen " + getName()
						+ " going DOWN after safety time.");
			}
			break;
		}
		gotUp = gotDown = false;
	}

	private void setStateAndEntryUp(long current) {
		timeStateStart = current;
		state = States.UP;
		getHw().writeDigitalOutput(chUp, true);
	}

	private void exitUp(long current) {
		timeStateStart = current;
		getHw().writeDigitalOutput(chUp, false);
	}

	private void setStateAndEntryDown(long current) {
		timeStateStart = current;
		state = States.DOWN;
		getHw().writeDigitalOutput(getChannel(), true);
	}

	private void exitDown(long current) {
		timeStateStart = current;
		getHw().writeDigitalOutput(getChannel(), false);
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open or close a screen.
	 */
	public int getMotorOnPeriod() {
		return (int) (motorOnPeriodMs / 1000);
	}

	/**
	 * Time in seconds a screen motor is working, i.e. time to completely open or close a screen.
	 */
	public void setMotorOnPeriod(int motorOnPeriod) {
		this.motorOnPeriodMs = motorOnPeriod * 1000L;
	}

	@Override
	public BlockInfo getBlockInfo() {
		return new BlockInfo(this.getName(),this.getClass().getSimpleName(), getDescription());
	}

	@Override
	public void update(String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "Screen " + super.toString() + ", motorOnPeriod="
				+ motorOnPeriodMs + ", state=" + state + "]";
	}
}
