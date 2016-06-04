package eu.dlvm.domotics.sensors;

import eu.dlvm.domotics.base.Sensor;

public interface IThresholdListener {

	public static enum EventType {
		HIGH, LOW;
	}

	public void onEvent(Sensor source, EventType event);
	
}
