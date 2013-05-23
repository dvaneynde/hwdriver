package eu.vaneynde.domotic;

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
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;

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

	public IHardwareIO setupHardware(String cfgFile) {
		XmlHwConfigurator xcf = new XmlHwConfigurator();
		xcf.setCfgFilepath(cfgFile);
		HwDriverTcpChannel hdtc = new HwDriverTcpChannel("localhost", 4444);
		HardwareIO hw = new HardwareIO(xcf, hdtc);
		return hw;
	}

	public Domotic setupBlocksConfig(String cfgFilename, IHardwareIO hw) {
		Domotic d = new Domotic();
		XmlDomoticConfigurator cf = new XmlDomoticConfigurator();
		cf.setCfgFilepath("DomoticConfig.xml");
		cf.configure(d);
		d.setHw(hw);
		return d;
	}

	/*
	public Domotic setupHardcodedDiamondsys(int msLooptime) {
		log.info("Domotica CONFIGURE.");

		// Setup Hardware
		HardwareIO hw = new HardwareIO();
		hw.setLoopInterval(msLooptime);

		// Setup Channel for Hardware Driver
		// s = new HwDriverTcpChannel(hw);

		// Setup Domotic
		Domotic dom = new Domotic();
		dom.setHw(hw);

		// Setup Blocks Layer, the blocks themselves
		Switch sAll = new Switch("AllOff", "Alles uit schakelaars.", new LogCh(
				0), dom);
		sAll.setLongClickEnabled(true);
		sAll.setLongClickTimeout(1500);
		sAll.setSingleClickEnabled(false);
		sAll.setDoubleClickEnabled(false);
		dom.addSensor(sAll);

		// Setup lamp WC
		Switch sWC = new Switch("SwitchWC0",
				"Schakelaar Licht WC Gelijkvloers", new LogCh(2), dom);
		Lamp lamp = new Lamp("LampWC0", "Licht WC Gelijkvloers", new LogCh(7),
				dom);
		SwitchBoard ssb = new SwitchBoard("LampenAanUit",
				"Lampen aan/uit via pushbutton switch.");
		ssb.add(sWC, lamp);
		ssb.add(sAll, null, true, false);
		dom.addSensor(sWC);
		dom.addActuator(lamp);

		// Setup Ventilator
		Switch sFan = new Switch("S_FanWC0",
				"Schakelaar ventilator WC gelijkvloers.", new LogCh(1), dom);
		Fan fan = new Fan("Fan_WC0", "Ventilator WC Gelijkvloers", lamp, 6, dom);
		fan.setDelayPeriodSec(2 * 60); // 2 minutes
		fan.setRunningPeriodSec(5 * 60); // 10 minutes
		SwitchBoardFans sbf = new SwitchBoardFans("SwitchBoardFans",
				"Schakelbord ventilatoren.");
		sbf.add(sFan, fan);
		dom.addSensor(sFan);
		dom.addActuator(fan);

		// Setup Dimmer
		DimmerSwitches dsw1 = new DimmerSwitches("DimSwitchCircante",
				"Dimmer Switches rond circante tafel.", 4, 3, dom);
		DimmedLamp dl1 = new DimmedLamp("DimLampCircante",
				"Gedimde lichten rond circante tafel",
				DmmatBoard.ANALOG_RESOLUTION - 1, 101, dom);
		SwitchBoardDimmers sbd = new SwitchBoardDimmers("Dimmers",
				"Switchboard Dimmers");
		sbd.add(dsw1, dl1);
		sbd.add(sAll, true, false);
		dom.addSensor(dsw1);
		dom.addActuator(dl1);

		return dom;
	}
	*/

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
	public void go(final Domotic dom, final Oscillator osc, String pathToDriver) {
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
			Thread t = new Thread(r, "Domotic Blocks Execution.");
			
			try {
				log.info("Start HwDriver.");
				ProcessBuilder pb = new ProcessBuilder(pathToDriver, "localhost");
				Process process = pb.start();
				log.info("Initialize domotic and its hardware.");
				dom.initialize();
				log.info("Start Domotic looping in separate thread 'Domotic Blocks Execution'.");
				t.start();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					logDriver.info(line);
				}
			} catch (IOException e) {
				log.fatal("Problem starting or running HwDriver program '" + pathToDriver
						+ "'.", e);
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
		String driver2Path = null;
		String logcfgfile = null;
		String blocksCfgFile = null;
		String hwCfgFile = null;
	    boolean consoleMode = false;
		int i = 0;
		while (i < args.length) {
			if (args[i].equals("-t")) {
				if (++i >= args.length)
					usage();
				looptime = Integer.valueOf(args[i++]);
			} else if (args[i].equals("-d")) {
				if (++i >= args.length)
					usage();
				driver2Path = args[i++];
			} else if (args[i].equals("-l")) {
				if (++i >= args.length)
					usage();
				logcfgfile = args[i++];
			} else if (args[i].equals("-b")) {
				if (++i >= args.length)
					usage();
				blocksCfgFile = args[i++];
			} else if (args[i].equals("-h")) {
				if (++i >= args.length)
					usage();
				hwCfgFile = args[i++];
            } else if (args[i].equals("-c")) {
                consoleMode = true;
			} else
				usage();
		}

		if (blocksCfgFile == null || hwCfgFile == null) {
			log.equals("Both blocks-config-file and hardware-config-file must be specified.");
			usage();
		}

		if (logcfgfile == null) {
			BasicConfigurator.configure();
		} else {
			PropertyConfigurator.configure(logcfgfile);
		}

		Main main = new Main();
		log.info("STARTING Domotic system. Configuration:\n\tdriver:\t"
				+ driver2Path + "\n\tlooptime:\t" + looptime
				+ "ms\n\tlog-config:\t" + logcfgfile + "\n\thardware cfg:\t"
				+ hwCfgFile + "\n\tblocks cfg:\t" + blocksCfgFile
				+ "\n\tprocess pid:\t" + main.getPid());

		main.storePid();
		IHardwareIO hw = main.setupHardware(hwCfgFile);
		Domotic dom = main.setupBlocksConfig(blocksCfgFile, hw);
		Oscillator osc = new Oscillator(dom, looptime);
		main.go(dom, osc, driver2Path);

		log.info("ENDED normally Domotic system.");
	}

	private static void usage() {
		System.out
				.println("Usage: "
						+ Main.class.getName()
						+ " [-d path2Driver] [-t looptime] [-l logconfigfile] -b blocks-config-file -h hardware-config-file\n"
						+"\t-d path to driver, if it needs to be started and managed by this program\n"
						+"\t-c console mode"
						+"\t-t time between loops, in ms\n"
						+"\t-b domotic blocks xml configuration file\n"
						+"\t-h hardware xml configuration file\n"
						+"\t-l log4j configuration file\n");
		System.exit(2);
	}

}
