package eu.dlvm.domotics.controllers.newyear;

public interface INewYearGadget {

	/**
	 * Basic interface for gadgets, i.e. they do things with Actuators, like
	 * blinking or change in a sine way.
	 * 
	 * @param time
	 *            Time in milliseconds since start of this gadget's gadgetset.
	 *            So a relative time, relative to gadget's set start.
	 * @param state
	 */
	public void loop2(long time, GSstate state);

}
