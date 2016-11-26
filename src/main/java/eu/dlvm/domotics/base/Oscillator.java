package eu.dlvm.domotics.base;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.utils.AverageLong;

public class Oscillator extends Thread {

	static Logger log = LoggerFactory.getLogger(Oscillator.class);
	
	private Domotic dom;
	private long tickTimeMs;
	private boolean goOn;
	private long currentTime;
	private AverageLong avgLong;	// for quality only
	
	public Oscillator(Domotic dom, long tickTimeMs) {
		super("Oscillator");
		this.dom = dom;
		this.tickTimeMs = tickTimeMs;
		this.currentTime = System.currentTimeMillis();
		this.avgLong = new AverageLong(10);
	}
	
	public long getLastCurrentTime() {
		return currentTime;
	}
	
	public void go() {
		boolean localGoOn;
		synchronized (this) {
			goOn = true;
			localGoOn = goOn;
		}
		while (localGoOn) {
			currentTime = System.currentTimeMillis();
			dom.loopOnce(currentTime);
			avgLong.add(System.currentTimeMillis() - currentTime);
			if (avgLong.enoughSamples())
				avgLong.avgAndClear();
				//logger.info("Loops took on average "+avgLong.avgAndClear()+" ms.");
			// FIXME must be tick - time-passed !!!
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
				log.error("Oh oh... oscillator has stopped for no apparent reason. Should not happen.");
		} catch (Exception e) {
			log.error("Oh oh... oscillator has stopped. Exception detail follows.", e);
		}
	}

}
