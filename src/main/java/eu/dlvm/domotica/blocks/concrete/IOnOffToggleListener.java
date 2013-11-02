package eu.dlvm.domotica.blocks.concrete;

import eu.dlvm.domotica.blocks.Block;

public interface IOnOffToggleListener {

	public enum ActionType {
		ON, OFF, TOGGLE;
	}

	public void onEvent(Block source, ActionType action);
}
