package eu.dlvm.domotics.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.ChannelFault;
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
public class Domotic implements IDomoticContext {

	private static Logger log = Logger.getLogger(Domotic.class);
	private static Logger MON = Logger.getLogger("MONITOR");

	private static Domotic singleton;
	// protected access for test cases only
	protected IHardwareIO hw = null;
	protected List<Sensor> sensors = new ArrayList<Sensor>(64);
	protected List<Actuator> actuators = new ArrayList<Actuator>(64);
	protected List<Controller> controllers = new ArrayList<Controller>(64);
	protected long loopSequence = -1L;
//	protected boolean ready = false;

	public static synchronized  Domotic singleton() {
		if (singleton == null) {
			singleton = new Domotic();
		}
		return singleton;
	}

	public static synchronized Domotic singleton(IHardwareIO hw) {
		if (singleton == null) {
			singleton = new Domotic();
			singleton.setHw(hw);
		}
		return singleton;
	}

	public static synchronized void resetSingleton() {
		singleton = null;
	}
	
	private Domotic() {
		super();
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
	 * @param prevOuts Map of actuator names and previous outputs. If not used must be empty map (not <code>null</code>).
	 */
	public void initialize(Map<String,RememberedOutput> prevOuts) {
		loopSequence++;
		try {
			hw.initialize();
		} catch (ChannelFault e) {
			log.error("Cannot start Domotic, cannot communicate with driver.");
			throw new RuntimeException("Problem communicating with driver.");
		}
		for (Actuator a : actuators) {
			RememberedOutput ro = prevOuts.get(a.getName());
			a.initializeOutput(ro);
		}
		hw.refreshOutputs();
//		ready = true;
	}

	public void shutdown() {
		hw.stop();
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
//		if (!ready) {
//			throw new RuntimeException("Domotic not initialized.");
//		}
		loopSequence++;
		if (loopSequence %10 == 0)
			MON.info("loopOnce() start, loopSequence="+loopSequence+", currentTime="+currentTime);
		hw.refreshInputs();
		for (Sensor s : sensors) {
			s.loop(currentTime, loopSequence);
		}
		for (Actuator a : actuators) {
			a.loop(currentTime, loopSequence);
		}
		hw.refreshOutputs();
		if (loopSequence %10 == 0)
			MON.info("loopOnce() done, loopSequence="+loopSequence+", currentTime="+currentTime);
	}

	/**
	 * Stops hardware. After this you need to call {@link #initialize()} if you
	 * need to restart.
	 */
	public void stop() {
		hw.stop();
//		ready = false;
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

	// TODO generic
	public void addController(Controller a) {
		for (Controller aa : controllers) {
			if (aa == a) {
				log.warn("Controller already added, ignored: " + a);
				assert (false);
				return;
			}
		}
		controllers.add(a);
		log.info("Added controller " + a.getName());
	}

	public List<Actuator> getActuators() {
		return actuators;
	}
	
	public Actuator findActuator(String name) {
		for (Actuator a: actuators) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public List<Sensor> getSensors() {
		// TODO copy of list?
		return sensors;
	}


//	public boolean isReady() {
//		return ready;
//	}
//
	public long getLoopSequence() {
		return loopSequence;
	}

}
