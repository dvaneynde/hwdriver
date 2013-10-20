package eu.vaneynde.domotic;

import org.apache.log4j.Logger;

public class ProcessWatch {
	static Logger log = Logger.getLogger(ProcessWatch.class);

	private String threadname;
	private Process process;
	private Thread thread;
	private boolean running;
	private int exitcode;

	public ProcessWatch(Process p, String threadname) {
		this.process = p;
		this.threadname = threadname;
	}

	public void startWatching() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				running = true;
				while (running) {
					try {
						exitcode = process.waitFor();
						running = false;
					} catch (InterruptedException e) {
						log.info("subprocess stopped due to exception, thread=" + Thread.currentThread().getName(), e);
					}
				}
			}
		};
		thread = new Thread(runner, threadname);
		thread.start();
	}

	public boolean isRunning() {
		log.debug("thread.isAlive()=" + thread == null ? "null" : thread.isAlive() + ", running=" + running);
		return running;
	}

	public int getExitcode() {
		return exitcode;
	}

	@Override
	public String toString() {
		return "ProcessWatch [threadname=" + threadname + ", process=" + process + ", running=" + running + ", exitcode=" + exitcode + "]";
	}
}
