package eu.dlvm.domotics.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.DriverMonitor;
import eu.dlvm.domotics.service_impl.ServiceServer;
import eu.dlvm.domotics.utils.OnceADayWithinPeriod;
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

	public static final int MONITORING_INTERVAL_MS = 5000;
	public static int RESTART_DRIVER_WAITTIME_MS = 30000;

	private static Logger log = LoggerFactory.getLogger(Domotic.class);

	private static Logger MON = LoggerFactory.getLogger("MONITOR");
	private static Domotic singleton;

	private Thread maintThread;
	private DriverMonitor driverMonitor;
	private Process driverProcess;
	private OutputStateSaver saveState;
	private OnceADayWithinPeriod restartOnceADay;

	private AtomicBoolean stopRequested = new AtomicBoolean();
	private AtomicBoolean restartDriverRequested = new AtomicBoolean();

	// protected access for test cases only
	protected IHardwareIO hw = null;
	protected List<Sensor> sensors = new ArrayList<Sensor>(64);
	protected List<Actuator> actuators = new ArrayList<Actuator>(64);
	protected List<Controller> controllers = new ArrayList<Controller>(64);
	protected long loopSequence = -1L;

	private List<IUserInterfaceAPI> uiblocks = new ArrayList<IUserInterfaceAPI>(64);
	
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

	// Forceer singleton
	private Domotic() {
		super();
		saveState = new OutputStateSaver();
		// TODO configurable via xml !
		restartOnceADay = new OnceADayWithinPeriod(23, 00, 23, 10);
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
		log.debug("Added sensor " + s.getName());
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
		if (a instanceof IUserInterfaceAPI)
			addUiCapableBlock((IUserInterfaceAPI)a);
		log.debug("Added actuator " + a.getName());
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
		if (a instanceof IUserInterfaceAPI)
			addUiCapableBlock((IUserInterfaceAPI)a);
		log.debug("Added controller " + a.getName());
	}

	public List<Sensor> getSensors() {
		// TODO copy of list?
		return sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public IUserInterfaceAPI findUiCapable(String name) {
		for (IUserInterfaceAPI ui : uiblocks) {
			if (ui.getName().equals(name))
				return ui;
		}
		return null;
	}

	/**
	 * @return all registered {@link Actuator} and {@link Controller} blocks that implement {@link IUserInterfaceAPI}, or those blocks registered explicitly...
	 */
	public List<IUserInterfaceAPI> getUiCapableBlocks() {
		return uiblocks;
	}
	
	@Override
	public void addUiCapableBlock(IUserInterfaceAPI uiblock0) {
//		Block block0 = (Block)uiblock0;
//		for (IUserInterfaceAPI uiblock:uiblocks) {
//			if (((Block)uiblock).getName().equals(block0.name)) {
//				log.warn("addUiCapableBlock(): incominb block '"+block0.name+"' already registered - ignored.");
//				return;
//			}
//		}
		for (IUserInterfaceAPI uiblock:uiblocks) {
			if (uiblock.getName().equals(uiblock0.getName())) {
				log.warn("addUiCapableBlock(): incoming UiCapable '"+uiblock0.getName()+"' already registered - ignored.");
				return;
			}
		}
		uiblocks.add(uiblock0);
		log.debug("Added UiCapableBlock " + uiblock0.getName());
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

	private void addShutdownHook(Domotic dom) {
		Runtime.getRuntime().addShutdownHook(new Thread("DomoticShutdownHook") {
			@Override
			public void run() {
				log.info("Inside Add Shutdown Hook");
				stopRequested.set(true);
				log.warn("Stop requested - may take up to 5 seconds...");
			}
		});
		log.info("Shutdown hook attached.");
	}

	/**
	 * Domotic will stop running asap and gracefully. No guarantee...
	 */
	public void requestStop() {
		stopRequested.set(true);

		if (maintThread == null) {
			log.error("Calling interruptMainThread(), but mainThread is not set. Ignored.");
		} else {
			maintThread.interrupt();
			log.info("Interrupted main thread, done by thread=" + Thread.currentThread().getName());
		}
		log.info("Request to stop fulfilled. No guarantee...");
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
		stopRequested.set(false);
		boolean fatalError = false;
		while (!stopRequested.get() && !fatalError) {
			if (pathToDriver != null) {
				fatalError = startDriverAndMonitoring(pathToDriver, fatalError);
				if (fatalError)
					break;
			}

			// TODO is initializatie nodig? probleem is dat ik het nu moet doen
			// want anders wordt TCP connectie niet gelegd
			// Rare is wel dat de lampen blijven branden als er geen connectie
			// is met driver - ik dacht dat dan alles zou uitgaan. Maar ook als
			// driver process weg is blijven lampen branden.
			// if (!restartDriverRequested.get()) {
			log.info("Initialize domotic system.");
			initialize(saveState.readRememberedOutputs());
			// }

			log.info("Start Domotic thread 'Oscillator'.");
			Oscillator osc = new Oscillator(this, looptime);
			osc.start();

			log.info("Everything started, now monitoring...");
			long lastLoopSequence = -1;
			while (!stopRequested.get() && !restartDriverRequested.get()) {
				sleep(MONITORING_INTERVAL_MS); // TODO deze sleep moet
												// interrupted ! Of heb ik dat
				// al gedaan?
				saveState.writeRememberedOutputs(getActuators());

				long currentLoopSequence = loopSequence;
				if (currentLoopSequence <= lastLoopSequence) {
					log.error("Domotic does not seem to be looping anymore, last recorded loopsequence=" + lastLoopSequence + ", current=" + currentLoopSequence
							+ ". I'll try to restart driver.");
					break;
				}
				lastLoopSequence = currentLoopSequence;
				if (pathToDriver != null) {
					if (driverMonitor.everythingSeemsWorking()) {
						MON.info("Checked driver sub-process, seems OK.");
					} else {
						log.error("Something is wrong with driver subprocess. I'll try to restart.\n" + driverMonitor.report());
						break;
					}
				}

				// TODO werkt niet, process gaat in TIME_WAIT; so_reuseaddr in
				// hwdriver toegevoegd, maar dan had ik er 2 draaien...
				// restartDriverRequested.set(checkIfDriverRestartTimeHasCome(osc.getLastCurrentTime()));
				// if (restartDriverRequested.get())
				// restartOnceADay.markDoneForToday();
			}
			// shutdown
			stopDriverOscilatorAndMonitor(pathToDriver, osc);
			if (!stopRequested.get() && !fatalError) {
				log.info("Will restart driver in " + RESTART_DRIVER_WAITTIME_MS / 1000 + " seconds...");
				sleep(RESTART_DRIVER_WAITTIME_MS);
			}
		}
		server.stop();
		log.info("Domotica run exited.");
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

	private boolean startDriverAndMonitoring(String pathToDriver, boolean fatalError) {
		log.info("Start HwDriver, and wait for startup message from driver...");
		ProcessBuilder pb = new ProcessBuilder(pathToDriver, "localhost");
		try {
			driverProcess = pb.start();
		} catch (IOException e) {
			log.error("Cannot start driver as subprocess. Abort startup.", e);
			fatalError = true;
			return fatalError;
		}
		driverMonitor = new DriverMonitor(driverProcess, "hwdriver");
		int maxTries = 5000 / 200;
		int trial = 0;
		while ((trial++ < maxTries) && driverMonitor.driverNotReady()) {
			sleep(200);
		}
		if (trial >= maxTries) {
			log.warn("Couldn't see startup message from HwDriver to be started, but I'll assume it started.");
		} else {
			log.info("Driver started in " + (trial - 1) * 200 / 1000.0 + " seconds.");
		}
		return fatalError;
	}

	private void stopDriverOscilatorAndMonitor(String pathToDriver, Oscillator osc) {
		osc.requestStop();
		// TODO 50 vervangen door tick time variable
		sleep(50);
		// Zend STOP naar driver
		hw.stop();
		if (pathToDriver != null) {
			// Zeker zijn dat STOP verwerkt is
			sleep(500);
			if (driverMonitor.getProcessWatch().isRunning()) {
				log.warn("STOP command to driver did not work, stop forcibly...");
				driverProcess.destroy();
				sleep(500);
				if (driverMonitor.getProcessWatch().isRunning()) {
					log.error("Could not destroy driver process, pid=" + driverMonitor.getProcessWatch().getPid() + ". Ignored, you'll see what happens.");
					// TODO stop domotic?
				}
			} else {
				log.info("Driver stopped, exit code=" + driverMonitor.getProcessWatch().getExitcode() + ". Now Stopping driver monitor.");
			}
			driverMonitor.terminate();
		}
		driverMonitor = null;
		driverProcess = null;
		log.info("Stopped hardware, oscillator and monitor.");
	}

	// TODO Werkt niet wegens TIME_WAIT, server zou even moeten wachten of select() doen - volgend leven
	@SuppressWarnings("unused")
	private boolean checkIfDriverRestartTimeHasCome(long lastCurrentTime) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
		cal.setTimeInMillis(lastCurrentTime);
		boolean restart = false;
		if (restartOnceADay.canCheckForToday(lastCurrentTime)) {
			log.debug("checkIfDriverRestartTimeHasCome() cancheck=true; now see if already done.");
			if (!restartOnceADay.checkDoneForToday(lastCurrentTime)) {
				log.debug("checkIfDriverRestartTimeHasCome() cancheck=true, checkDone=false, so RESTART !");
				restart = true;
			} else {
				log.debug("checkIfDriverRestartTimeHasCome() cancheck and already done, so no restart.");
			}
		}
		if (restart)
			log.info("The time has come to restart the driver. Time=" + cal.get(Calendar.HOUR) + ':' + cal.get(Calendar.MINUTE) + '.');
		return restart;
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.debug("Got interrupted, in Thread.sleep(). ", e);
		}
	}

}
