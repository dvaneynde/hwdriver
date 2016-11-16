package eu.dlvm.domotics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.diamondsys.HwConsole;

public class HwConsoleRunner {

	private static final Logger log = LoggerFactory.getLogger(HwConsoleRunner.class);

	@SuppressWarnings("deprecation")
	public void run(final String cfgFilename, final String hostname, final int port, String pathToDriver) {
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
					Main.logDriver.info(line);
				}
			} catch (IOException e) {
				log.error("Problem starting or running HwDriver program '" + pathToDriver + "'.", e);
			} catch (InterruptedException e) {
				log.error("Problem starting or running HwDriver program '" + pathToDriver + "'.", e);
			} finally {
				t.stop();
			}
		}
	}

}
