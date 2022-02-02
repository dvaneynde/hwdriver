package eu.dlvm.domotics;

import eu.dlvm.domotics.Main;

import java.io.File;

public class DomConfig {

    public static final int DEFAULT_LOOP_TIME_MS = 20;


    public int looptime = DEFAULT_LOOP_TIME_MS;
    public boolean domotic = false;
    public boolean simulation = false;
    public String path2Driver = null;
    public String blocksCfgFile = null;
    public String hwCfgFile = null;
    public String hostname = "localhost";
    public int port = 4444;
    public File htmlRootFile = null;

    public DomConfig(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("No arguments given.");
        }
        if (args[0].equalsIgnoreCase("domo"))
            domotic = true;
        else if (!args[0].equalsIgnoreCase("hw"))
            throw new RuntimeException("Need either \"domo\" or \"hw\" as first parameter.");
        int i = 1;
        while (i < args.length) {
            if (args[i].equals("-t")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -t needs an argument.");
                looptime = Integer.valueOf(args[i++]);
            } else if (args[i].equals("-d")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -d needs an argument.");
                path2Driver = args[i++];
            } else if (args[i].equals("-s")) {
                i++;
                simulation = true;
            } else if (args[i].equals("-b")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -b needs an argument.");
                blocksCfgFile = args[i++];
            } else if (args[i].equals("-c")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -c needs an argument.");
                hwCfgFile = args[i++];
            } else if (args[i].equals("-h")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -h needs an argument.");
                hostname = args[i++];
            } else if (args[i].equals("-p")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -p needs an argument.");
                port = Integer.parseInt(args[i++]);
            } else if (args[i].equals("-w")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -w needs an argument.");
                htmlRootFile = new File(args[i++]);
            } else {
                throw new RuntimeException("Argument error. Failed on " + args[i]);
            }
        }

        if (hwCfgFile == null) {
            throw new RuntimeException("Need hardware configuration file.");
        }
        if (domotic && (blocksCfgFile == null)) {
            throw new RuntimeException("Both blocks-config-file and hardware-config-file must be specified for domotic system.");
        }
        if (simulation && path2Driver != null) {
            throw new RuntimeException("Cannot have both simulation and a path to the hardware driver.");
        }
    }

    public static void usage() {
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
    }
}
