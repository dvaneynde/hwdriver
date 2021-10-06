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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputStateSaver {

	public static final String SAVED_OUTPUTS_FILE = "DomoticOutputStates.txt";
	private static final Logger log = LoggerFactory.getLogger(OutputStateSaver.class);
	private boolean alreadyReportedFileNotFound = false;

	Map<String, RememberedOutput> readRememberedOutputs() {
		Map<String, RememberedOutput> ros = new HashMap<String, RememberedOutput>();
		File f = new File(SAVED_OUTPUTS_FILE);
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
		File f = new File(SAVED_OUTPUTS_FILE);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Actuator a : as) {
				RememberedOutput ro = a.dumpOutput();
				if (ro != null) {
					bw.write(ro.dump() + '\n');
				}
			}
			log.debug("Wrote last output state to " + f.getAbsolutePath());
		} catch (FileNotFoundException e) {
			if (!alreadyReportedFileNotFound)
				log.warn("Cannot write remembered output, file not found: " + f.getAbsolutePath());
			alreadyReportedFileNotFound = true;
		} catch (IOException e) {
			log.error("Failed writing to " + f.getName() + ". Functionality might not work.", e);
		}
	}
}
