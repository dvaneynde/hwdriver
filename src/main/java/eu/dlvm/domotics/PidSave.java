package eu.dlvm.domotics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PidSave {

	private static Logger log = LoggerFactory.getLogger(PidSave.class);
	private String pid;
	private File pidPath;

	/**
	 * @param fileToSavePid
	 */
	public PidSave(File fileToSavePid) {
		this.pidPath = fileToSavePid;
	}

	/**
	 * Find PID of current process (once) and save it to file (once).
	 */
	public synchronized String getPidFromCurrentProcessAndStoreToFile() {
		if (pid == null) {
			String fullpid = ManagementFactory.getRuntimeMXBean().getName();
			pid = fullpid.substring(0, fullpid.indexOf('@'));
			storePid();
			log.info("Stored pid " + pid + " of current process to file" + pidPath.getAbsolutePath() + '.');
		}
		return pid;
	}

	private void storePid() {
		FileWriter fw = null;
		try {
			fw = new FileWriter(pidPath);
			fw.write(getPidFromCurrentProcessAndStoreToFile());
		} catch (FileNotFoundException e) {
			log.error("Cannot start, cannot write pid file.", e);
			System.exit(2);
		} catch (IOException e) {
			log.error("Cannot start, cannot write pid file.", e);
			System.exit(2);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
