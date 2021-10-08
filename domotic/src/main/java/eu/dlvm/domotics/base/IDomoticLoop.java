package eu.dlvm.domotics.base;

public interface IDomoticLoop {

	/**
	 * Called regularly from {@link Domotic} so that concrete implementing blocks can do their thing. See {@link Domotic#loopOnce(long)}.
	 * 
	 * @param currentTime
	 *            Timestamp at which this loop is called. The same for each
	 *            loop.
	 */
	public abstract void loop(long currentTime);


}
