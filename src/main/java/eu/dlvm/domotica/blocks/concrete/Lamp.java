package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.service.BlockInfo;
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
	public void execute(String op) {
		switch (op) {
		case "on":
			setOn(true);
			break;
		case "off":
			setOn(false);
			break;
		case "toggle":
			toggle();
			break;
		default:
			log.warn("execute() ignored unknown event: " + op);
		}
	}

	@Override
	public void loop(long currentTime, long sequence) {
		checkLoopSequence(sequence);
	}

	@Override
	public String toString() {
		return "Lamp (" + super.toString() + ") on=" + isOn();
	}

	@Override
	public BlockInfo getActuatorInfo() {
		BlockInfo ai = new BlockInfo(this.getName(), this.getClass().getSimpleName(), this.getDescription());
		ai.addParm("on", isOn() ? "1" : "0");
		return ai;
	}

}
