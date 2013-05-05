package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

/**
 * A Fan that runs for a {@link #getRunningPeriodSec()} seconds when toggled on.
 * Optionally it can be connected to a {@link Lamp}, so that if the lamp goes on
 * after {@link #getDelayPeriodSec()} the lamp will run for
 * {@link #getRunningPeriodSec()} seconds.
 * <p>
 * The state diagram below shows all possibilities.
 * <p>
 * <img src="./doc-files/Fan.jpg">
 * <p>
 * <ul>
 * <li>dT is the time spent in a given state</li>
 * <li>there is a toggle and long-toggle; long-toggle is only used in one case,
 * when the lamp is on and the fan runs you want to make sure the fan remains
 * off. With a normal toggle, after the delay period, the fan will go on again.</li>
 * </ul>
 * 
 * @author Dirk Vaneynde
 */

public class Fan extends Actuator {

	static Logger log = Logger.getLogger(Fan.class);

	/**
	 * If lamp is used, default time lamp has to be on in seconds before Fan
	 * runs.
	 */
	public static final int DEFAULT_LAMP_DELAY_PERIOD_SEC = 120;
	/**
	 * Default running period for a Fan.
	 */
	public static final int DEFAULT_RUN_PERIOD_SEC = 300;

	private int delayPeriodMs;
	private int runningPeriodMs;
	private long timeStateEntered;
	private boolean resetTimeStateEntered = false; // Flag loop() to reset
	// timeStateEntered to current time.
	private Lamp lamp;

	public enum States {
		REST, RUN, DELAYED_LAMP_ON, RUN_LAMP_ON, RUN_LAMP_OFF, WAIT_LAMP_OFF
	};

	private States state;

	/**
	 */
	public Fan(String name, String description, LogCh channel, IDomoContext ctx) {
		super(name, description, channel, ctx);
		this.state = States.REST;
		this.runningPeriodMs = 1000 * DEFAULT_RUN_PERIOD_SEC;
	}

	/**
	 */
	public Fan(String name, String description, Lamp lamp, LogCh channel
			, IDomoContext ctx) {
		this(name, description, channel, ctx);
		this.lamp = lamp;
		this.delayPeriodMs = 1000 * DEFAULT_LAMP_DELAY_PERIOD_SEC;
	}

	/**
	 * Constructor. Output is set to false, and transferred to hardware.
	 * <p>
	 * Note if this Fan is connected to a lamp for automatically going on and
	 * out, see {@link #Fan(String, String, Lamp, LogCh, IHardwareIO)}.
	 * 
	 * @param name
	 *            See superclass
	 *            {@link Actuator#Actuator(String, String, LogCh)}.
	 * @param description
	 *            See superclass.
	 * @param lamp
	 *            If not <code>null</code>, connects control of this fan to the
	 *            lamp. When lamp goes on, after {@link #getDelayPeriodSec()}
	 *            the fan starts running, when lamp goes out after
	 *            {@link #getRunningPeriodSec()} the fan stops running.
	 * @param logicalChannel
	 *            Logical output channel of fan.
	 * @param ctrl
	 */
	public Fan(String name, String description, Lamp lamp, int logicalChannel
			, IDomoContext ctx) {
		super(name, description, new LogCh(logicalChannel), ctx);
		this.state = States.REST;
		this.lamp = lamp;
	}

	/**
	 * @return Time in seconds that a lamp must be on, before the fan starts to
	 *         run.
	 */
	public int getDelayPeriodSec() {
		return delayPeriodMs / 1000;
	}

	/**
	 * @see #getDelayPeriodSec()
	 */
	public void setDelayPeriodSec(int seconds) {
		this.delayPeriodMs = seconds * 1000;
	}

	/**
	 * @return Period in seconds that a fan keeps running.
	 */
	public int getRunningPeriodSec() {
		return runningPeriodMs / 1000;
	}

	/**
	 * @see #getRunningPeriodSec()
	 */
	public void setRunningPeriodSec(int seconds) {
		this.runningPeriodMs = seconds * 1000;
	}

	@Override
	public void initializeOutput() {
		writeOutput(false);
	}

	/**
	 * @return true iff. fan is running.
	 */
	public boolean isRunning() {
		return (state == States.RUN || state == States.RUN_LAMP_ON || state == States.RUN_LAMP_OFF);
	}

	/**
	 * @return State of fan. See {@link States}.
	 */
	public States getState() {
		return state;
	}

	/* To be used outside {@link #loop()}. */
	private void changeState(States target) {
		state = target;
		resetTimeStateEntered = true;
	}

	/**
	 * Toggle between immediately turning and stopping. If started, it runs for
	 * {@link #getDelayPeriodSec()} seconds.
	 * <p>
	 * TODO could set a flag, which is treated in loop(). Perhaps cleaner,
	 * because only one thing can happen, i.e. stuff is easier to keep
	 * consistent?
	 */
	public void toggle() {
		switch (state) {
		case REST:
		case WAIT_LAMP_OFF:
		case DELAYED_LAMP_ON:
			changeState(States.RUN);
			writeOutput(true);
			log.info("Fan '" + getName() + "' goes ON for "
					+ getRunningPeriodSec()
					+ " sec. or until switched off (state=" + getState() + ").");
			break;
		case RUN:
		case RUN_LAMP_ON:
		case RUN_LAMP_OFF:
		default:
			changeState(States.REST);
			writeOutput(false);
			log.info("Fan '" + getName() + "' goes OFF because toggled.");
			break;
		}
	}

	/**
	 * Only effective when in state WAIT_LAMP_OFF, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void turnOffUntilLampOff() {
		switch (state) {
		case RUN_LAMP_ON:
			changeState(States.WAIT_LAMP_OFF);
			writeOutput(false);
			log.info("Fan '"
					+ getName()
					+ "' goes OFF, and will not turn on again because lamp is still ON (state="
					+ getState() + ").");
			break;
		default:
			// ignored;
			break;
		}
	}

	@Override
	public void loop(long current, long sequence) {
		if (resetTimeStateEntered) {
			// If some toggle occurred, that always changes state, so we reset
			// timeStateEntered.
			// If at same time something happens with lamp, no problem - that
			// changes state and thus timeStateEntered too.
			timeStateEntered = current;
			resetTimeStateEntered = false;
		}
		switch (state) {
		case REST:
			if ((lamp != null) && (lamp.isOn())) {
				state = States.DELAYED_LAMP_ON;
				timeStateEntered = current;
				log.info("Fan '" + getName() + "' watches lamp '"
						+ lamp.getName() + "' for " + getDelayPeriodSec()
						+ " sec. before going ON (seq=" + sequence + ").");
			}
			break;
		case RUN:
			if ((current - timeStateEntered) > runningPeriodMs) {
				writeOutput(false);
				state = States.REST;
				timeStateEntered = current;
				log.info("Fan '" + getName()
						+ "' goes off because it has run for "
						+ getRunningPeriodSec() + " sec (seq=" + sequence
						+ ").");
			}
			break;
		case DELAYED_LAMP_ON:
			if (!lamp.isOn()) {
				state = States.REST;
				timeStateEntered = current;
				log.info("Lamp goes off before delay period has expired. No fanning.");
			} else if ((current - timeStateEntered) > delayPeriodMs) {
				writeOutput(true);
				state = States.RUN_LAMP_ON;
				timeStateEntered = current;
				log.info("Fan '" + getName()
						+ "' stays ON  for as long as lamp '" + lamp.getName()
						+ "' is ON (seq=" + sequence + ").");
			}
			break;
		case RUN_LAMP_ON:
			if (!lamp.isOn()) {
				state = States.RUN_LAMP_OFF;
				timeStateEntered = current;
				log.info("Fan '" + getName() + "', lamp '" + lamp.getName()
						+ "'has gone out, keep running for "
						+ getRunningPeriodSec() + " sec (seq=" + sequence
						+ ").");
			}
			break;
		case RUN_LAMP_OFF:
			if ((current - timeStateEntered) > runningPeriodMs) {
				writeOutput(false);
				state = States.REST;
				timeStateEntered = current;
				log.info("Fan '" + getName() + "' goes off because lamp '"
						+ lamp.getName() + " has been OFF for "
						+ getRunningPeriodSec() + " sec (seq=" + sequence
						+ ").");
			}
			break;
		case WAIT_LAMP_OFF:
			if (!lamp.isOn()) {
				state = States.REST;
				timeStateEntered = current;
				log.info("Fan '"
						+ getName()
						+ "', lamp '"
						+ lamp.getName()
						+ "'has gone off, which I was waiting for to go to REST (seq="
						+ sequence + ").");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	private void writeOutput(boolean val) {
		hw().writeDigitalOutput(getChannel(), val);
	}

	@Override
	public String toString() {
		return "Fan (" + super.toString() + ") running=" + isRunning()
				+ " state=" + getState().name() + " delay="
				+ getDelayPeriodSec() + "s runperiod=" + getRunningPeriodSec()
				+ "s.";
	}

}
