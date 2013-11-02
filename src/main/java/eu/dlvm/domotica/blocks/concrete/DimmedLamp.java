package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.IHardwareAccess;
import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.iohardware.LogCh;

/**
 * Dimmed Lamp.
 * <p>
 * When going up from level 0, at the first loop() output is still 0.
 * 
 * @author dirk
 */
public class DimmedLamp extends Actuator implements IOnOffToggleListener {
	/**
	 * Time in ms to dim from off to fully on.
	 */
	public static final int DEFAULT_FULL_DIMTIME_MS = 5000; // milliseconds

	static Logger log = Logger.getLogger(DimmedLamp.class);
	private int level;
	private int prevOnLevel;
	private int factorHwOut;
	private int msTimeFullDim = DEFAULT_FULL_DIMTIME_MS;
	private long lastUpDnLoopTime;
	private States state;

	// private boolean newLevelToWrite;

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
	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, LogCh channel, IHardwareAccess ctx) {
		super(name, description, channel, ctx);
		this.factorHwOut = outputValueHardwareIfFull;
		state = States.OFF;
		level = 0;
		prevOnLevel = 100;
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
	 */
	public DimmedLamp(String name, String description, int outputValueHardwareIfFull, int channel, IHardwareAccess ctx) {
		this(name, description, outputValueHardwareIfFull, new LogCh(channel), ctx);
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
	 * Initializes dimmed lamp to 50% on.
	 */
	@Override
	public void initializeOutput() {
		log.info("Dimmed Lamp initializing to ON, 50%.");
		on(50);
	}

	/**
	 * Current level of lamp, between 0 and 100. Note that a lamp can be in
	 * state ON (see {@link #getState()}) and at the same time its level is 0.
	 */
	public int getLevel() {
		return level;
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
	public void toggle() {
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
	}

	/**
	 * Switch lamp on, to previous on level (could be 0, so if lamp was off user
	 * won't see a difference; but the same is true if the lamp was before on at
	 * 1%... so we don't bother about this).
	 */
	public void on() {
		level = prevOnLevel;
		// TODO waarom mag ik hier niet ineens de output aansturen? er was een
		// reden, maar is die nog geldig...
		// newLevelToWrite = true;
		state = States.ON;
		writeAnalogOutput();
		log.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
	}

	/**
	 * Switch lamp on if necessary, and set level to given percentage (range
	 * [0..100]).
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
		// newLevelToWrite = true;
		// TODO waarom mag ik hier niet ineens de output aansturen? er was een
		// reden, maar is die nog geldig...
		state = States.ON;
		writeAnalogOutput();
		log.info("DimmedLamp '" + getName() + "' set to ON: " + level + '%');
	}

	/**
	 * Switch lamp off, i.e. {@link DimmedLamp#getLevel()} returns 0.
	 */
	public void off() {
		if (state != States.OFF) {
			prevOnLevel = level;
			level = 0;
			// newLevelToWrite = true;
			// TODO waarom mag ik hier niet ineens de output aansturen? er was
			// een reden, maar is die nog geldig...
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
	public void onEvent(Block source, ActionType action) {
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
		checkLoopSequence(sequence);
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
				double change = ((double) (current - lastUpDnLoopTime)) * 100 / (double) msTimeFullDim;
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

	@Override
	public BlockInfo getActuatorInfo() {
		BlockInfo bi = new BlockInfo(getName(), this.getClass().getSimpleName(), getDescription());
		bi.addParm("state", getState().toString().toLowerCase());
		bi.addParm("level", Integer.toString(getLevel()));
		return bi;
	}

	@Override
	public String toString() {
		return "DimmedLamp (" + super.toString() + ") level=" + getLevel() + " state=" + getState().name();
	}

	private void writeAnalogOutput() {
		getHw().writeAnalogOutput(getChannel(), (int) (level * factorHwOut / 100));
	}

}
