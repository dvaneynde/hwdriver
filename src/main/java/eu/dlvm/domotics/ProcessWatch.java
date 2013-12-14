package eu.dlvm.domotics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class ProcessWatch {
	static Logger log = Logger.getLogger(ProcessWatch.class);

	private String threadname;
	private Process process;
	private String psName;
	private Thread thread;
	private boolean running;
	private int exitcode;

	public ProcessWatch(Process p, String threadname, String psName) {
		this.process = p;
		this.threadname = threadname;
		this.psName = psName;
	}

	public void startWatching() {
		try {
			String pid = getPidOfProcess(psName);
			log.info("PID of process " + psName + " is " + pid);
		} catch (IOException e) {
			log.warn("Could not get pid of " + psName + " process. Ignored, will continue.", e);
		}
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				running = true;
				while (running) {
					try {
						exitcode = process.waitFor();
						running = false;
					} catch (InterruptedException e) {
						log.info("process.waitFor() interrupted, will continue. Thread=" + Thread.currentThread().getName(), e);
					}
				}
			}
		};
		thread = new Thread(runner, threadname);
		thread.start();
	}

	private String getPidOfProcess(String name) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("ps -C " + name + " -o pid --noheading");
		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String pid = br.readLine();
		return pid;
	}

	public boolean isRunning() {
		log.debug("thread.isAlive()=" + thread == null ? "null" : thread.isAlive() + ", running=" + running);
		return running;
	}

	public int getExitcode() {
		return exitcode;
	}

	@SuppressWarnings("deprecation")
	public void terminate() {
		thread.stop();
	}

	@Override
	public String toString() {
		return "ProcessWatch [threadname=" + threadname + ", process=" + process + ", running=" + running + ", exitcode=" + exitcode + "]";
	}
}
