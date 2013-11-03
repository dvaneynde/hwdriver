package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.iohardware.LogCh;



/**
 * Actuators actuate output.
 * <p>
 * They have at least one output channel. They control their own state, control
 * the hardware, have a fairly high-level input to change their state.
 * <p>
 * Some rules:
 * <ol>
 * <li>Only Actuators change outputs.</li>
 * <li>Only Actuators should use the Hardware layers output operations. These
 * must only be called within an {@link #loop(long, long)} call.</li>
 * <li></li>
 * </ol>
 * <p>
 * These rules avoid difficult to find bugs (from experience, but don't remember
 * exactly) and simplify things.
 * 
 * @author Dirk Vaneynde
 */
public abstract class Actuator extends BlockWithHardwareAccess {

	static Logger log = Logger.getLogger(Lamp.class);

	private LogCh channel;
	private long previousLoopSequence = -1L;

	/**
	 * Create Actuator Block, i.e. a Block that abstracts output devices like
	 * Lamps (1 relay), Vents (1 relay with timers), Screens (2 relays) etc.
	 * 
	 * @param channel
	 *            Output channel in Hardware that corresponds to this Actuator.
	 */
	public Actuator(String name, String description, LogCh channel, IHardwareAccess ctx) {
		super(name, description, ctx);
		this.channel = channel;
		ctx.addActuator(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on.
	 */
	public LogCh getChannel() {
		return channel;
	}

	@Override
	public String toString() {
		return "Actuator name='" + name + "', ch=" + getChannel() + ", description='" + description + "'";
	}

	/**
	 * After hardware is initialized, this function may set output values, to be
	 * picked up in the next {@link #loop(long, long)}.
	 */
	public abstract void initializeOutput();

	/**
	 * Called regularly by {@link Domotic}, so that concrete actuators can check
	 * timeouts etc.
	 * <p>
	 * To be enabled this Actuator must first have been registered with
	 * {@link Domotic#addActuator(Actuator)}.
	 * @param currentTime
	 *            Timestamp at which this loop is called. The same for each
	 *            loop.
	 * @param sequence
	 *            A number that increments with each loop. Useful to detect
	 *            being called twice - which is forbidden.
	 * TODO move to IHardwareAccess
	 */
	public abstract void loop(long currentTime, long sequence);
	
	public abstract BlockInfo getActuatorInfo();
	
	/**
	 * To be called from {{@link #loop(long, long)} implementations, to stop program if a loop is looped (should be graph).
	 * @param currentLoopSequence
	 * TODO go to IHardwareAcess, also sensors can use it (must use it), perhaps use delegate?
	 */
	protected void checkLoopSequence(long currentLoopSequence) {
		if (currentLoopSequence <= previousLoopSequence) {
			log.error("Current loop sequence equal to, or before last recorded. Abort program. current="+currentLoopSequence+", previous="+previousLoopSequence);
			throw new RuntimeException("Current loop sequence equal to, or before last recorded. Abort program.");
		}
		previousLoopSequence = currentLoopSequence;
	}
}
