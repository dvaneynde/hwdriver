package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

public class Oscillator {

	static Logger log = Logger.getLogger(Oscillator.class);
	
	private Domotic dom;
	private long tickTimeMs;
	private boolean goOn;
	
	public Oscillator(Domotic dom, long tickTimeMs) {
		this.dom = dom;
		this.tickTimeMs = tickTimeMs;
	}
	
	public void go() {
		boolean localGoOn;
		synchronized (this) {
			goOn = true;
			localGoOn = goOn;
		}
		while (localGoOn) {
			long currentTime = System.currentTimeMillis();
			dom.loopOnce(currentTime);
			try {
				Thread.sleep(tickTimeMs);
			} catch (InterruptedException e) {
			}
			synchronized (this) {
				localGoOn = goOn;
			}
		}
	}
	
	public synchronized void stop() {
		goOn = false;
	}
	
	public synchronized boolean stopRequested() {
		return !goOn;
	}
}
