package eu.dlvm.domotics.mappers;

import eu.dlvm.domotics.base.Block;

public interface IOnOffToggleListener {

	public enum ActionType {
		ON, OFF, TOGGLE;
	}

	public void onEvent(Block source, ActionType action);
}
