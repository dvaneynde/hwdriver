package eu.dlvm.domotics.actuators;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.service.UiInfo;
import eu.dlvm.iohardware.LogCh;

public class Lamp extends Actuator implements IOnOffToggleCapable {

	static Logger log = LoggerFactory.getLogger(Lamp.class);
	private boolean outval;

	/**
	 * Constructor. Output is set to false, and transferred to hardware.
	 * <p>
	 * See {@link Actuator#Actuator(String, String, LogCh)} for most parameters.
	 * 
	 * @param hardware
	 *            Hardware to set output value(s).
	 */
	public Lamp(String name, String description, LogCh channel, IDomoticContext ctx) {
		super(name, description, null, channel, ctx);
		this.outval = false;
	}

	public Lamp(String name, String description, String ui, LogCh channel, IDomoticContext ctx) {
		super(name, description, ui, channel, ctx);
		this.outval = false;
	}

	/**
	 * @deprecated
	 */
	public Lamp(String name, String description, int channel, IDomoticContext ctx) {
		this(name, description, new LogCh(channel), ctx);
	}

	@Override
	public void initializeOutput(RememberedOutput ro) {
		if (ro != null)
			setOn(ro.getVals()[0] == 1);
		getHw().writeDigitalOutput(getChannel(), outval);
	}

	@Override
	public RememberedOutput dumpOutput() {
		return new RememberedOutput(getName(), new int[] { isOn() ? 1 : 0 });
	}

	/**
	 * Toggles the output.
	 * 
	 * @return New output state.
	 */
	@Override
	public boolean toggle() {
		setOn(!outval);
		return outval;
	}

	@Override
	public void on() {
		setOn(true);
	}

	@Override
	public void off() {
		setOn(false);
	}

	/**
	 * Sets lamp to On or Off
	 * 
	 * @deprecate Use on() or off() or
	 *            {@link IOnOffToggleCapable#onEvent(Block, eu.dlvm.domotics.connectors.IOnOffToggleCapable.ActionType)}
	 *            .
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
	public void onEvent(ActionType action) {
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
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(this.getName(), this.getClass().getSimpleName(), this.getDescription());
		//ai.addParm("on", isOn() ? "1" : "0");
		bi.setOn(isOn());
		return bi;
	}

	@Override
	public void update(String action) {
		try {
			ActionType at = ActionType.valueOf(action.toUpperCase());
			onEvent(at);
		} catch (IllegalArgumentException e) {
			log.warn("update(), ignored unknown action: " + action);
		}
	}

	@Override
	public String toString() {
		return "Lamp (" + super.toString() + ") on=" + isOn();
	}

}
