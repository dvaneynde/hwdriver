package eu.dlvm.domotics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO move to base, or move base/* to here?

public class ProcessWatch {
	static Logger log = LoggerFactory.getLogger(ProcessWatch.class);

	private String threadname;
	private Process process;
	private String pid;
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
			pid = getPidOfProcess(psName);
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
		if (log.isDebugEnabled())
			log.debug("getPidOfProcess(), search pid of process with name=" + name);
		//ProcessBuilder pb = new ProcessBuilder("/bin/ps", "-C ", name, "-o", "pid", "--noheading");
		ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c", "ps -C "+name+" -o pid --noheading");
		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String pid = "";
		char[] cbuf = new char[64];
		int len;
		while ((len = br.read(cbuf, 0, cbuf.length)) != -1) {
			if (log.isDebugEnabled())
				log.debug("getPidOfProcess(), line=" + new String(cbuf,0, len));
			pid += new String(cbuf,0, len);
		}
		if (log.isDebugEnabled())
			log.debug("getPidOfProcess(), found pid=" + pid);
		return pid;
	}

	public String getPid() {
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
