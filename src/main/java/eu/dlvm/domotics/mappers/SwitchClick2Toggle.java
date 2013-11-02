package eu.dlvm.domotics.mappers;

import eu.dlvm.domotics.mappers.IOnOffToggleListener.ActionType;

public class SwitchClick2Toggle extends Switch2OnOffToggle {

	public SwitchClick2Toggle(String name, String description) {
		super(name, description);
		map(ClickType.SINGLE, ActionType.TOGGLE);
	}

}
