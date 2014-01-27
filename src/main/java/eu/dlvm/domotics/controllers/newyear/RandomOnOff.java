package eu.dlvm.domotics.controllers.newyear;

import eu.dlvm.domotics.actuators.Lamp;

public class RandomOnOff implements INewYearGadget {

	private Lamp lamp;
	private int minMs;
	private int multMs;
	private long nextToggleTime;
	
	/**
	 * tijd aan of uit = min + rand() x mult
	 * @param lamp
	 * @param min minimale tijd aan of uit, milliseconden
	 * @param mult gemiddelde tijd aan of uit bovenop min, ms.
	 */
	public RandomOnOff(Lamp lamp, int minTimeOnOffMs, int randomMultiplierMs ) {
		this.lamp = lamp;
		minMs = minTimeOnOffMs;
		multMs = randomMultiplierMs;
		nextToggleTime = -1;
	}

	@Override
	public void loop2(long time, GSstate gs) {
		if ((nextToggleTime < 0) || (time >= nextToggleTime)) {
			lamp.toggle();
			int rand = (int)(Math.random() * multMs + minMs);
			nextToggleTime = time + rand;	
		}
	}
}
