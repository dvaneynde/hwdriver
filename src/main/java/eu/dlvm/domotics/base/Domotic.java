package eu.dlvm.domotics.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.DriverMonitor;
import eu.dlvm.domotics.service.impl.ServiceServer;
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
	private Thread maintThread;
	private File outputStatesFile;

	// protected access for test cases only
	protected IHardwareIO hw = null;
	protected List<Sensor> sensors = new ArrayList<Sensor>(64);
	protected List<Actuator> actuators = new ArrayList<Actuator>(64);
	protected List<Controller> controllers = new ArrayList<Controller>(64);
	protected long loopSequence = -1L;


	public AtomicBoolean stopRequested = new AtomicBoolean();

	public static synchronized Domotic singleton() {
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

//	private Domotic() {
//		super();
//	}
//
	/**
	 * Initializes {@link IHardwareIO} and Domotic {@link Actuator} blocks.
	 * Initializing the Actuators implies that the hardware outputs are set in
	 * line with the Actuator output state.
	 * <p>
	 * Note that this happens before - and does not need -
	 * {@link #loopOnce(long)}.
	 * <p>
	 * Must be called before {@link #loopOnce(long)} or {@link #stop}.
	 * 
	 * @param prevOuts
	 *            Map of actuator names and previous outputs. If not used must
	 *            be empty map (not <code>null</code>).
	 */
	public void initialize(Map<String, RememberedOutput> prevOuts) {
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
	}

	/**
	 * TODO move naar Oscillator, en hernoem die naar DomoticBlocksLoop
	 * 
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
	public synchronized void loopOnce(long currentTime) {
		loopSequence++;
		if (loopSequence % 100 == 0)
			MON.info("loopOnce() start, loopSequence=" + loopSequence + ", currentTime=" + currentTime);
		hw.refreshInputs();
		for (Sensor s : sensors) {
			s.loop(currentTime, loopSequence);
		}
		for (Controller c : controllers) {
			c.loop(currentTime, loopSequence);
		}
		for (Actuator a : actuators) {
			a.loop(currentTime, loopSequence);
		}
		hw.refreshOutputs();
		if (loopSequence % 10 == 0)
			MON.info("loopOnce() done, loopSequence=" + loopSequence + ", currentTime=" + currentTime);
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
		for (IUserInterfaceAPI aa : actuators) {
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

	public IUserInterfaceAPI findActuator(String name) {
		for (Actuator a : actuators) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public List<Sensor> getSensors() {
		// TODO copy of list?
		return sensors;
	}

	public long getLoopSequence() {
		return loopSequence;
	}

	class ShutdownHook implements Runnable {
		private Domotic dom;

		ShutdownHook(Domotic domotic) {
			dom = domotic;
		}

		@Override
		public void run() {
			log.info("Inside Add Shutdown Hook");
			dom.stopRequested.set(true);
			log.warn("Stop requested - may take up to 5 seconds...");
		}
	}

	private void addShutdownHook(Domotic dom) {
		Thread shutdownThread = new Thread(new ShutdownHook(dom), "DomoticShutdownHook");
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		log.info("Shutdown hook attached.");
	}

	// TODO ook naar DomoticBlockLoop ?
	class LoopExecutor implements Runnable {
		private Oscillator osc;

		LoopExecutor(Oscillator oscillator) {
			osc = oscillator;
		}

		@Override
		public void run() {
			try {
				log.info("Oscillator oscillates...");
				osc.go();
				if (osc.stopRequested())
					log.info("Oscillator stops since so requested.");
				else
					log.error("Oh oh... oscillator has stopped for no apparent reason. Should not happen. Nothing done for now.");
			} catch (Exception e) {
				log.error("Oh oh... oscillator has stopped. Nothing done further, should restart or something...", e);
			}
		}
	}

	public void interruptMainThread() {
		if (maintThread == null) {
			log.error("Calling interruptMainThread(), but mainThread is not set. Ignored.");
		} else {
			maintThread.interrupt();
			log.info("Interrupted main thread, done by thread="+Thread.currentThread().getName());
		}
	}

	/**
	 * Runs it.
	 * 
	 * @param osc
	 *            Drives the Domotic system, and references it.
	 * @param pathToDriver
	 *            If non-null, attempts to start the HwDriver executable at that
	 *            path. Note that this driver must be on the same host, since
	 *            'localhost' is passed to it as an argument. Otherwise that
	 *            driver should be started separately, after this one shows
	 *            "START" in the log.
	 */
	public void runDomotic(int looptime, String pathToDriver) {
		addShutdownHook(this);
		this.maintThread = Thread.currentThread();

		ServiceServer server = new ServiceServer();
		server.start();

		// TODO see
		// http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
		Oscillator osc = new Oscillator(this, looptime);
		LoopExecutor domoticRunner = new LoopExecutor(osc);
		stopRequested.set(false);
		boolean fatalError = false;
		while (!stopRequested.get() && !fatalError) {
			DriverMonitor monitor = null;
			Process driverProcess = null;
			if (pathToDriver != null) {
				log.info("Start HwDriver, and wait for startup message from driver...");
				ProcessBuilder pb = new ProcessBuilder(pathToDriver, "localhost");
				try {
					driverProcess = pb.start();
				} catch (IOException e) {
					log.error("Cannot start driver as subprocess. Abort startup.", e);
					fatalError = true;
					break;
				}
				monitor = new DriverMonitor(driverProcess, "hwdriver");
				int maxTries = 5000 / 200;
				int trial = 0;
				while ((trial++ < maxTries) && monitor.driverNotReady())
					sleep(200);
				if (trial >= maxTries)
					log.warn("Couldn't see startup message from HwDriver to be started, but I'll assume it started.");
				else
					log.info("Driver started in " + (trial - 1) * 200 / 1000.0 + " seconds.");
			}
			log.info("Initialize domotic system.");
			Map<String, RememberedOutput> ros = readRememberedOutputs();
			initialize(ros);
			log.info("Start Domotic thread 'Domotic Blocks Execution'.");
			Thread domoticThread = new Thread(domoticRunner, "DomoticOscillator.");
			domoticThread.start();

			log.info("Everything started, now monitoring...");
			long lastLoopSequence = -1;
			while (!stopRequested.get()) {
				// TODO deze sleep moet interrupted !
				sleep(5000);
				long currentLoopSequence = getLoopSequence();
				if (currentLoopSequence <= lastLoopSequence)
					log.error("Domotic does not seem to be looping anymore, last recorded loopsequence=" + lastLoopSequence + ", current=" + currentLoopSequence);
				lastLoopSequence = currentLoopSequence;
				if (pathToDriver != null) {
					if (monitor.everythingSeemsWorking()) {
						MON.info("Checked driver sub-process, seems OK.");
					} else {
						log.error("Something is wrong with driver subprocess. I'll try to restart.\n" + monitor.report());
						break;
					}
				}
				writeRememberedOutputs(getActuators());
			}
			// shutdown
			if (pathToDriver != null) {
				log.info("Stopping driver and monitor.");
				driverProcess.destroy();
				monitor.terminate();
			}
			// shutdown();
			hw.stop();
			osc.stop();
			server.stop();
			// domoticThread.stop();
			log.info("Stopped hardware, oscillator and server ...");
			if (!stopRequested.get() && !fatalError) {
				log.info("Will restart driver in 3 seconds...");
				sleep(3000);
			}
		}
		log.info("Domotica exited.");
	}

	private Map<String, RememberedOutput> readRememberedOutputs() {
		Map<String, RememberedOutput> ros = new HashMap<String, RememberedOutput>();
		File f = getOutputStatesFile();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				RememberedOutput ro = RememberedOutput.parse(line);
				if (ro != null) {
					ros.put(ro.getBlockName(), ro);
				}
			}
			log.info("Read previous ouptut states, number of entries is " + ros.size());
		} catch (FileNotFoundException e) {
			log.info("No remembered outputs file found, will initialize with defaults. File should be: " + f.getName());
		} catch (IOException e) {
			log.error("Error reading " + f.getName() + ", will not use remembered outputs.", e);
		}
		return ros;
	}

	private boolean alreadyReportedFileNotFound = false;

	private void writeRememberedOutputs(List<Actuator> as) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputStatesFile()))) {
			for (Actuator a : as) {
				RememberedOutput ro = a.dumpOutput();
				if (ro != null) {
					bw.write(ro.dump() + '\n');
				}
			}
			log.debug("Wrote last output state to " + getOutputStatesFile().getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (!alreadyReportedFileNotFound)
				log.warn("Cannot write remembered output, file not found: " + getOutputStatesFile());
			alreadyReportedFileNotFound = true;
		} catch (IOException e) {
			log.error("Failed writing to " + getOutputStatesFile().getName() + ". Functionality might not work.", e);
		}
	}

	private File getOutputStatesFile() {
		if (outputStatesFile == null) {
			outputStatesFile = new File("/var/local", "DomoticOutputStates.txt");
			log.info("Safeguard last output states to file " + outputStatesFile.getAbsolutePath());
		}
		return outputStatesFile;
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.debug("Got interrupted, in Thread.sleep(). ", e);
		}
	}

}
