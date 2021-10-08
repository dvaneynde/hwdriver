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
import eu.dlvm.domotics.service.uidata.UiInfoOnOff;

/**
 * A Fan that runs for a {@link #getOnDurationSec()} seconds when toggled on.
 * <P>
 * The fan also supports delayed on and off, useful when connected to a Lamp (or
 * any other event source): if the lamp goes on then after
 * {@link #getDelayOff2OnSec()} the fan will start running and goes off after
 * the lamp was out for {@link #getDelayOn2OffSec()} seconds.
 * <p>
 * The state diagram below shows all possibilities.
 * <p>
 * <img src="./doc-files/FanStateChart.jpg">
 * <p>
 * <ul>
 * <li>TODO</li>
 * <li></li>
 * </ul>
 * FIXME externalize state machine - has become too complex and need
 * encapsulation
 * 
 * @author Dirk Vaneynde
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

	private FanStatemachine statemachine;
	private long onDurationMs = DEFAULT_ON_DURATION_SEC * 1000L;
	private long delayToOnDurationMs = DEFAULT_DELAY_OFF2ON_SEC * 1000L;
	private long delayToOffDurationMs = DEFAULT_DELAY_ON2OFF_SEC * 1000L;

	/**
	 * Constructor.
	 */
	public Fan(String name, String description, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		super(name, description, null, channel, writer, builder);
		statemachine = new FanStatemachine(this);
	}

	// ========== Configuration
	
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

	public long getOnDurationMs() {
		return onDurationMs;
	}

	public long getDelayToOnDurationMs() {
		return delayToOnDurationMs;
	}

	public long getDelayToOffDurationMs() {
		return delayToOffDurationMs;
	}

	public long getDelayToOffDurationSec() {
		return delayToOffDurationMs/1000L;
	}

	@Override
	public void initializeOutput(RememberedOutput ro) {
		writeOutput(false);
	}

	// ========== Queries
	/**
	 * @return State of fan.
	 */
	public FanStatemachine.States getState() {
		return statemachine.getState();
	}

	public boolean isOn() { return statemachine.isFanning(); }
	

	// ========== Events
	
	/**
	 * Toggle between immediately turning and stopping.
	 */
	public boolean toggle() {
		return statemachine.toggle();
	}

	//	public void on() {
	//		if (!isOn())
	//			toggle();
	//	}
	//
	//	public void off() {
	//		if (isOn())
	//			reallyOff();
	//	}
	//
	
	/**
	 * Only effective when in state ON_DELAY, otherwise ignored.
	 * <p>
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		statemachine.reallyOff();
	}

	public void delayOn() {
		statemachine.delayOn();
	}

	public void delayOff() {
		statemachine.delayOff();
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
			// TODO should not be used at the moment?
			//on();
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

	// ===== UI =====

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, getState().toString(), statemachine.isFanning());
		// TODO time still running, if running
		return uiInfo;
	}

	@Override
	// FIXME must only be toggle, not on or off... or reallyOff could be added
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			statemachine.toggle();
		else if (action.equalsIgnoreCase("off"))
			statemachine.toggle();
		else
			logger.warn("update on '" + getName() + "' got unsupported action '" + action + ".");
	}


	// ===== Internal =====

	@Override
	public void loop(long current) {
		statemachine.loop(current);
	}


	void writeOutput(boolean val) {
		getHwWriter().writeDigitalOutput(getChannel(), val);
	}

	@Override
	public String toString() {
		return "Fan [name=" + name + ", description=" + description + ", uiGroup=" + uiGroup + ", onDurationMs=" + onDurationMs + ", delayToOnDurationMs="
				+ delayToOnDurationMs + ", delayToOffDurationMs=" + delayToOffDurationMs + ", statemachine=" + statemachine + "]";
	}
}
