package eu.dlvm.domotics.controllers.gadgets;

import eu.dlvm.domotics.actuators.Lamp;

public class Blink implements IGadget {

	private Lamp lamp;
	private int freq;
	private long nextToggleTime;

	/**
	 * 
	 * @param lamp
	 * @param frequency
	 */
	public Blink(Lamp lamp, int frequency) {
		this.lamp = lamp;
		freq = frequency;
		nextToggleTime = -1;
	}

	@Override
	public void loop2(long time, GadgetState state) {
		if ((nextToggleTime < 0) || (time >= nextToggleTime)) {
			lamp.toggle();
			nextToggleTime = time + 1000 / freq / 2;
		}
	}

	@Override
	public void onBefore(long time) {
	}

	@Override
	public void onDone(long time) {
	}
}
