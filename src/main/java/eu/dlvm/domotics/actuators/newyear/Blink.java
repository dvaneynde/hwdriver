package eu.dlvm.domotics.actuators.newyear;

import eu.dlvm.domotics.actuators.Lamp;

public class Blink implements INewYearGadget {

	private Lamp lamp;
	private int freq;
	private long nextToggleTime;

	/**
	 * tijd aan of uit = min + rand() x mult
	 * 
	 * @param lamp
	 * @param min
	 *            minimale tijd aan of uit, milliseconden
	 * @param mult
	 *            gemiddelde tijd aan of uit bovenop min, ms.
	 */
	public Blink(Lamp lamp, int frequency) {
		this.lamp = lamp;
		freq = frequency;
		nextToggleTime = -1;
	}

	@Override
	public void loop2(long time, GSstate state) {
		if ((nextToggleTime < 0) || (time >= nextToggleTime)) {
			lamp.toggle();
			nextToggleTime = time + 1000 / freq / 2;
		}
	}
}
