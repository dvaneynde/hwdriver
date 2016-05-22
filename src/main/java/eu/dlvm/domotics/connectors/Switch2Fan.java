package eu.dlvm.domotics.connectors;

import eu.dlvm.domotics.connectors.IOnOffToggleCapable.ActionType;

public class Switch2Fan extends Switch2OnOffToggle {

	public Switch2Fan(String name, String description) {
		super(name, description, null);
		map(ClickType.SINGLE,ActionType.TOGGLE);
		map(ClickType.LONG,ActionType.OFF);
	}

}
