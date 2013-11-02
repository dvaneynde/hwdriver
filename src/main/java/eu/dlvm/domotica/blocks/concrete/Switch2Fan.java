package eu.dlvm.domotica.blocks.concrete;

import eu.dlvm.domotica.blocks.concrete.IOnOffToggleListener.ActionType;

public class Switch2Fan extends Switch2OnOffToggle {

	public Switch2Fan(String name, String description) {
		super(name, description);
		map(ClickType.SINGLE,ActionType.TOGGLE);
		map(ClickType.LONG,ActionType.OFF);
	}

}
