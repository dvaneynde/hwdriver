package eu.dlvm.domotics;

import java.io.File;

import eu.dlvm.iohardware.diamondsys.HardwareBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.factories.XmlDomoticConfigurator;
import eu.dlvm.iohardware.IHardwareIO;

/**
 * Domotic system main entry point.
 *
 * @author Dirk Vaneynde
 */
public class Main {

    public static final Logger logDriver = LoggerFactory.getLogger("DRIVER");

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public Domotic setupBlocksConfig(String cfgFilename, IHardwareIO hw) {
        try {
            Domotic domotic = Domotic.createSingleton(hw);
            XmlDomoticConfigurator.configure(cfgFilename, hw, domotic);
            return domotic;
        } catch (Exception e) {
            log.error("Cannot configure system, abort.", e);
            throw new RuntimeException("Abort. Cannot configure system.", e);
        }
    }

    private void startAndRunDomotic(int looptime, String path2Driver, String blocksCfgFile, String hwCfgFile, String hostname, int port, File htmlRootFile,
                                    boolean simulation) {
        PidSave pidSave = new PidSave(new File("./domotic.pid"));
        String pid = pidSave.getPidFromCurrentProcessAndStoreToFile();
        log.info("STARTING Domotic system. Configuration:\n\tdriver:\t" + path2Driver + "\n\tlooptime:\t" + looptime + "ms\n\thardware cfg:\t" + hwCfgFile
                + "\n\tblocks cfg:\t" + blocksCfgFile + "\n\tprocess pid:\t" + pid);

        // See coments in IHardwareBuilder, this is not yet generic enough
        IHardwareIO hw = (new HardwareBuilder()).build(hwCfgFile, hostname, port, looptime * 9 / 10, simulation);
        Domotic dom = setupBlocksConfig(blocksCfgFile, hw);

        dom.runDomotic(looptime, path2Driver, htmlRootFile);
    }

    /**
     * Starts either domotic or hardware-console program. Optionally it starts
     * the hardware driver, or connects to an already running one.
     *
     * @param args See DomConfig
     */
    public static void main(String[] args) {
        DomConfig cfg;
        try {
            cfg = new DomConfig(args);
            if (cfg.domotic) {
                new Main().startAndRunDomotic(cfg.looptime, cfg.path2Driver, cfg.blocksCfgFile, cfg.hwCfgFile, cfg.hostname, cfg.port, cfg.htmlRootFile, cfg.simulation);
            } else {
                new HwConsoleRunner().run(cfg.hwCfgFile, cfg.hostname, cfg.port, cfg.path2Driver);
            }
            log.info("ENDED normally Domotic system.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            DomConfig.usage();
            System.exit(2);
        }

    }


}
