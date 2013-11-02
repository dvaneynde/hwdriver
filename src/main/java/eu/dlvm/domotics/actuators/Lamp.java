package eu.dlvm.domotics.actuators;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.mappers.IOnOffToggleListener;
import eu.dlvm.domotics.mappers.IOnOffToggleListener.ActionType;
import eu.dlvm.iohardware.LogCh;

public class Lamp extends Actuator implements IOnOffToggleListener {

	static Logger log = Logger.getLogger(Lamp.class);
	private boolean outval;

	/**
	 * Constructor. Output is set to false, and transferred to hardware.
	 * <p>
	 * See {@link Actuator#Actuator(String, String, LogCh)} for most parameters.
	 * 
	 * @param hardware
	 *            Hardware to set output value(s).
	 */
	public Lamp(String name, String description, LogCh channel, IHardwareAccess ctx) {
		super(name, description, channel, ctx);
		this.outval = false;
	}

	public Lamp(String name, String description, int channel, IHardwareAccess ctx) {
		this(name, description, new LogCh(channel), ctx);
	}

	@Override
	public void initializeOutput() {
		getHw().writeDigitalOutput(getChannel(), outval);
	}

	/**
	 * Toggles the output.
	 * 
	 * @return New output state.
	 */
	public boolean toggle() {
		setOn(!outval);
		return outval;
	}

	/**
	 * Sets lamp to On or Off
	 * 
	 * @param outval
	 *            true for On, false for Off
	 */
	public void setOn(boolean outval) {
		this.outval = outval;
		log.info("Lamp '" + getName() + "' set state to " + (isOn() ? "ON" : "OFF"));
		getHw().writeDigitalOutput(getChannel(), outval);
	}

	/**
	 * @return true iff. lamp is on
	 */
	public boolean isOn() {
		return outval;
	}

	@Override
	public void onEvent(Block source, ActionType action) {
		switch (action) {
		case ON:
			setOn(true);
			break;
		case OFF:
			setOn(false);
			break;
		case TOGGLE:
			toggle();
			break;
		}
	}


	@Override
	public void loop(long currentTime, long sequence) {
		checkLoopSequence(sequence);
	}

	@Override
	public BlockInfo getActuatorInfo() {
		BlockInfo ai = new BlockInfo(this.getName(), this.getClass().getSimpleName(), this.getDescription());
		ai.addParm("on", isOn() ? "1" : "0");
		return ai;
	}

	@Override
	public String toString() {
		return "Lamp (" + super.toString() + ") on=" + isOn();
	}

}
