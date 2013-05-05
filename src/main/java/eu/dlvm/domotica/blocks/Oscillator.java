package eu.dlvm.domotica.blocks;

public class Oscillator {

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (this) {
				localGoOn = goOn;
			}
		}
	}
	
	public synchronized void stop() {
		goOn = false;
	}
}
