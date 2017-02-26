package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoOnOff;

/**
 * A Fan that runs for a {@link #getOnDurationSec()} seconds when toggled on.
 * <P>
 * The fan also supports delayed on and off, useful when connected to a Lamp (or
 * any other event source): if the lamp goes on then after
 * {@link #getDelayOff2OnSec()} the fan will start running and goes off
 * after the lamp was out for {@link #getDelayOn2OffSec()} seconds.
 * <p>
 * The state diagram below shows all possibilities.
 * <p>
 * <img src="./doc-files/FanStateChart.jpg">
 * <p>
 * <ul>
 * <li>TODO</li>
 * <li></li>
 * </ul>
 * FIXME externalize state machine - has become too complex and need encapsulation
 * @author Dirk Vaneynde
 */

/*
 * BUG
2017-02-26 18:55:35 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Fan 'VentilatorWC0' received delay-off, keep running for 180 sec.
2017-02-26 18:55:35 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes OFF, toggle() called.
2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.sensors.Switch - Switch 'SchakLichtWC0' notifies SINGLE click event (seq=30969012).
2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Fan 'VentilatorWC0' in delay for ON for 180 sec.
2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes on, on() called.
2017-02-26 18:57:23 [Oscillator] INFO  eu.dlvm.domotics.actuators.Lamp - Lamp 'LichtWC0' goes ON, toggle() called.
2017-02-26 18:57:29 [Oscillator] INFO  eu.dlvm.domotics.sensors.Switch - Switch 'SchakLichtWC0' notifies SINGLE click event (seq=30969281).
2017-02-26 18:57:29 [Oscillator] INFO  eu.dlvm.domotics.actuators.Fan - Lamp goes off before delay period has expired. No fanning.
2017-02-26 18:57:29 [Oscillator] WARN  eu.dlvm.domotics.actuators.Fan - delayOff ignored, is missing code. status=Fan [onDurationMs=300000, delayToOnDurationMs=180000, delayTo
OffDurationMs=180000, timeStateEntered=-1, state=OFF] 
MAAR: ventilator blijft draaien, ging dus niet echt uit !
*/
public class Fan extends Actuator implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(Fan.class);

	/**
	 * Default time fan will be held off to run when delay-on event (typically
	 * connected lamp going on) has been received.
	 */
	public static final int DEFAULT_DELAY_OFF2ON_SEC = 120;
	/**
	 * Default time fan keeps running when delay-off event (typically connected
	 * lamp going off) has been received.
	 */
	public static final int DEFAULT_DELAY_ON2OFF_SEC = 180;
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
	 * ON_AFTER_DELAY is in combination with OFF_DELAY2ON and _OFF.
	 * <p>
	 * <img src="doc-files/FanStatechart.jpg"/>
	 * 
	 * @author dirk
	 */
	public enum States {
		OFF, ON, OFF_DELAY2ON, ON_AFTER_DELAY, ON_DELAY2OFF
	};

	private States state;
	private long onDurationMs = DEFAULT_ON_DURATION_SEC * 1000L;
	private long delayToOnDurationMs = DEFAULT_DELAY_OFF2ON_SEC * 1000L;
	private long delayToOffDurationMs = DEFAULT_DELAY_ON2OFF_SEC * 1000L;
	private long timeStateEntered = -1L;

	/**
	 * Constructor.
	 */
	public Fan(String name, String description, String channel, IDomoticContext ctx) {
		super(name, description, null, channel, ctx);
		this.state = States.OFF;
	}

	public long getOnDurationSec() {
		return onDurationMs / 1000L;
	}

	public void setOnDurationSec(long runPeriodSec) {
		this.onDurationMs = runPeriodSec * 1000L;
	}

	public Fan overrideOnDurationSec(long runPeriodSec) {
		setOnDurationSec(runPeriodSec);
		return this;
	}

	public long getDelayOff2OnSec() {
		return delayToOnDurationMs / 1000L;
	}

	public void setDelayOff2OnSec(long delayOnPeriodSec) {
		this.delayToOnDurationMs = delayOnPeriodSec * 1000L;
	}

	public Fan overrideDelayOff2OnSec(long delayOnPeriodSec) {
		setDelayOff2OnSec(delayOnPeriodSec);
		return this;
	}

	public long getDelayOn2OffSec() {
		return delayToOffDurationMs / 1000L;
	}

	public void setDelayOn2OffSec(long delayOffPeriodSec) {
		this.delayToOffDurationMs = delayOffPeriodSec * 1000L;
	}

	public Fan overrideDelayOn2OffSec(long delayOffPeriodSec) {
		setDelayOn2OffSec(delayOffPeriodSec);
		return this;
	}

	@Override
	public void initializeOutput(RememberedOutput ro) {
		writeOutput(false);
	}

	public boolean isOn() {
		return (state == States.ON || state == States.ON_AFTER_DELAY || state == States.ON_DELAY2OFF);
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
		case OFF_DELAY2ON:
			changeState(States.ON_AFTER_DELAY);
			fanOn = true;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes ON for " + getOnDurationSec() + " sec. or until switched off (state=" + getState() + ").");
			break;
		case ON_AFTER_DELAY:
			changeState(States.OFF_DELAY2ON);
			fanOn = false;
			writeOutput(fanOn);
			logger.info("Fan '" + getName() + "' goes off and into OFF_DELAY2ON because of toggle.");
			break;
		case ON:
		case ON_DELAY2OFF:
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
	 * Only effective when in state ON_AFTER_DELAY, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		changeState(States.OFF);
		writeOutput(false);
		logger.info("Fan '" + getName() + "' goes OFF (really-off) (state=" + getState() + ").");
	}

	public void delayOn() {
		// TODO check state...
		state = States.OFF_DELAY2ON;
		timeStateEntered = -1L;
		logger.info("Fan '" + getName() + "' in delay OFF to ON for " + getDelayOff2OnSec() + " sec.");
	}

	public void delayOff() {
		// TODO check state...
		switch (state) {
		case ON_AFTER_DELAY:
			state = States.ON_DELAY2OFF;
			timeStateEntered = -1L;
			logger.info("Fan '" + getName() + "' received delay-off, keep running for " + getDelayOn2OffSec() + " sec.");
			break;
		case OFF_DELAY2ON:
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
				logger.info("Fan '" + getName() + "' goes off because it has run for " + getOnDurationSec() + " sec (seq=" + sequence + ").");
			}
			break;
		case OFF_DELAY2ON:
			if ((current - timeStateEntered) > delayToOnDurationMs) {
				writeOutput(true);
				state = States.ON_AFTER_DELAY;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' stays ON for as long as no delay-off is received, or manual off.");
			}
			break;
		case ON_AFTER_DELAY:
			//  if running too long, without delay-off, then stop too
			if ((current - timeStateEntered) > onDurationMs) {
				writeOutput(false);
				state = States.OFF;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' goes off (while on because lamp triggered this) because it has run for " + getOnDurationSec() + " sec (seq=" + sequence + ").");
			}
			break;
		case ON_DELAY2OFF:
			if ((current - timeStateEntered) > delayToOffDurationMs) {
				writeOutput(false);
				state = States.OFF;
				timeStateEntered = current;
				logger.info("Fan '" + getName() + "' goes off because delayed OFF has been busy for " + getOnDurationSec() + " sec.");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	// ===== UI =====

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, getState().toString(), isOn());
		// TODO time still running, if running
		return uiInfo;
	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on '" + getName() + "' got unsupported action '" + action + ".");
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
