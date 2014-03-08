package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

public class Oscillator extends Thread {

	static Logger log = Logger.getLogger(Oscillator.class);
	
	private Domotic dom;
	private long tickTimeMs;
	private boolean goOn;
	
	public Oscillator(Domotic dom, long tickTimeMs) {
		super("DomoticOscillator.");
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
	
	public synchronized void requestStop() {
		goOn = false;
	}
	
	public synchronized boolean stopRequested() {
		return !goOn;
	}
	
	@Override
	public void run() {
		try {
			log.info("Oscillator oscillates...");
			go();
			if (stopRequested())
				log.info("Oscillator stops since so requested.");
			else
				log.error("Oh oh... oscillator has stopped for no apparent reason. Should not happen. Nothing done for now.");
		} catch (Exception e) {
			log.error("Oh oh... oscillator has stopped. Nothing done further, should restart or something...", e);
		}
	}

}
