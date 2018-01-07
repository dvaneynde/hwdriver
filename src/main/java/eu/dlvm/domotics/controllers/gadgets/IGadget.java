package eu.dlvm.domotics.controllers.gadgets;

/**
 * Base interface for gadgets, which do things with Actuators, like blinking or
 * change as a sine wave.
 * 
 * @author dirk
 */
public interface IGadget {

	/**
	 * Called together with loop(), but gadget specific.
	 * 
	 * @param time
	 *            Time in milliseconds since start of this gadget's gadgetset.
	 *            So a relative time, relative to gadget's set start.
	 * @param state
	 */
	public void loop2(long time, GadgetState state);

	/**
	 * Called before gadget starts working, so before
	 * {@link #loop2(long, GadgetState)}.
	 * 
	 * @param time
	 */
	public void onBefore(long time);

	/**
	 * Called after gadget stopped working, so after {@link #loop2(long, GadgetState)}.
	 * 
	 * @param time
	 */
	public void onDone(long time);

}
