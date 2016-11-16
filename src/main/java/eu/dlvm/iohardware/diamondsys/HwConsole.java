package eu.dlvm.iohardware.diamondsys;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.Main;
import eu.dlvm.iohardware.ChannelFault;
import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;

// TODO moet naar diamondsys sub-package, want gebruikt meer dan alleen IHardwareIO... Zie import statemens !
public class HwConsole {

	static Logger log = LoggerFactory.getLogger(Main.class);

	private HardwareIO hw;

	final static String ESC = "\033[";
	private Pattern patternHELP = Pattern.compile("h(?:elp)?\\s*", Pattern.CASE_INSENSITIVE);
	private Pattern patternQUIT = Pattern.compile("q(?:uit)?\\s*", Pattern.CASE_INSENSITIVE);
	private Pattern patternINIT = Pattern.compile("i(?:nit)? *(\\d*+)", Pattern.CASE_INSENSITIVE);
	private Pattern patternSTATUS = Pattern.compile("s(?:tatus)? *(\\d*+)", Pattern.CASE_INSENSITIVE);
	private Pattern patternLISTEN = Pattern.compile("l(?:isten)? *(\\d++)", Pattern.CASE_INSENSITIVE);
	private Pattern patternOA = Pattern.compile("oa *(\\d++)\\.(\\d++) +(\\d++)", Pattern.CASE_INSENSITIVE);
	private Pattern patternOD = Pattern.compile("od *(\\d++)\\.(\\d++) +(t(?:rue)?|f(?:alse)?)",
			Pattern.CASE_INSENSITIVE);
	private Pattern patternALL = Pattern.compile("a(?:ll)? +(on|off) *(\\d*+)", Pattern.CASE_INSENSITIVE);
	private Pattern patternFUN = Pattern.compile("f(?:un)?\\s*(\\d++) +(\\d++)", Pattern.CASE_INSENSITIVE);

	private void help() {
		System.out.println("help | quit: this help, or quit.");
		System.out.println("i [boardnr]: initialise all/specific board(s)\t\t" + patternINIT.toString());
		System.out.println("s [boardnr]: status of all/specific board(s)\t\t" + patternSTATUS.toString());
		System.out.println("l [boardnr]:  listen on one/all board status\t\t" + patternLISTEN.toString());
		System.out.println("oa boardnr.channel value: set analog out\t\t" + patternOA.toString());
		System.out.println("od boardnr.channel {t[rue]|f[alse]}: set digital out\t" + patternOD.toString());
		System.out.println("a {on|off} [boardnr]: set all on/off, or only boardnr\t" + patternALL.toString());
		System.out.println("fun time runs: fun loop, 'time' ms between and 'runs' runs\t" + patternFUN.toString());
	}

	public HwConsole(String cfgFilename, String hostname, int port) throws IllegalArgumentException {
		if (cfgFilename == null)
			throw new IllegalArgumentException("Hardware config file must be specified.");
		File cfgFile = new File(cfgFilename);
		if (!cfgFile.exists()) {
			throw new IllegalArgumentException("Hardware cfg file does not exist.");
		}

		XmlHwConfigurator cfgr = new XmlHwConfigurator(cfgFile.toString());
		// Om te testen, zonder hw: volgende twee lijnen vervangen door een pseudo mock IHardwareIO maken, met boards en channelMap.
		HwDriverTcpChannel hdtc = new HwDriverTcpChannel(hostname, port, 1000);
		hw = new HardwareIO(cfgr, hdtc);
		try {
			hw.initialize();
		} catch (ChannelFault e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void processCommands() {
		String line = ""; // Line read from standard in

		System.out.println("Enter a line of text (type 'quit' to exit): ");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);

		Matcher m;
		boolean quit = false;
		while (!quit) {
			try {
				line = in.readLine();
				if (line.length() == 0)
					continue;
				if ((m = patternHELP.matcher(line)).matches()) {
					help();
				} else if ((m = patternQUIT.matcher(line)).matches()) {
					quit = true;
					hw.getDriverChannel().disconnect();
				} else if ((m = patternFUN.matcher(line)).matches()) {
					fun(m);
				} else if ((m = patternOD.matcher(line)).matches()) {
					outputDigital(m);
				} else if ((m = patternOA.matcher(line)).matches()) {
					outputAnalog(m);
				} else if ((m = patternALL.matcher(line)).matches()) {
					all(m);
				} else if ((m = patternSTATUS.matcher(line)).matches()) {
					status(m);
				} else if ((m = patternLISTEN.matcher(line)).matches()) {
					listen(m);
				} else if ((m = patternINIT.matcher(line)).matches()) {
					init(m);
				} else {
					System.err.println("Unknown command.");
				}
			} catch (Exception e) {
				log.warn("Exception caught, ignored.", e);
				System.err.println("Exception: " + e.getMessage());
			}
		}
		System.out.println("Okay, I quit.");
	}

	private void status(Matcher m) {
		hw.refreshInputs();
		if (m.group(1).equals("")) {
			for (Board b : hw.getBoards())
				System.out.println(b);
		} else {
			int boardnr = Integer.parseInt(m.group(1));
			Board b = findBoard(boardnr);
			System.out.println(b);
		}
	}

	private void listen(Matcher m) {
		int boardnr = Integer.parseInt(m.group(1));
		final Board b = findBoard(boardnr);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					int ctr = 0;
					while (true) {
						hw.refreshInputs();
						System.out.println("\u001b[2J");
						System.out.println(b + "\n\n[" + ctr + "] Press enter to stop...");
						Thread.sleep(1500);
						ctr++;
					}
				} catch (InterruptedException e) {
				}
			}

		};
		Thread t = new Thread(r, "show-board");
		t.start();
		try {
			System.in.read();
		} catch (IOException e) {
		}
		t.interrupt();
		System.out.println("Listen done.");
	}

	private void fun(Matcher m) throws InterruptedException {
		int time = Integer.parseInt(m.group(1));
		int runs = Integer.parseInt(m.group(2));
		if (time < 10)
			time = 10;
		int[] boardnrs = { 0, 2, 4, 2, 3 };
		int[] start = { 7, 8, 7, 0, 0 };
		int[] step = { -1, 1, -1, 1, 1 };
		List<Board> boards = hw.getBoards();
		boolean value = true;
		for (int run = 0; run < runs; run++) {
			System.out.println("Fun! run " + run + "...");
			for (int idx = 0; idx < boardnrs.length; idx++) {
				Board b = boards.get(boardnrs[idx]);
				for (int channel = start[idx]; channel < (start[idx] + 8) && channel >= 0; channel += step[idx]) {
					if (channel == start[idx])
						System.out.println("Fun!\tboard=" + b.getBoardNumber() + " setting channels [" + start[idx]
								+ ".." + (start[idx] + step[idx] * 7) + "] to:" + value);
					//System.out.println("idx="+idx+", channel="+channel);
					b.writeDigitalOutput(channel, value);
					hw.refreshOutputs();
					Thread.sleep(time);
				}
			}
			value = !value;
		}
	}

	private void outputDigital(Matcher m) {
		// Pattern "od (\\d++) (\\d++) (t(?:rue)|f(?:alse))"
		try {
			int nr = Integer.parseInt(m.group(1));
			int ch = Integer.parseInt(m.group(2));
			boolean val = (m.group(3).charAt(0) == 't');
			Board b = findBoard(nr);
			if (b == null)
				System.err.println("Unknown board number.");
			else {
				b.writeDigitalOutput(ch, val);
				hw.refreshOutputs();
			}
			System.out.println("Output changed.");
		} catch (NumberFormatException e) {
			System.err.println("Error in command. Please retry.");
		}
	}

	private void outputAnalog(Matcher m) {
		// Pattern "oa +(\\d++) +(\\d++) +(\\d++)
		try {
			int nr = Integer.parseInt(m.group(1));
			int ch = Integer.parseInt(m.group(2));
			int val = Integer.parseInt(m.group(3));
			Board b = findBoard(nr);
			if (b == null)
				System.err.println("Unknown board number.");
			else {
				b.writeAnalogOutput(ch, val);
				hw.refreshOutputs();
			}
			System.out.println("Output changed.");
		} catch (NumberFormatException e) {
			System.err.println("Error in command. Please retry.");
		}
	}

	private void all(Matcher m) {
		boolean on = (m.group(1).equals("on"));
		if (m.group(2).equals("")) {
			for (Board b : hw.getBoards())
				boardAll(b, on);
		} else {
			int boardnr = Integer.parseInt(m.group(2));
			Board b = findBoard(boardnr);
			boardAll(b, on);
		}
		hw.refreshOutputs();
	}

	private void boardAll(Board b, boolean on) {
		if (b.isEnabled(ChannelType.DigiOut)) {
			for (int ch = 0; ch < b.nrOfChannels(ChannelType.DigiOut); ch++)
				b.writeDigitalOutput(ch, on);
		}
		if (b.isEnabled(ChannelType.AnlgOut)) {
			for (int ch = 0; ch < b.nrOfChannels(ChannelType.AnlgOut); ch++)
				b.writeAnalogOutput(ch, (on ? 1000 : 0));
		}
	}

	private void init(Matcher m) {
		if (m.group(1).equals("")) {
			for (Board b : hw.getBoards())
				b.init();
		} else {
			int boardnr = Integer.parseInt(m.group(1));
			Board b = findBoard(boardnr);
			b.init();
		}
		hw.refreshOutputs();
		System.out.println("Board(s) initialized.");
	}

	private Board findBoard(int boardnr) {
		for (Board b : hw.getBoards())
			if (b.getBoardNumber() == boardnr)
				return b;
		return null;
	}

	public static void main(String[] args) {
		String hwCfgFile = null;
		String driverHostname = "localhost";
		int driverPort = HwDriverTcpChannel.DEFAULT_DRIVER_PORT;

		int i = 0;
		while (i < args.length) {
			if (args[i].equals("-h")) {
				if (++i >= args.length)
					usage();
				driverHostname = args[i++];
			} else if (args[i].equals("-p")) {
				if (++i >= args.length)
					usage();
				driverPort = Integer.parseInt(args[i++]);
			} else if (args[i].equals("-c")) {
				if (++i >= args.length)
					usage();
				hwCfgFile = args[i++];
			} else
				usage();
		}

		try {
			HwConsole c = new HwConsole(hwCfgFile, driverHostname, driverPort);
			c.processCommands();
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			usage();
		}

	}

	private static void usage() {
		System.out.println("Usage: " + HwConsole.class.getName() + " -c config-file [-h hostname] [-p port]");
		System.out.println("\t-c hardware xml configuration file");
		System.out.println("\t-h hostname, default localhost");
		System.out.println("\t-l portnumber, default " + HwDriverTcpChannel.DEFAULT_DRIVER_PORT);
		System.exit(2);
	}

}
