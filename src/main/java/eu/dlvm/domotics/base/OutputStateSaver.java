package eu.dlvm.domotics.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class OutputStateSaver {

	public static final String FILENAME = "DomoticOutputStates.txt";

	static Logger log = Logger.getLogger(OutputStateSaver.class);

	private File outputStatesFile;
	private boolean alreadyReportedFileNotFound = false;

	private File getOutputStatesFile() {
		if (outputStatesFile == null) {
			outputStatesFile = new File("/var/local", FILENAME);
			if (!outputStatesFile.exists()) {
				String homedir = System.getProperty("user.home");
				log.warn("/var/local does not seem to exist, so I'll try home directory instead, being:" + homedir);
				outputStatesFile = new File(homedir, FILENAME);
			}
			log.info("Safeguard last output states to file " + outputStatesFile.getAbsolutePath());
		}
		return outputStatesFile;
	}

	Map<String, RememberedOutput> readRememberedOutputs() {
		Map<String, RememberedOutput> ros = new HashMap<String, RememberedOutput>();
		File f = getOutputStatesFile();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				RememberedOutput ro = RememberedOutput.parse(line);
				if (ro != null) {
					ros.put(ro.getBlockName(), ro);
				}
			}
			log.info("Read previous ouptut states, number of entries is " + ros.size());
		} catch (FileNotFoundException e) {
			log.info("No remembered outputs file found, will initialize with defaults.");
		} catch (IOException e) {
			log.error("Error reading " + f.getName() + ", will not use remembered outputs.", e);
		}
		return ros;
	}

	void writeRememberedOutputs(List<Actuator> as) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputStatesFile()))) {
			for (Actuator a : as) {
				RememberedOutput ro = a.dumpOutput();
				if (ro != null) {
					bw.write(ro.dump() + '\n');
				}
			}
			log.debug("Wrote last output state to " + getOutputStatesFile().getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (!alreadyReportedFileNotFound)
				log.warn("Cannot write remembered output, file not found: " + getOutputStatesFile());
			alreadyReportedFileNotFound = true;
		} catch (IOException e) {
			log.error("Failed writing to " + getOutputStatesFile().getName() + ". Functionality might not work.", e);
		}
	}

}
