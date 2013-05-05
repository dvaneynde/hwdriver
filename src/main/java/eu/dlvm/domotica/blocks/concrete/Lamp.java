package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.iohardware.LogCh;

public class Lamp extends Actuator {

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
	public Lamp(String name, String description, LogCh channel, IDomoContext ctx) {
		super(name, description, channel, ctx);
		this.outval = false;
	}

	public Lamp(String name, String description, int channel, IDomoContext ctx) {
		this(name, description, new LogCh(channel), ctx);
	}

	@Override
	public void initializeOutput() {
		hw().writeDigitalOutput(getChannel(), outval);
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
	 * Sets relay to On or Off
	 * 
	 * @param outval
	 *            true for On, false for Off
	 */
	public void setOn(boolean outval) {
		this.outval = outval;
		log.info("Lamp '" + getName() + "' set state to "
				+ (isOn() ? "ON" : "OFF"));
		hw().writeDigitalOutput(getChannel(), outval);
	}

	/**
	 * @return true iff. relay is on
	 */
	public boolean isOn() {
		return outval;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		// Nothing to do.
		// log.warn("Called loop(), should not be the case.");
	}

	@Override
	public String toString() {
		return "Lamp (" + super.toString() + ") on=" + isOn();
	}

}
