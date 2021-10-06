package eu.dlvm.domotics.controllers.gadgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.Lamp;

public class RandomOnOff implements IGadget {

	private static Logger logger = LoggerFactory.getLogger(RandomOnOff.class);
	
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
	public void onBusy(long time) {
		if ((nextToggleTime < 0) || (time >= nextToggleTime)) {
			logger.info("toggling lamp "+lamp.getName());
			lamp.toggle();
			int rand = (int)(Math.random() * multMs + minMs);
			nextToggleTime = time + rand;	
		}
	}
	
	@Override
	public void onBefore() {
	}

	@Override
	public void onDone() {
	}

}
