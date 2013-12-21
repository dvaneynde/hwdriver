package eu.dlvm.domotics.actuators.newyear;

public interface INewYearGadget {

	/**
	 * @param time
	 *            Time in milliseconds since start of this gadget's gadgetset.
	 *            So a relative time, relative to gadget's set start.
	 */
	public void loop2(long time, GSstate state);

}
