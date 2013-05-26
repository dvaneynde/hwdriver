package eu.dlvm.iohardware;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;
import eu.vaneynde.domotic.Main;

// TODO moet naar diamondsys sub-package, want gebruikt meer dan alleen IHardwareIO... Zie import statemens !
public class HwConsole implements Runnable {

    static Logger log = Logger.getLogger(Main.class);

 
    private HardwareIO hw;

    final static String ESC = "\033[";
    private Thread thread;
    private Pattern patternINIT = Pattern.compile("i(?:nit)? *(\\d*+)", Pattern.CASE_INSENSITIVE);
    private Pattern patternSTATUS = Pattern.compile("s(?:tatus)? *(\\d*+)", Pattern.CASE_INSENSITIVE);
    private Pattern patternLISTEN = Pattern.compile("l(?:isten)? *(\\d++)", Pattern.CASE_INSENSITIVE);
    private Pattern patternOA = Pattern.compile("oa *(\\d++)\\.(\\d++) +(\\d++)", Pattern.CASE_INSENSITIVE);
    private Pattern patternOD = Pattern.compile("od *(\\d++)\\.(\\d++) +(t(?:rue)?|f(?:alse)?)", Pattern.CASE_INSENSITIVE);
    private Pattern patternALLON = Pattern.compile("a(?:llon)? *(\\d*+)", Pattern.CASE_INSENSITIVE);
    private Pattern patternHELP = Pattern.compile("h(?:elp)?\\s*", Pattern.CASE_INSENSITIVE);
    private Pattern patternQUIT = Pattern.compile("q(?:uit)?\\s*", Pattern.CASE_INSENSITIVE);

    private void help() {
        System.out.println("help | quit: this help, or quit.");
        System.out.println("i [boardnr]: initialise all/specific board(s)\t\t" + patternSTATUS);
        System.out.println("s [boardnr]: status of all/specific board(s)\t\t" + patternSTATUS);
        System.out.println("l [boardnr]: continuesly listen on a board status, or all boards\t\t" + patternSTATUS);
        System.out.println("oa boardnr channel value: set analog out\t\t" + patternOA.toString());
        System.out.println("od boardnr channel {true|false}: set digital out\t" + patternOD.toString());
        System.out.println("a [boardnr]: set all on, or only boardnr\t" + patternALLON.toString());
    }

    public HwConsole(File cfgFile, String hostname, int port) {
        thread = new Thread(this, "HwConsole");
        XmlHwConfigurator cfgr = new XmlHwConfigurator();
        cfgr.setCfgFilepath(cfgFile.toString());
        // Om te testen, zonder hw: volgende twee lijnen vervangen door een pseudo mock IHardwareIO maken, met boards en channelMap.
        HwDriverTcpChannel hdtc = new HwDriverTcpChannel(hostname, port);
        hw = new HardwareIO(cfgr, hdtc);
        hw.initialize();
    }

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
        String line = ""; // Line read from standard in

        System.out.println("Enter a line of text (type 'quit' to exit): ");
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);

        Matcher m;
        boolean quit = false;
        while (!quit) {
            try {
                line = in.readLine();
                if ((m = patternHELP.matcher(line)).matches()) {
                    help();
                } else if ((m = patternQUIT.matcher(line)).matches()) {
                    quit = true;
                } else if ((m = patternOD.matcher(line)).matches()) {
                    outputDigital(m);
                } else if ((m = patternOA.matcher(line)).matches()) {
                    outputAnalog(m);
                } else if ((m = patternALLON.matcher(line)).matches()) {
                    allon(m);
                } else if ((m = patternSTATUS.matcher(line)).matches()) {
                    status(m);
                } else if ((m = patternLISTEN.matcher(line)).matches()) {
                    listen(m);
                } else if ((m = patternINIT.matcher(line)).matches()) {
                    init(m);
                } else {
                    System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                log.warn("Exception caught, ignored.", e);
                System.out.println("Exception: " + e.getMessage());
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
                    while (true) {
                        hw.refreshInputs();
                        System.out.println("\u001b[2J");
                        System.out.println(b + "\n\nPress enter to stop...");
                        Thread.sleep(1500);
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
    }

    private void outputDigital(Matcher m) {
        // Pattern "od (\\d++) (\\d++) (t(?:rue)|f(?:alse))"
        try {
            int nr = Integer.parseInt(m.group(1));
            int ch = Integer.parseInt(m.group(2));
            boolean val = (m.group(3).charAt(0) == 't');
            Board b = findBoard(nr);
            if (b == null)
                System.out.println("Unknown board number.");
            else {
                b.writeDigitalOutput(ch, val);
                hw.refreshOutputs();
            }
            System.out.println("Output changed.");
        } catch (NumberFormatException e) {
            System.out.println("Error in command. Please retry.");
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
                System.out.println("Unknown board number.");
            else {
                b.writeAnalogOutput(ch, val);
                hw.refreshOutputs();
            }
            System.out.println("Output changed.");
        } catch (NumberFormatException e) {
            System.out.println("Error in command. Please retry.");
        }
    }

    private void allon(Matcher m) {
        if (m.group(1).equals("")) {
            for (Board b : hw.getBoards())
                boardAllOn(b);
        } else {
            int boardnr = Integer.parseInt(m.group(1));
            Board b = findBoard(boardnr);
            boardAllOn(b);
        }
        hw.refreshOutputs();
    }

    private void boardAllOn(Board b) {
        if (b.isEnabled(ChannelType.DigiOut)) {
            for (int ch = 0; ch < b.nrOfChannels(ChannelType.DigiOut); ch++)
                b.writeDigitalOutput(ch, true);
        }
        if (b.isEnabled(ChannelType.AnlgOut)) {
            for (int ch = 0; ch < b.nrOfChannels(ChannelType.AnlgOut); ch++)
                b.writeAnalogOutput(ch, 1000);
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
        String logcfgfile = null;
        String hwCfgFile = null;
        String driverHostname = "localhost";
        int driverPort = HwDriverTcpChannel.DEFAULT_DRIVER_PORT;

        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-l")) {
                if (++i >= args.length)
                    usage();
                logcfgfile = args[i++];
            } else if (args[i].equals("-h")) {
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

        if (hwCfgFile == null) {
            System.err.println("Hardware config file must be specified.");
            usage();
        }
        File cfgFile = new File(hwCfgFile);
        if (!cfgFile.exists()) {
            System.err.println("Hardware cfg file does not exist.");
            usage();
        }
        if (logcfgfile == null) {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
        } else {
            PropertyConfigurator.configure(logcfgfile);
        }

        HwConsole c = new HwConsole(cfgFile, driverHostname, driverPort);
        c.start(); // TODO, niet nodig; maar 1 thread nodig voor listen

    }

    private static void usage() {
        System.out.println("Usage: " + HwConsole.class.getName() + " -c config-file [-l logconfigfile] [-h hostname] [-p port]");
        System.out.println("\t-c hardware xml configuration file");
        System.out.println("\t-l log4j configuration file");
        System.out.println("\t-h hostname, default localhost");
        System.out.println("\t-l portnumber, default " + HwDriverTcpChannel.DEFAULT_DRIVER_PORT);
        System.exit(2);
    }

}
