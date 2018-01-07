package eu.dlvm.domotics.controllers.gadgets;

import eu.dlvm.domotics.actuators.Lamp;

public class RandomOnOff implements IGadget {

	private Lamp lamp;
	private int minMs;
	private int multMs;
	private long nextToggleTime;
	
	/**
	 * tijd aan of uit = min + rand() x mult
	 * @param lamp
	 * @param minTimeOnOffMs minimale tijd aan of uit, milliseconden
	 * @param randomMultiplierMs gemiddelde tijd aan of uit bovenop min, ms.
	 */
	public RandomOnOff(Lamp lamp, int minTimeOnOffMs, int randomMultiplierMs ) {
		this.lamp = lamp;
		minMs = minTimeOnOffMs;
		multMs = randomMultiplierMs;
		nextToggleTime = -1;
	}

	@Override
	public void loop2(long time, GadgetState gs) {
		if ((nextToggleTime < 0) || (time >= nextToggleTime)) {
			lamp.toggle();
			int rand = (int)(Math.random() * multMs + minMs);
			nextToggleTime = time + rand;	
		}
	}
	
	@Override
	public void onBefore(long time) {
	}

	@Override
	public void onDone(long time) {
	}

}
