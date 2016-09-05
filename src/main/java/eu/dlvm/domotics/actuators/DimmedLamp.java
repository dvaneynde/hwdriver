package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.service.UiInfo;
import eu.dlvm.iohardware.LogCh;

/**
 * Dimmed Lamp.
 * <p>
 * When going up from level 0, at the first loop() output is still 0.
 * 
 * @author dirk
 */
public class DimmedLamp extends Actuator implements IOnOffToggleCapable {
	/**
	 * Time in ms to dim from off to fully on.
	 */
	public static final int DEFAULT_FULL_DIMTIME_MS = 5000; // milliseconds

	static Logger log = LoggerFactory.getLogger(DimmedLamp.class);
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
	 * @param hardware
	 *            Link to underlying hardware layer.
	 */
	public DimmedLamp(String name, String description, String ui, int outputValueHardwareIfFull, LogCh channel,
			IDomoticContext ctx) {
		super(name, description, ui, channel, ctx);
		this.factorHwOut = outputValueHardwareIfFull;
		state = States.OFF;
		level = 0;
		prevOnLevel = 100;
	}

	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, LogCh channel,
			IDomoticContext ctx) {
		this(name, description, null, outputValueHardwareIfFull, channel, ctx);
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
	 * @param hardware
	 *            Link to underlying hardware layer.
	 * @deprecated
	 */
	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, int channel,
			IDomoticContext ctx) {
		this(name, description, null, outputValueHardwareIfFull, new LogCh(channel), ctx);
	}

	/**
	 * Initializes dimmed lamp to 50% on.
	 */
	@Override
	public void initializeOutput(RememberedOutput ro) {
		if (ro == null) {
			log.info("Dimmed Lamp initializing to ON, 50% - default behaviour when no previous state known.");
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
	 * @param The
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
	@Override
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
			log.warn("User did toggle, but I'm dimming up or down - should not be possible. Switching to off.");
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
	@Override
	public void on() {
		level = prevOnLevel;
		state = States.ON;
		writeAnalogOutput();
		log.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
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
			log.warn("DimmedLamp on(" + newLevel + ") called, argument not in range [0..100]. Operation is ignored.");
			return;
		}
		level = newLevel;
		state = States.ON;
		writeAnalogOutput();
		log.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
	}

	/**
	 * Switch lamp off.
	 */
	@Override
	public void off() {
		if (state != States.OFF) {
			prevOnLevel = level;
			level = 0;
			state = States.OFF;
			writeAnalogOutput();
			log.info("DimmedLamp '" + getName() + "' set to OFF (remembered level: " + prevOnLevel + "%)");
		}
	}

	/**
	 * Start or stop increasing level of lamp. Typically used to connect to an
	 * up-switch.
	 * <p>
	 * One call <code>up(true)</code> will cause an increase of the lamp level
	 * at each {@link #loop(long, long)}, until <code>up(false)</code> is called
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
				log.warn("Caller desactivates up, but I am OFF. Ignored.");
			} else {
				log.debug("up(true), going from OFF to UP.");
				level = 0;
				lastUpDnLoopTime = -1L;
				state = States.UP;
			}
			break;
		case ON:
			if (!active) {
				log.warn("Caller desactivates up, but I am ON. Ignored.");
			} else {
				lastUpDnLoopTime = -1L;
				state = States.UP;
			}
			break;
		case UP:
			if (active) {
				log.warn("Caller activates up, but I am already UP. Ignored.");
			} else {
				state = States.ON;
				log.info("DimmedLamp '" + getName() + "' dimmed up to level: " + level + '%');
			}
			break;
		case DOWN:
			log.warn("Caller activates/deactivates (active=" + active + ") up, but I am in DOWN. Ignored.");
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
	 * at each {@link #loop(long, long)}, until <code>down(false)</code> is
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
				log.warn("Caller desactivates down, but I am OFF. Ignored");
			} else {
				level = prevOnLevel;
				lastUpDnLoopTime = -1L;
				state = States.DOWN;
			}
			break;
		case ON:
			if (!active) {
				log.warn("Caller desactivates down, but I am ON. Ignored");
			} else {
				lastUpDnLoopTime = -1L;
				state = States.DOWN;
			}
			break;
		case UP:
			log.warn("Caller activates/deactivates (active=" + active + ") down, but I am in UP. Ignored");
			break;
		case DOWN:
			if (active) {
				log.warn("Caller activates down, but I am already DOWN. Ignored");
			} else {
				state = States.ON; // Note: ON even if 0%; light strength is
				// basically the same as 1%, so...
				log.info("DimmedLamp '" + getName() + "' dimmed down to level: " + level + "%, state is ON");
			}
			break;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void onEvent(ActionType action) {
		switch (action) {
		case ON:
			on();
			break;
		case OFF:
			off();
			break;
		case TOGGLE:
			toggle();
			break;
		}
	}

	@Override
	public void loop(long current, long sequence) {
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
				log.debug("Going (event=)" + state.toString() + ", change=" + change + ", current level=" + level
						+ ", current-last=" + (current - lastUpDnLoopTime) + ", timeFullDimMs=" + msTimeFullDim);
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
				log.debug("After change, level =" + level);
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
		getHw().writeAnalogOutput(getChannel(), (int) (level * factorHwOut / 100));
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(getName(), this.getClass().getSimpleName(), getDescription());
		// bi.addParm("on", getState() == States.OFF ? "0" : "1");
		// bi.addParm("level", Integer.toString(getLevel()));
		bi.setOn(getState() != States.OFF);
		bi.setLevel(getLevel());
		return bi;
	}

	@Override
	public void update(String action) {
		try {
			ActionType at = ActionType.valueOf(action.toUpperCase());
			onEvent(at);
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
			log.warn("update(); unknown action=" + action);
		}
	}

	@Override
	public String toString() {
		return "DimmedLamp (" + super.toString() + ") level=" + getLevel() + " state=" + getState().name();
	}

}
