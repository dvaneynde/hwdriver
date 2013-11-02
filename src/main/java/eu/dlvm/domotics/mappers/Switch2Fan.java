package eu.dlvm.domotics.mappers;

import eu.dlvm.domotics.mappers.IOnOffToggleListener.ActionType;

public class Switch2Fan extends Switch2OnOffToggle {

	public Switch2Fan(String name, String description) {
		super(name, description);
		map(ClickType.SINGLE,ActionType.TOGGLE);
		map(ClickType.LONG,ActionType.OFF);
	}

}
