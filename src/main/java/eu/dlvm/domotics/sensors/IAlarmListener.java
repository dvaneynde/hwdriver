package eu.dlvm.domotics.sensors;

import eu.dlvm.domotics.base.Block;

@Deprecated
public interface IAlarmListener {

	public static enum EventType {
		ALARM, SAFE;
	}

	public void onEvent(Block source, EventType event);
	
}
