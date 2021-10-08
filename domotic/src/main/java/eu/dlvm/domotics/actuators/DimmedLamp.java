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
import eu.dlvm.domotics.service.uidata.UiInfoOnOffLevel;

/**
 * Dimmed Lamp.
 * <p>
 * When going up from level 0, at the first loop() output is still 0.
 * 
 * @author dirk
 */
public class DimmedLamp extends Actuator implements IEventListener, IUiCapableBlock {
	/**
	 * Time in ms to dim from off to fully on.
	 */
	public static final int DEFAULT_FULL_DIMTIME_MS = 5000; // milliseconds

	private static Logger logger = LoggerFactory.getLogger(DimmedLamp.class);

	private double level, prevOnLevel;
	private int factorHwOut;
	private int msTimeFullDim = DEFAULT_FULL_DIMTIME_MS;
	private long lastUpDnLoopTime;
	private States state;

	public enum States {
		OFF, ON, UP, DOWN
	};

	/**
	 * Create a Dimmed Lamp.
	 * 
	 * @param name
	 *            Identifying name of this Dimmed Lamp.
	 * @param description
	 *            Description.
	 * @param outputValueHardwareIfFull
	 *            Byte or word value put on the analog channel when full.
	 * @param channel
	 *            Logical id of analog output channel.
	 * @param writer
	 *            Link to underlying hardware layer.
	 */
	public DimmedLamp(String name, String description, String ui, int outputValueHardwareIfFull, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		super(name, description, ui, channel, writer, builder);
		this.factorHwOut = outputValueHardwareIfFull;
		state = States.OFF;
		level = 0;
		prevOnLevel = 100;
	}

	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		this(name, description, null, outputValueHardwareIfFull, channel, writer, builder);
	}

	/**
	 * Create a Dimmed Lamp.
	 * 
	 * @param name
	 *            Identifying name of this Dimmed Lamp.
	 * @param description
	 *            Description.
	 * @param outputValueHardwareIfFull
	 *            Byte or word value put on the analog channel when full.
	 * @param channel
	 *            Logical id of analog output channel.
	 * @param writer
	 *            Link to underlying hardware layer.
	 * @deprecated
	 */
	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, int channel, IHardwareWriter writer, IDomoticBuilder builder) {
		this(name, description, null, outputValueHardwareIfFull, Integer.toString(channel), writer, builder);
	}

	/**
	 * Initializes dimmed lamp to 50% on.
	 */
	@Override
	public void initializeOutput(RememberedOutput ro) {
		if (ro == null) {
			logger.info("Dimmed Lamp initializing to ON, 50% - default behaviour when no previous state known.");
			on(50);
		} else {
			if (ro.getVals()[0] == 0)
				off();
			else
				on(ro.getVals()[1]);
		}
	}

	@Override
	public RememberedOutput dumpOutput() {
		return new RememberedOutput(getName(), new int[] { (getState() == States.OFF ? 0 : 1), getLevel() });
	}

	/**
	 * @return The time to dim between 0% and 100%, in milliseconds.
	 */
	public int getMsTimeFullDim() {
		return msTimeFullDim;
	}

	/**
	 * @param msTimeFullDim
	 *            time to dim between 0% and 100%, in milliseconds.
	 */
	public void setMsTimeFullDim(int msTimeFullDim) {
		this.msTimeFullDim = msTimeFullDim;
	}

	/**
	 * Current level of lamp, between 0 and 100. Note that a lamp can be in
	 * state ON (see {@link #getState()}) and at the same time its level is 0.
	 * <br/>
	 * TODO make double
	 */
	public int getLevel() {
		if (getState() == States.OFF)
			return (int) prevOnLevel;
		else
			return (int) level;
	}

	/**
	 * Current state of dimmed lamp.
	 */
	public States getState() {
		return state;
	}

	/**
	 * Toggles between on and off.
	 * <p>
	 * When going on the level of the last on-state is taken. For instance, if
	 * lamp was 50% and then switched off, it will go on to 50% again.
	 */
	public boolean toggle() {
		switch (state) {
		case OFF:
			on();
			break;
		case ON:
			off();
			break;
		case UP:
		case DOWN:
			logger.warn("User did toggle, but I'm dimming up or down - should not be possible. Switching to off.");
			off();
			break;
		}
		return (getState() == States.ON);
	}

	/**
	 * Switch lamp on, to previous on level (could be 0, so if lamp was off user
	 * won't see a difference; but the same is true if the lamp was before on at
	 * 1%... so we don't bother about this).
	 */
	public void on() {
		level = prevOnLevel;
		state = States.ON;
		writeAnalogOutput();
		logger.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
	}

	/**
	 * FIXME opsplitsen on/off en setlevel? Switch lamp on if necessary, and set
	 * level to given percentage (range [0..100]).
	 * 
	 * @param newLevel
	 *            Level of lamp as percentage.
	 */
	public void on(int newLevel) {
		if ((newLevel < 0) || (newLevel > 100)) {
			logger.warn("DimmedLamp on(" + newLevel + ") called, argument not in range [0..100]. Operation is ignored.");
			return;
		}
		level = newLevel;
		state = States.ON;
		writeAnalogOutput();
		logger.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
	}

	/**
	 * Switch lamp off.
	 */
	public void off() {
		if (state != States.OFF) {
			prevOnLevel = level;
			level = 0;
			state = States.OFF;
			writeAnalogOutput();
			logger.info("DimmedLamp '" + getName() + "' set to OFF (remembered level: " + prevOnLevel + "%)");
		}
	}

	/**
	 * Start or stop increasing level of lamp. Typically used to connect to an
	 * up-switch.
	 * <p>
	 * One call <code>up(true)</code> will cause an increase of the lamp level
	 * at each {@link #loop(long)}, until <code>up(false)</code> is called
	 * or any other state-changing call.
	 * <p>
	 * If state was off, lamp will first go to remembered level and then start
	 * increasing.
	 * 
	 * @param active
	 *            true to start increasing, false to stop increasing
	 */
	public void up(boolean active) {
		switch (state) {
		case OFF:
			if (!active) {
				logger.warn("Caller desactivates up, but I am OFF. Ignored.");
			} else {
				logger.debug("up(true), going from OFF to UP.");
				level = 0;
				lastUpDnLoopTime = -1L;
				state = States.UP;
			}
			break;
		case ON:
			if (!active) {
				logger.warn("Caller desactivates up, but I am ON. Ignored.");
			} else {
				lastUpDnLoopTime = -1L;
				state = States.UP;
			}
			break;
		case UP:
			if (active) {
				logger.warn("Caller activates up, but I am already UP. Ignored.");
			} else {
				state = States.ON;
				logger.info("DimmedLamp '" + getName() + "' dimmed up to level: " + level + '%');
			}
			break;
		case DOWN:
			logger.warn("Caller activates/deactivates (active=" + active + ") up, but I am in DOWN. Ignored.");
			break;
		default:
			throw new RuntimeException();
		}
	}

	/**
	 * Start or stop de-creasing level of lamp. Typically used to connect to a
	 * down-switch.
	 * <p>
	 * One call <code>down(true)</code> will cause a decrease of the lamp level
	 * at each {@link #loop(long)}, until <code>down(false)</code> is
	 * called or any other state-changing call.
	 * <p>
	 * If state was off, lamp will first go to remembered level and then start
	 * increasing.
	 * 
	 * @param active
	 *            true to start decreasing, false to stop
	 */
	public void down(boolean active) {
		switch (state) {
		case OFF:
			if (!active) {
				logger.warn("Caller desactivates down, but I am OFF. Ignored");
			} else {
				level = prevOnLevel;
				lastUpDnLoopTime = -1L;
				state = States.DOWN;
			}
			break;
		case ON:
			if (!active) {
				logger.warn("Caller desactivates down, but I am ON. Ignored");
			} else {
				lastUpDnLoopTime = -1L;
				state = States.DOWN;
			}
			break;
		case UP:
			logger.warn("Caller activates/deactivates (active=" + active + ") down, but I am in UP. Ignored");
			break;
		case DOWN:
			if (active) {
				logger.warn("Caller activates down, but I am already DOWN. Ignored");
			} else {
				state = States.ON; // Note: ON even if 0%; light strength is
				// basically the same as 1%, so...
				logger.info("DimmedLamp '" + getName() + "' dimmed down to level: " + level + "%, state is ON");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	/**
	 * For the dimmers, depending on the input the following happens (only
	 * describing the LEFT part):
	 * <ul>
	 * <li>LEFT_CLICK - switch on/off</li>
	 * <li>LEFT_HOLD_DOWN - start dimming; if light was off first turn it on to
	 * previous on-level</li>
	 * <li>LEFT_RELEASED - stop dimming.</li>
	 * <li>LEFT_WITH_RIGHTCLICK - set to full strength</li>
	 * </ul>
	 */
	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			on();
			break;
		case OFF:
			off();
			break;
		case TOGGLE:
			toggle();
			break;
		case LEFT_CLICK:
		case RIGHT_CLICK:
			toggle();
			break;
		case LEFT_HOLD_DOWN:
			down(true);
			break;
		case LEFT_RELEASED:
			down(false);
			break;
		case RIGHT_HOLD_DOWN:
			up(true);
			break;
		case RIGHT_RELEASED:
			up(false);
			break;
		case LEFT_WITH_RIGHTCLICK:
		case RIGHT_WITH_LEFTCLICK:
			on(100);
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	@Override
	public void loop(long current) {
		switch (state) {
		case OFF:
		case ON:
			break;
		case UP:
		case DOWN:
			/*
			 * Tussen 0 en 1 moet in T ms. Loop gebeurt om elke I ms. Dus is
			 * stapgrootte I/T.
			 */
			if (lastUpDnLoopTime != -1) {
				// This is skipped at the first loop() while UP or DOWN
				double change = ((double) (current - lastUpDnLoopTime)) * 100.0d / (double) msTimeFullDim;
				logger.debug("Going (event=)" + state.toString() + ", change=" + change + ", current level=" + level + ", current-last="
						+ (current - lastUpDnLoopTime) + ", timeFullDimMs=" + msTimeFullDim);
				if (state == States.UP) {
					level += change;
					if (level > 100)
						level = 100;
				} else {
					level -= change;
					if (level < 0) {
						level = 0;
					}
				}
				logger.debug("After change, level =" + level);
			}
			// also write if first time up or down, since if the lamp was OFF we
			// first have to put it to ON.
			writeAnalogOutput();
			lastUpDnLoopTime = current;
			break;
		default:
			throw new RuntimeException();
		}
	}

	private void writeAnalogOutput() {
		getHwWriter().writeAnalogOutput(getChannel(), (int) (level * factorHwOut / 100));
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOffLevel uiInfo = new UiInfoOnOffLevel(this, getState().toString(), getState() != States.OFF, getLevel());
		return uiInfo;
	}

	@Override
	public void update(String action) {
		// dirty, but effective...
		try {
			EventType at = EventType.valueOf(action.toUpperCase());
			onEvent(null, at);
			return;
		} catch (IllegalArgumentException e) {
		}
		try {
			int level = Integer.parseInt(action);
			if (level > 0)
				on(level);
			else
				off();
		} catch (NumberFormatException e) {
			logger.warn("update(); unknown action=" + action);
		}
	}

	@Override
	public String toString() {
		return "DimmedLamp (" + super.toString() + ") level=" + getLevel() + " state=" + getState().name();
	}

}
