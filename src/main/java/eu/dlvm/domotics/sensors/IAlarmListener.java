package eu.dlvm.domotics.sensors;

import eu.dlvm.domotics.base.Sensor;


public interface IAlarmListener {

	public static enum EventType {
		ALARM, SAFE;
	}

	public void onEvent(Sensor source, EventType event);
	
}
