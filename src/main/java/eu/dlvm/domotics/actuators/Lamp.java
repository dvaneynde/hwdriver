package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.UiInfo;

public class Lamp extends Actuator implements IEventListener {

	static Logger logger = LoggerFactory.getLogger(Lamp.class);
	private boolean outval;

	public Lamp(String name, String description, String channel, IDomoticContext ctx) {
		this(name, description, null, channel, ctx);
	}

	public Lamp(String name, String description, String ui, String channel, IDomoticContext ctx) {
		super(name, description, ui, channel, ctx);
		this.outval = false;
	}

	/**
	 * @deprecated
	 */
	public Lamp(String name, String description, int channel, IDomoticContext ctx) {
		this(name, description, Integer.toString(channel), ctx);
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
	public boolean toggle() {
		setOn(!outval);
		return outval;
	}

	public void on() {
		setOn(true);
	}

	public void off() {
		setOn(false);
	}

	private void setOn(boolean outval) {
		this.outval = outval;
		logger.info("Lamp '" + getName() + "' set state to " + (isOn() ? "ON" : "OFF"));
		getHw().writeDigitalOutput(getChannel(), outval);
		notifyListeners(outval ? EventType.ON : EventType.OFF);
	}

	/**
	 * @return true iff. lamp is on
	 */
	public boolean isOn() {
		return outval;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			setOn(true);
			break;
		case OFF:
			setOn(false);
			break;
		case TOGGLE:
			toggle();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}

	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on Lamp '" + getName() + "' got unsupported action '" + action + ".");
	}

	@Override
	public void loop(long currentTime, long sequence) {
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(this);
		//ai.addParm("on", isOn() ? "1" : "0");
		bi.setOn(isOn());
		return bi;
	}

	@Override
	public String toString() {
		return "Lamp (" + super.toString() + ") on=" + isOn();
	}

}
