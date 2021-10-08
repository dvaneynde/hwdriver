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
import eu.dlvm.domotics.service.uidata.UiInfoOnOffEco;

/**
 * Lamp - or anything that can go on or off - with optional auto-off.
 * <p>
 * When {@link @isEcoEnabled()} then the eco mode is enabled. If
 * {@link #isEco()}==true, the lamp goes out automatically after
 * {@link #getAutoOffSec()} seconds.
 * <p>
 * If on top {@link #isBlink()}==true, then the following happens:
 * <ul>
 * <li>After {@link #getAutoOffSec()} seconds the lights go off/on/off/on with a
 * 1 second interval.</li>
 * <li>If within 5 seconds after these blinks the {@link #toggle()} or
 * {@link #on()} have not been called the lights go off. If {@link #toggle()} or
 * {@link #on()} were called the auto-off timer is reset.
 * </ul>
 * <p>
 * Blinking is out by default, e.g. fluo lamps and blinking do not go together
 * well.
 * 
 * @author dirk
 *
 *         TODO merge blink:boolean and blinks:int
 */
public class Lamp extends Actuator implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(Lamp.class);
	private States state;
	private long timeStateEntered = -1L;
	// Eco stuff
	private boolean ecoEnabled;
	private boolean eco, blink;
	private int blinkCtr = 0;
	private int blinks = DEFAULT_BLINK_COUNT;
	private int autoOffSec = DEFAULT_AUTO_OFF_SEC;

	/** If {@link #isEco()} then this is the default on time. */
	public static final int DEFAULT_AUTO_OFF_SEC = 10 * 60;
	/**
	 * Number of blinks, default is 1 (i.e. being On, then Off, then remains On
	 * for grace period.
	 */
	public static final int DEFAULT_BLINK_COUNT = 1;
	/** Time in ms. between on and off, or off and on. */
	public static final long BLINK_TIME_MS = 500;

	public enum States {
		ON, OFF, GOING_OFF_BLINK, GOING_OFF_UNLESS_CLICK;
	}

	public Lamp(String name, String description, boolean isEcoEnabled, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		this(name, description, isEcoEnabled, null, channel, writer, builder);
	}

	public Lamp(String name, String description, boolean isEcoEnabled, String ui, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		super(name, description, ui, channel, writer, builder);
		ecoEnabled = isEcoEnabled;
		state = States.OFF;
	}

	public boolean isEcoEnabled() {
		return ecoEnabled;
	}

	public boolean isEco() {
		return eco && isEcoEnabled();
	}

	public void setEco(boolean eco) {
		if (!isEcoEnabled()){
			logger.warn("Ignore setting eco since eco is disabled: "+this);
			return;
		}
		this.eco = eco;
	}

	public boolean isBlink() {
		return blink;
	}

	public void setBlink(boolean blink) {
		this.blink = blink;
	}

	public int getBlinks() {
		return blinks;
	}

	public void setBlinks(int blinks) {
		this.blinks = blinks;
	}

	public int getAutoOffSec() {
		return autoOffSec;
	}

	public void setAutoOffSec(int maxOnSec) {
		this.autoOffSec = maxOnSec;
	}

	/**
	 * @return true iff. lamp is on (or blinking while about to go off)
	 */
	public boolean isOn() {
		return (state != States.OFF);
	}

	public States getState() {
		return state;
	}

	/**
	 * Toggles the output.
	 * 
	 * @return New output state.
	 */
	public boolean toggle() {
		switch (state) {
		case ON:
			internalOff();
			logger.info("Lamp '" + getName() + "' goes OFF, toggle() called.");
			break;
		case OFF:
			internalOn();
			logger.info("Lamp '" + getName() + "' goes ON, toggle() called.");
			break;
		case GOING_OFF_BLINK:
		case GOING_OFF_UNLESS_CLICK:
			// might be blinking, so force on
			on();
			logger.info("Lamp '" + getName() + "' was going OFF, but now ON since toggle() called.");
			break;
		}
		return isOn();
	}

	public void on() {
		internalOn();
		logger.info("Lamp '" + getName() + "' goes ON, on() called.");
	}

	private void internalOn() {
		timeStateEntered = -1;
		state = States.ON;
		writeOutput(true);
		notifyListeners(EventType.ON);
	}

	public void off() {
		internalOff();
		logger.info("Lamp '" + getName() + "' goes OFF, off() called.");
	}

	private void internalOff() {
		timeStateEntered = -1;
		state = States.OFF;
		writeOutput(false);
		notifyListeners(EventType.OFF);
	}

	private void internalChangeEco(boolean newEco) {
		if (isEco() == newEco)
			return;
		if (newEco)
			setEco(true);
		else {
			switch (state) {
			case OFF:
			case ON:
				setEco(false);
				break;
			case GOING_OFF_BLINK:
			case GOING_OFF_UNLESS_CLICK:
				writeOutput(true);
				blinkCtr = 0;
				state = States.ON;
				timeStateEntered = -1L;
				break;
			}
		}
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
		case LIGHT_LOW:
			on();
			break;
		case OFF:
		case LIGHT_HIGH:
			off();
			break;
		case TOGGLE:
			toggle();
			break;
		case ECO_ON:
			internalChangeEco(true);
			logger.info("Lamp '" + getName() + "' has set eco mode ON.");
			break;
		case ECO_OFF:
			internalChangeEco(false);
			logger.info("Lamp '" + getName() + "' has set eco mode OFF.");
			break;
		case ECO_TOGGLE:
			internalChangeEco(!isEco());
			logger.info("Lamp '" + getName() + "' has toggled eco mode to " + (isEco() ? "ON" : "OFF") + ".");
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	// ===== UI =====

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else if (action.equalsIgnoreCase("ecoToggle"))
			internalChangeEco(!isEco());
		else
			logger.warn("update on Lamp '" + getName() + "' got unsupported action '" + action + ".");
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo uiInfo = isEcoEnabled() ? new UiInfoOnOffEco(this, getState().toString(), isOn(), isEco())
				: new UiInfoOnOff(this, getState().toString(), isOn());
		return uiInfo;
	}

	// ===== Init =====

	@Override
	public void initializeOutput(RememberedOutput ro) {
		if (ro != null) {
			if (ro.getVals()[0] == 1)
				internalOn();
			else
				internalOff();
		}
	}

	@Override
	public RememberedOutput dumpOutput() {
		return new RememberedOutput(getName(), new int[] { isOn() ? 1 : 0 });
	}

	// ===== Internal =====

	@Override
	public void loop(long currentTime) {
		if (timeStateEntered == -1L)
			timeStateEntered = currentTime;
		if (!eco)
			return;
		// Below is only executed when in ECO mode.
		switch (state) {
		case OFF:
			break;
		case ON:
			if ((currentTime - timeStateEntered) > autoOffSec * 1000L) {
				if (isBlink()) {
					timeStateEntered = currentTime;
					state = States.GOING_OFF_BLINK;
					writeOutput(false);
					blinkCtr = 1;
					logger.info("Lamp '" + getName() + "' is about to go off because eco mode and it has been on for "
							+ getAutoOffSec() + " sec.");
				} else {
					internalOff();
					logger.info("Lamp '" + getName() + "' goes OFF because eco is enabled and " + autoOffSec
							+ " sec. have passed.");
				}
			}
			break;
		case GOING_OFF_BLINK:
			if ((currentTime - timeStateEntered) >= BLINK_TIME_MS) {
				blinkCtr++;
				if (blinkCtr >= 2 * blinks) {
					writeOutput(true);
					blinkCtr = 0;
					state = States.GOING_OFF_UNLESS_CLICK;
				} else {
					writeOutput(blinkCtr % 2 == 0);
				}
				timeStateEntered = currentTime;
			}
			break;
		case GOING_OFF_UNLESS_CLICK:
			if ((currentTime - timeStateEntered) >= 5 * 1000L) {
				internalOff();
				logger.info("Lamp '" + getName() + "' goes OFF because eco is enabled and " + autoOffSec
						+ " sec. have passed and not interrupted by user.");
			}
		}
	}

	private void writeOutput(boolean val) {
		getHwWriter().writeDigitalOutput(getChannel(), val);
	}

	@Override
	public String toString() {
		return "Lamp [name=" + name + ", state=" + state + ", eco=" + eco + ", blinkCtr=" + blinkCtr + ", autoOffSec="
				+ autoOffSec + "]";
	}
}

