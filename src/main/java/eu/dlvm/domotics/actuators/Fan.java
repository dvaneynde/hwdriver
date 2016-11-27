package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.UiInfo;

/**
 * A Fan that runs for a {@link #getOnDurationSec()} seconds when toggled on.
 * <P>The fan also supports delayed on and off, useful when connected to a Lamp (or any other event source): if the lamp goes on then
 * after {@link #getDelayOnDurationSec()} the fan will run until the lamp goes off
 * again plus  {@link #getDelayOffDurationSec()} seconds while the lamp is
 * off.
 * <p>
 * The state diagram below shows all possibilities.
 * <p>
 * <img src="./doc-files/FanStateChart.jpg">
 * <p>
 * <ul>
 * <li>TODO</li>
 * <li></li>
 * </ul>
 * 
 * @author Dirk Vaneynde
 */

public class Fan extends Actuator implements IEventListener {

	static Logger logger = LoggerFactory.getLogger(Fan.class);

	/**
	 * Default time fan will be held off to run when delay-on event (typically
	 * connected lamp going on) has been received.
	 */
	public static final int DEFAULT_DELAY_ON_DURATION_SEC = 120;
	/**
	 * Default time fan keeps running when delay-off event (typically connected
	 * lamp going off) has been received.
	 */
	public static final int DEFAULT_DELAY_OFF_DURATION_SEC = 180;
	/**
	 * Default running period for a Fan.
	 */
	public static final int DEFAULT_ON_DURATION_SEC = 300;
	/**
	 * Maximum time that a fan is allowed to run. <br/>
	 * TODO Must have minimal remain_off time.
	 */
	public static final int MAX_ON_PERIOD_SED = 10 * 60;

	/**
	 * ON_D is in combination with DELAYED_ON and _OFF.
	 * <p>
	 * <img src="doc-files/FanStatechart.jpg"/>
	 * 
	 * @author dirk
	 */
	public enum States {
		OFF, ON, DELAYED_ON, ON_D, DELAYED_OFF
	};

	private States state;
	private long onDurationMs = DEFAULT_ON_DURATION_SEC * 1000L;
	private long delayToOnDurationMs = DEFAULT_DELAY_ON_DURATION_SEC*1000L;
	private long delayToOffDurationMs = DEFAULT_DELAY_OFF_DURATION_SEC*1000L;
	private long timeStateEntered;

	/**
	 * Constructor.
	 */
	public Fan(String name, String description, String channel, IDomoticContext ctx) {
		super(name, description, null, channel, ctx);
		this.state = States.OFF;
	}

	public long getOnDurationSec() {
		return onDurationMs/1000L;
	}

	public Fan overrideOnDurationSec(long runPeriodSec) {
		this.onDurationMs = runPeriodSec*1000L;
		return this;
	}

	public long getDelayOnDurationSec() {
		return delayToOnDurationMs/1000L;
	}

	public Fan overrideDelayOnDurationSec(long delayOnPeriodSec) {
		this.delayToOnDurationMs = delayOnPeriodSec*1000L;
		return this;
	}

	public long getDelayOffDurationSec() {
		return delayToOffDurationMs/1000L;
	}

	public Fan overrideDelayOffDurationSec(long delayOffPeriodSec) {
		this.delayToOffDurationMs = delayOffPeriodSec*1000L;
		return this;
	}

	@Override
	public void initializeOutput(RememberedOutput ro) {
		writeOutput(false);
	}

	public boolean isOn() {
		return (state == States.ON || state == States.ON_D || state == States.DELAYED_OFF);
	}

	public void on() {
		if (!isOn())
			toggle();
	}

	public void off() {
		if (isOn())
			reallyOff();
	}
	
	/**
	 * Toggle between immediately turning and stopping. If started, it runs for
	 * {@link #getDelayPeriodSec()} seconds.
	 */
	public boolean toggle() {
		boolean fanOn;
		switch (state) {
		case OFF:
			changeState(States.ON);
			fanOn = true;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes ON (state=" + getState() + ").");
			break;
		case DELAYED_ON:
			changeState(States.ON_D);
			fanOn = true;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes ON for " + getOnDurationSec() + " sec. or until switched off (state="
					+ getState() + ").");
			break;
		case ON_D:
			changeState(States.DELAYED_ON);
			fanOn = false;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes off and into DELAYED_ON because of toggle.");
			break;
		case ON:
		case DELAYED_OFF:
		default:
			changeState(States.OFF);
			fanOn = false;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes OFF because toggled.");
			break;
		}
		return fanOn;
	}


	/**
	 * Only effective when in state ON_D, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		changeState(States.OFF);
		writeOutput(false);
		logger.info("Fan '" + getName() + "' goes OFF (state=" + getState() + ").");
	}

	public void delayOn() {
		state = States.DELAYED_ON;
		timeStateEntered = -1L;
		logger.info("Fan '" + getName() + "' in delay for ON for " + getDelayOnDurationSec() + " sec.");
	}

	public void delayOff() {
		switch (state) {
		case ON_D:
			state = States.DELAYED_OFF;
			timeStateEntered = -1L;
			logger.info("Fan '" + getName() + "' received delay-off, keep running for " + getDelayOffDurationSec() + " sec.");
			break;
		case DELAYED_ON:
			state = States.OFF;
			timeStateEntered = -1L;
			logger.info("Lamp goes off before delay period has expired. No fanning.");
		default:
			logger.warn("delayOff ignored, is missing code. status=" + this.toString());
		}
	}


	/* To be used outside {@link #loop()}. */
	private void changeState(States target) {
		state = target;
		timeStateEntered = -1L;
	}

	/**
	 * @return State of fan. See {@link States}.
	 */
	public States getState() {
		return state;
	}

	/**
	 * Reacts on {@see IOnOffToggle.ActionType} events, with some pecularities:
	 * <ul>
	 * <li>ON: if off, behaves like {@link #toggle()}, otherwise no effect</li>
	 * <li>OFF: if on, behaves as {@link #reallyOff()}</li>
	 * <li>TOGGLE: see {@link #toggle()}</li>
	 * </ul>
	 * TODO add check on source; if lamp then it should sync wiht lamp
	 */
	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			on();
			break;
		case OFF:
			reallyOff();
			break;
		case TOGGLE:
			toggle();
			break;
		case DELAY_ON:
			delayOn();
			break;
		case DELAY_OFF:
			delayOff();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	// ===== Internal =====

	@Override
	public void loop(long current, long sequence) {
		if (timeStateEntered == -1L)
			timeStateEntered = current;
		switch (state) {
		case OFF:
			break;
		case ON:
			if ((current - timeStateEntered) > onDurationMs) {
				writeOutput(false);
				state = States.OFF;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' goes off because it has run for " + getOnDurationSec() + " sec (seq="
						+ sequence + ").");
			}
			break;
		case DELAYED_ON:
			if ((current - timeStateEntered) > delayToOnDurationMs) {
				writeOutput(true);
				state = States.ON_D;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' stays ON for as long as no delay-off is received, or manual off.");
			}
			break;
		case ON_D:
			// TODO if running for 10 minutes, without delay-off, then stop too
			break;
		case DELAYED_OFF:
			if ((current - timeStateEntered) > onDurationMs) {
				writeOutput(false);
				state = States.OFF;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' goes off because delayed OFF has been busy for " + getOnDurationSec()
						+ " sec.");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	// ===== UI =====

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(this);
		//bi.addParm("on", isOn() ? "1" : "0");
		bi.setOn(isOn());
		// TODO time still running, if running
		return bi;
	}

	@Override
	public void update(String action) {
	}

	// ===== Private =====

	private void writeOutput(boolean val) {
		getHw().writeDigitalOutput(getChannel(), val);
	}

	@Override
	public String toString() {
		return "Fan [onDurationMs=" + onDurationMs + ", delayToOnDurationMs=" + delayToOnDurationMs + ", delayToOffDurationMs=" + delayToOffDurationMs
				+ ", timeStateEntered=" + timeStateEntered + ", state=" + state + "]";
	}


}
