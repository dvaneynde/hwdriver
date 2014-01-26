package eu.dlvm.domotics.base;

public interface IDomoticLoop {

	/**
	 * Called regularly by {@link Domotic}, so that concrete actuators can check
	 * timeouts etc.
	 * <p>
	 * To be enabled this Actuator must first have been registered with
	 * {@link Domotic#addActuator(Actuator)}.
	 * 
	 * @param currentTime
	 *            Timestamp at which this loop is called. The same for each
	 *            loop.
	 * @param sequence
	 *            A number that increments with each loop. Useful to detect
	 *            being called twice - which is forbidden.
		 */
	public abstract void loop(long currentTime, long sequence);


}
