package eu.dlvm.domotics;

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

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.Oscillator;
import eu.dlvm.domotics.factories.XmlDomoticConfigurator;
import eu.dlvm.domotics.service.ServiceServer;
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

	public IHardwareIO setupHardware(String cfgFile, String host, int port, int readTimeout) {
		XmlHwConfigurator xcf = new XmlHwConfigurator();
		xcf.setCfgFilepath(cfgFile);
		IHwDriverChannel hdc = new HwDriverTcpChannel(host, port, readTimeout);
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
			log.error("Cannot configure system, abort. Reason:" + e.getMessage());
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
	public void runDomotic(final Domotic dom, final Oscillator osc, String pathToDriver) {
		if (pathToDriver == null) {
			log.info("Initializing domotic.");
			dom.initialize();
			log.info("Domotic starts looping now, in main thread.");
			osc.go();
		} else {
			// TODO see
			// http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
			Runnable domoticRunner = new Runnable() {
				@Override
				public void run() {
					try {
						log.info("Oscillator oscillates...");
						osc.go();
						log.error("Oh oh... oscillator has stopped for no apparent reason. Should not happen. Nothing done for now.");
					} catch (Exception e) {
						log.error("Oh oh... oscillator has stopped. Nothing done further, should restart or something...", e);
					}
				}
			};
			Thread domoticThread = new Thread(domoticRunner, "Domotic Blocks Execution.");
			try {
				log.info("Start HwDriver, and wait 5 seconds...");
				ProcessBuilder pb = new ProcessBuilder(pathToDriver, "localhost");
				final Process process = pb.start();
				Thread.sleep(5000); // TODO replace later by
									// prStdout.isDriverReady()
				log.info("Initialize domotic system.");
				dom.initialize();
				log.info("Start Domotic thread 'Domotic Blocks Execution'.");
				domoticThread.start();
				log.info("Start watchdogs, on hwdriver and on blocks execution.");
				// dan pas domotic starten
				ProcessWatch prWatch = new ProcessWatch(process, "Driver Process Watch");
				ProcessReader prStdout = new ProcessReader(process.getInputStream(), "Driver STDOUT Reader");
				ProcessReader prStderr = new ProcessReader(process.getErrorStream(), "Driver STDERR Reader");
				prWatch.startWatching();
				prStdout.startReading();
				prStderr.startReading();
				log.info("Everything started, now watching...");
				long lastLoopSequence = -1;
				while (true) {
					Thread.sleep(5000);
					{
						long currentLoopSequence = dom.getLoopSequence();
						if (currentLoopSequence <= lastLoopSequence)
							log.error("Domotic does not seem to be looping anymore, last recorded loopsequence=" + lastLoopSequence + ", current=" + currentLoopSequence);
						lastLoopSequence = currentLoopSequence;
					}
					// TODO dump state to disk...
					// TODO extra test: loopcounter moet verhoogd zijn...
					if (prWatch.isRunning() && prStdout.isRunning() && prStderr.isRunning()) {
						log.debug("Checked driver sub-process, seems OK.");
					} else {
						log.error("Something is wrong with driver subprocess. I'll just continue, recovery not yet implemented, but below is report:");
						log.error("\tprocess watch: " + prWatch.toString());
						log.error("\tprocess stdout: " + prStdout.toString());
						log.error("\tprocess stderr: " + prStderr.toString());
						// TODO misschien best non-zero exit code geven, en
						// soort wrapper herstart het volledige domotica, dat
						// ook eerst laatst weggeschreven status leest
					}
				}

			} catch (IOException e) {
				log.fatal("Problem starting or running HwDriver program '" + pathToDriver + "'.", e);
			} catch (InterruptedException e) {
				log.fatal("Got interrupted, could be Thread.sleep() stuff; put in try-catch-repeat.", e);
			} finally {
				stopThread(domoticThread);
			}
		}
		log.info("Domotica stopped looping.");
	}

	@SuppressWarnings("deprecation")
	private void stopThread(Thread t) {
		t.stop(new CancellationException("Stop requested, due to error starting HwDriver as subprocess."));
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

	public void runHwConsole(final String cfgFilename, final String hostname, final int port, String pathToDriver) {
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
				ProcessBuilder pb = new ProcessBuilder(pathToDriver, "localhost");
				Process process = pb.start();
				Thread.sleep(5000);
				t.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					logDriver.info(line);
				}
			} catch (IOException e) {
				log.fatal("Problem starting or running HwDriver program '" + pathToDriver + "'.", e);
			} catch (InterruptedException e) {
				log.fatal("Problem starting or running HwDriver program '" + pathToDriver + "'.", e);
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
			System.err.println("Both blocks-config-file and hardware-config-file must be specified for domotic system.");
			usage();
		}

		if (logcfgfile == null) {
			System.out.println("Logging starts, using log4j default configuration.");
			BasicConfigurator.configure();
		} else {
			System.out.println("Logging starts, using log4j configuration from '"+logcfgfile+"'.");
			PropertyConfigurator.configure(logcfgfile);
		}

		Main main = new Main();
		if (domotic) {
			log.info("STARTING Domotic system. Configuration:\n\tdriver:\t" + path2Driver + "\n\tlooptime:\t" + looptime + "ms\n\tlog-config:\t" + logcfgfile + "\n\thardware cfg:\t"
					+ hwCfgFile + "\n\tblocks cfg:\t" + blocksCfgFile + "\n\tprocess pid:\t" + main.getPid());

			main.storePid();
			IHardwareIO hw;
			if (simulation)
				hw = main.setupSimulatedHardware(hwCfgFile);
			else
				hw = main.setupHardware(hwCfgFile, hostname, port, looptime * 9 / 10);
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
		System.out.println("Usage:\t" + Main.class.getSimpleName()
				+ " domo [-d path2Driver] [-t looptime] [-l logconfigfile] [-h hostname] [-p port] -b blocks-config-file -c hardware-config-file\n" + "\t" + Main.class.getSimpleName()
				+ " hw [-d path2Driver] [-l logconfigfile] [-h hostname] [-p port] -c hardware-config-file\n" + "\t-d path to driver, if it needs to be started and managed by this program\n"
				+ "\t-t time between loops, in ms\n" + "\t-b domotic blocks xml configuration file\n" + "\t-c hardware xml configuration file\n" + "\t-l log4j configuration file\n");
		System.exit(2);
	}

}
