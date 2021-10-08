package eu.dlvm.domotics;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.factories.XmlDomoticConfigurator;
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

	public static final int DEFAULT_LOOP_TIME_MS = 20;
	public static final Logger logDriver = LoggerFactory.getLogger("DRIVER");

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public IHardwareIO setupHardware(String cfgFile, String host, int port, int readTimeout, boolean simulated) {
		XmlHwConfigurator xhc = new XmlHwConfigurator(cfgFile);
		IHwDriverChannel hdc;
		if (simulated)
			hdc = new HwDriverChannelSimulator();
		else
			hdc = new HwDriverTcpChannel(host, port, readTimeout);
		HardwareIO hw = new HardwareIO(xhc, hdc);
		return hw;
	}

	public Domotic setupBlocksConfig(String cfgFilename, IHardwareIO hw) {
		try {
			Domotic domotic = Domotic.createSingleton(hw);
			XmlDomoticConfigurator.configure(cfgFilename, hw, domotic);
			return domotic;
		} catch (Exception e) {
			log.error("Cannot configure system, abort.", e);
			throw new RuntimeException("Abort. Cannot configure system.",e);
		}
	}

	private void startAndRunDomotic(int looptime, String path2Driver, String blocksCfgFile, String hwCfgFile, String hostname, int port, File htmlRootFile,
			boolean simulation) {
		PidSave pidSave = new PidSave(new File("./domotic.pid"));
		String pid = pidSave.getPidFromCurrentProcessAndStoreToFile();
		log.info("STARTING Domotic system. Configuration:\n\tdriver:\t" + path2Driver + "\n\tlooptime:\t" + looptime + "ms\n\thardware cfg:\t" + hwCfgFile
				+ "\n\tblocks cfg:\t" + blocksCfgFile + "\n\tprocess pid:\t" + pid);

		IHardwareIO hw = setupHardware(hwCfgFile, hostname, port, looptime * 9 / 10, simulation);
		Domotic dom = setupBlocksConfig(blocksCfgFile, hw);

		dom.runDomotic(looptime, path2Driver, htmlRootFile);
	}

	/**
	 * Starts either domotic or hardware-console program. Optionally it starts
	 * the hardware driver, or connects to an already running one.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int looptime = DEFAULT_LOOP_TIME_MS; // ms default
		String path2Driver = null;
		String blocksCfgFile = null;
		String hwCfgFile = null;
		String hostname = "localhost";
		int port = HwDriverTcpChannel.DEFAULT_DRIVER_PORT;
		File htmlRootFile = null;
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
			} else if (args[i].equals("-w")) {
				if (++i >= args.length)
					usage();
				htmlRootFile = new File(args[i++]);
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
		if (simulation && path2Driver != null) {
			System.err.println("Cannot have both simulation and a path to the hardware driver.");
			usage();
		}

		if (domotic) {
			new Main().startAndRunDomotic(looptime, path2Driver, blocksCfgFile, hwCfgFile, hostname, port, htmlRootFile, simulation);
		} else {
			new HwConsoleRunner().run(hwCfgFile, hostname, port, path2Driver);
		}

		log.info("ENDED normally Domotic system.");
	}

	private static void usage() {
		System.out.println("Usage:\ttwo options:\n\t"
                + Main.class.getSimpleName() + " domo [-s] [-r] [-d path2Driver] [-t looptime] [-h hostname] [-p port] [-w webapproot] -b blocks-config-file -c hardware-config-file\n\t"
				+ Main.class.getSimpleName() + " hw [-d path2Driver] [-h hostname] [-p port] -c hardware-config-file"
                + "\n\twhere:"
				+ "\n\t-s simulate hardware driver (domotic only, for testing and development)"
				+ "\n\t-d path to driver, if it needs to be started and managed by this program"
                + "\n\t-t time between loops, in ms; defaults to "+ DEFAULT_LOOP_TIME_MS + " ms."
                + "\n\t-h hostname of hardware driver; incompatible with -d"
				+ "\n\t-p port of hardware driver; incompatible with -d"
                + "\n\t-w path of directory with webapp (where index.html is located)"
				+ "\n\t-b domotic blocks xml configuration file"
                + "\n\t-c hardware xml configuration file"
				+ "\nTo configure logging externally, use 'java -Dlogback.configurationFile=/path/to/config.xml ...' or system env variable.\n");
		System.exit(2);
	}

}
