package eu.dlvm.domotics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO move to eu.dlvm.domotics.processwatch

public class ProcessReader {
	static Logger log = LoggerFactory.getLogger(ProcessReader.class);
	static Logger logDriver = LoggerFactory.getLogger("DRIVER");

	private String threadname;
	private InputStream is;
	private boolean driverStarted;
	private boolean running;
	private boolean driverReady;
	private Thread thread;

	public ProcessReader(InputStream is, String threadname) {
		this.is = is;
		this.threadname = threadname;
	}

	public boolean isDriverStarted() {
		return driverStarted;
	}

	public void startReading() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				log.info("Started process-reader. Checker driver logger for a 'started' message too.");
				logDriver.info("Started process-reader.");
				running = true;
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String line;
					while ((line = br.readLine()) != null) {
						logDriver.info(line);
						if (line.contains("HwDriver started"))
							driverReady = true;
						driverStarted = true;
					}
				} catch (IOException e) {
					log.error("driver subprocess reader stopped due to exception, thread=" + Thread.currentThread().getName(), e);
				} finally {
					running = false;
				}
			}
		};

		thread = new Thread(runner, threadname);
		thread.start();
	}

	public boolean driverNotReady() {
		return !driverReady;
	}

	public boolean isRunning() {
		log.debug("thread.isAlive()=" + thread == null ? "null" : thread.isAlive() + ", running=" + running);
		return running;
	}

	@SuppressWarnings("deprecation")
	public void terminate() {
		thread.stop();
	}

	@Override
	public String toString() {
		return "ProcessReader [threadname=" + threadname + ", driverStarted=" + driverStarted + ", running=" + running + "]";
	}
}
