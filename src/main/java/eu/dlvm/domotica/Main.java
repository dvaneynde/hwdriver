package eu.dlvm.domotica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CancellationException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.Oscillator;
import eu.dlvm.domotica.factories.XmlDomoticConfigurator;
import eu.dlvm.domotica.service.ServiceServer;
import eu.dlvm.iohardware.HwConsole;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverChannelSimulator;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;
import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel;

/**
 * Domotic system main entry point.
 * 
 * @author Dirk Vaneynde
 * 
 */
public class Main {

	static Logger log = Logger.getLogger(Main.class);
	static Logger logDriver = Logger.getLogger("DRIVER");
	private String pid;

	public IHardwareIO setupHardware(String cfgFile, String host, int port) {
		XmlHwConfigurator xcf = new XmlHwConfigurator();
		xcf.setCfgFilepath(cfgFile);
		IHwDriverChannel hdc = new HwDriverTcpChannel(host, port);
		HardwareIO hw = new HardwareIO(xcf, hdc);
		return hw;
	}

	public IHardwareIO setupSimulatedHardware(String cfgFile) {
		XmlHwConfigurator xcf = new XmlHwConfigurator();
		xcf.setCfgFilepath(cfgFile);
		IHwDriverChannel hdc = new HwDriverChannelSimulator();
		HardwareIO hw = new HardwareIO(xcf, hdc);
		return hw;
	}

	public Domotic setupBlocksConfig(String cfgFilename, IHardwareIO hw) {
		try {
			Domotic d = Domotic.singleton();
			XmlDomoticConfigurator cf = new XmlDomoticConfigurator();
			cf.setCfgFilepath(cfgFilename);
			cf.configure(d);
			d.setHw(hw);
			return d;
		} catch (Exception e) {
			log.error("Cannot configure system, abort. Reason:"
					+ e.getMessage());
			throw new RuntimeException("Abort. Cannot configure system.");
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
	public void runDomotic(final Domotic dom, final Oscillator osc,
			String pathToDriver) {
		if (pathToDriver == null) {
			log.info("Initializing domotic.");
			dom.initialize();
			log.info("Domotic starts looping now, in main thread.");
			osc.go();
		} else {
			// see
			// http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
			Runnable r = new Runnable() {
				@Override
				public void run() {
					log.info("Domotic starts looping now, in separate thread. Logger DRIVER gives more information. Main thread watches driver.");
					osc.go();
				}
			};
			// TODO driver in aparte thread, main loop doet osc.go() en kijkt of
			// alles nog draait - driver en domotic; zoniet herstart of exit;
			// TODO dan kan driver start ook herbruikt voor HwConsole
			Thread t = new Thread(r, "Domotic Blocks Execution.");
			try {
				log.info("Start HwDriver, and wait 5 seconds...");
				ProcessBuilder pb = new ProcessBuilder(pathToDriver,
						"localhost");
				Process process = pb.start();
				Thread.sleep(5000);
				log.info("Initialize domotic and its hardware.");
				dom.initialize();
				log.info("Start Domotic looping in separate thread 'Domotic Blocks Execution'.");
				t.start(); // TODO wacht op klaar lijn in driver log hieronder,
							// dan pas domotic starten
				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					logDriver.info(line);
				}
			} catch (IOException e) {
				log.fatal("Problem starting or running HwDriver program '"
						+ pathToDriver + "'.", e);
			} catch (InterruptedException e) {
				log.fatal("Problem starting or running HwDriver program '"
						+ pathToDriver + "'.", e);
			} finally {
				stopThread(t);
			}
		}
		log.info("Domotica stopped looping.");
	}

	@SuppressWarnings("deprecation")
	private void stopThread(Thread t) {
		t.stop(new CancellationException(
				"Stop requested, due to error starting HwDriver as subprocess."));
	}

	public String getPid() {
		if (pid == null) {
			String fullpid = ManagementFactory.getRuntimeMXBean().getName();
			pid = fullpid.substring(0, fullpid.indexOf('@'));
		}
		return pid;
	}

	public void storePid() {
		File f = new File("./domotic.pid");
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			fw.write(getPid());
		} catch (FileNotFoundException e) {
			log.fatal("Cannot start, cannot write pid file.", e);
			System.exit(2);
		} catch (IOException e) {
			log.fatal("Cannot start, cannot write pid file.", e);
			System.exit(2);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e1) {
				}
			}
		}
		log.info("domotic pid=" + pid + ", written to domotic.pid file.");
	}

	public void runHwConsole(final String cfgFilename, final String hostname,
			final int port, String pathToDriver) {
		if (pathToDriver == null) {
			HwConsole hc = new HwConsole(cfgFilename, hostname, port);
			hc.processCommands();
		} else {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					HwConsole hc = new HwConsole(cfgFilename, hostname, port);
					hc.processCommands();
				}
			};
			Thread t = new Thread(r, "HwConsole");
			try {
				log.info("Start HwDriver, wait for 5 seconds...");
				ProcessBuilder pb = new ProcessBuilder(pathToDriver,
						"localhost");
				Process process = pb.start();
				Thread.sleep(5000);
				t.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					logDriver.info(line);
				}
			} catch (IOException e) {
				log.fatal("Problem starting or running HwDriver program '"
						+ pathToDriver + "'.", e);
			} catch (InterruptedException e) {
				log.fatal("Problem starting or running HwDriver program '"
						+ pathToDriver + "'.", e);
			} finally {
				stopThread(t);
			}
		}
	}

	/**
	 * Starts different things, in 3 separate threads:
	 * <ol>
	 * <li>The Hardware Listener, listening for incoming messages from the C
	 * hardware driver, and returning commands.</li>
	 * <li>The Hardware Driver, a C program.</li>
	 * <li>The RMI server, for a Swing client, or JBoss server (for web site
	 * later on).</li>
	 * </ol>
	 * This object acts as a watchdog too, if one of the treads fails this one
	 * tries to restart. If that fails it exits, and the parent shell must
	 * restart.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int looptime = 100; // 100 ms default
		String path2Driver = null;
		String logcfgfile = null;
		String blocksCfgFile = null;
		String hwCfgFile = null;
		String hostname = "localhost";
		int port = HwDriverTcpChannel.DEFAULT_DRIVER_PORT;
		if (args.length == 0) {
			System.err.println("No arguments given.");
			usage();
		}
		boolean domotic = false;
		boolean simulation = false;
		if (args[0].equalsIgnoreCase("domo"))
			domotic = true;
		else if (!args[0].equalsIgnoreCase("hw"))
			usage();
		int i = 1;
		while (i < args.length) {
			if (args[i].equals("-t")) {
				if (++i >= args.length)
					usage();
				looptime = Integer.valueOf(args[i++]);
			} else if (args[i].equals("-d")) {
				if (++i >= args.length)
					usage();
				path2Driver = args[i++];
			} else if (args[i].equals("-s")) {
				i++;
				simulation = true;
			} else if (args[i].equals("-l")) {
				if (++i >= args.length)
					usage();
				logcfgfile = args[i++];
			} else if (args[i].equals("-b")) {
				if (++i >= args.length)
					usage();
				blocksCfgFile = args[i++];
			} else if (args[i].equals("-c")) {
				if (++i >= args.length)
					usage();
				hwCfgFile = args[i++];
			} else if (args[i].equals("-h")) {
				if (++i >= args.length)
					usage();
				hostname = args[i++];
			} else if (args[i].equals("-p")) {
				if (++i >= args.length)
					usage();
				port = Integer.parseInt(args[i++]);
			} else {
				System.err.println("Argument error. Failed on " + args[i]);
				usage();
			}
		}

		if (hwCfgFile == null) {
			System.err.println("Need hardware configuration file.");
			usage();
		}
		if (domotic && (blocksCfgFile == null)) {
			System.err
					.println("Both blocks-config-file and hardware-config-file must be specified for domotic system.");
			usage();
		}

		if (logcfgfile == null) {
			BasicConfigurator.configure();
		} else {
			PropertyConfigurator.configure(logcfgfile);
		}

		Main main = new Main();
		if (domotic) {
			log.info("STARTING Domotic system. Configuration:\n\tdriver:\t"
					+ path2Driver + "\n\tlooptime:\t" + looptime
					+ "ms\n\tlog-config:\t" + logcfgfile
					+ "\n\thardware cfg:\t" + hwCfgFile + "\n\tblocks cfg:\t"
					+ blocksCfgFile + "\n\tprocess pid:\t" + main.getPid());

			main.storePid();
			IHardwareIO hw;
			if (simulation)
				hw = main.setupSimulatedHardware(hwCfgFile);
			else
				hw = main.setupHardware(hwCfgFile, hostname, port);
			Domotic dom = main.setupBlocksConfig(blocksCfgFile, hw);

			ServiceServer server = new ServiceServer();
			server.start();

			Oscillator osc = new Oscillator(dom, looptime);
			main.runDomotic(dom, osc, path2Driver);
		} else {
			main.runHwConsole(hwCfgFile, hostname, port, path2Driver);
		}

		log.info("ENDED normally Domotic system.");
	}

	private static void usage() {
		System.out
				.println("Usage:\t"
						+ Main.class.getSimpleName()
						+ " domo [-d path2Driver] [-t looptime] [-l logconfigfile] [-h hostname] [-p port] -b blocks-config-file -c hardware-config-file\n"
						+ "\t"
						+ Main.class.getSimpleName()
						+ " hw [-d path2Driver] [-l logconfigfile] [-h hostname] [-p port] -c hardware-config-file\n"
						+ "\t-d path to driver, if it needs to be started and managed by this program\n"
						+ "\t-t time between loops, in ms\n"
						+ "\t-b domotic blocks xml configuration file\n"
						+ "\t-c hardware xml configuration file\n"
						+ "\t-l log4j configuration file\n");
		System.exit(2);
	}

}
