package eu.dlvm.domotica.blocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.IHardwareIO;

/**
 * Makes the {@link Block} do something, that have to be registered here, and
 * then will be triggered multiple times a second via {@link #loopOnce()}.
 * <p>
 * You need something else that calls {@link #loopOnce()} regularly.
 * 
 * @author dirk vaneynde
 * 
 */
public class Domotic implements IDomoContext {

	static Logger log = Logger.getLogger(Domotic.class);

	// protected access for test cases only
	protected IHardwareIO hw = null;
	protected List<Sensor> sensors = new ArrayList<Sensor>(64);
	protected List<Actuator> actuators = new ArrayList<Actuator>(32);
	protected long loopSequence = -1L;
	protected boolean ready = false;

	public Domotic() {
		super();
	}

	public Domotic(IHardwareIO hw) {
		super();
		setHw(hw);
	}

	/**
	 * Initializes {@link IHardwareIO} and Domotic {@link Actuator} blocks.
	 * Initializing the Actuators implies that the hardware outputs are set in
	 * line with the Actuator output state.
	 * <p>
	 * Note that this happens before - and does not need -
	 * {@link #loopOnce(long)}.
	 * <p>
	 * Must be called before {@link #loopOnce(long)} or {@link #stop}.
	 */
	public void initialize() {
		loopSequence++;
		hw.initialize();
		for (Actuator a : actuators) {
			a.initializeOutput();
		}
		hw.refreshOutputs();
		ready = true;
	}

	/**
	 * Some external clock must regularly call this method, to let the Blocks
	 * 'work'.
	 * <p>
	 * This is what happens:
	 * <ol>
	 * <li>{@link IHardwareIO#refreshInputs()} is called, so that hardware layer
	 * inputs are refreshed.</li>
	 * <li>All registered Sensors have their {@link Sensor#loop()} run to read
	 * input and/or check timeouts etc. This typically triggers some Boards that
	 * in turn trigger some Actuators.</li>
	 * <li>Then any registered Actuators have their {@link Actuator#loop()}
	 * executed, so they can check timeouts etc.</li>
	 * <li>{@link IHardwareIO#refreshOutputs()} is called, so that hardware
	 * layer outputs are updated.</li>
	 * </ol>
	 * 
	 * @param currentTime
	 *            Current time at loopOnce invocation.
	 */
	public void loopOnce(long currentTime) {
		if (!ready) {
			throw new RuntimeException("Domotic not initialized.");
		}
		loopSequence++;
		hw.refreshInputs();
		for (Sensor s : sensors) {
			s.loop(currentTime, loopSequence);
		}
		for (Actuator a : actuators) {
			a.loop(currentTime, loopSequence);
		}
		hw.refreshOutputs();
	}

	/**
	 * Stops hardware. After this you need to call {@link #initialize()} if you
	 * need to restart.
	 */
	public void stop() {
		hw.stop();
		ready = false;
	}

	public void setHw(IHardwareIO hw) {
		this.hw = hw;
	}

	@Override
	public IHardwareIO getHw() {
		return hw;
	}

	/**
	 * Add Sensor to loop set (see {@link #loopOnce()}.
	 * 
	 * @param s
	 *            Added, if not already present. Each Sensor can be present no
	 *            more than once.
	 */
	public void addSensor(Sensor s) {
		for (Sensor ss : sensors) {
			if (ss == s) {
				log.warn("Sensor already added, ignored: " + s);
				assert (false);
				return;
			}
		}
		sensors.add(s);
		log.info("Added sensor " + s.getName());
	}

	/**
	 * Add Actuator to loop set (see {@link #loopOnce()}.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	public void addActuator(Actuator a) {
		for (Actuator aa : actuators) {
			if (aa == a) {
				log.warn("Actuator already added, ignored: " + a);
				assert (false);
				return;
			}
		}
		actuators.add(a);
		log.info("Added actuator " + a.getName());
	}
}
