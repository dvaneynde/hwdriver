package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

public abstract class BlockWithLoop extends Block {

	private static Logger log = Logger.getLogger(BlockWithLoop.class);

	// FIXME naar domotic, via IDomoticCtx, en thread safe !
	private long previousLoopSequence = -1L;

	public BlockWithLoop(String name, String description) {
		super(name, description);
	}

	public synchronized void loopWithCheck(long currentTime, long sequence) {
		checkLoopSequence(sequence);
		loop(currentTime, sequence);
	}
	
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

	/**
	 * To be called from {{@link #loop(long, long)} implementations, to stop
	 * program if a loop is looped (should be graph without cycles).
	 * 
	 * @param currentLoopSequence
	 *            TODO go to IHardwareAcess, also sensors can use it (must use
	 *            it), perhaps use delegate?
	 */
	private synchronized void checkLoopSequence(long currentLoopSequence) {
		if (currentLoopSequence <= previousLoopSequence) {
			log.error("Current loop sequence equal to, or before last recorded. Abort program. current=" + currentLoopSequence + ", previous=" + previousLoopSequence);
			throw new RuntimeException("Current loop sequence equal to, or before last recorded. Abort program.");
		}
		previousLoopSequence = currentLoopSequence;
	}

}
