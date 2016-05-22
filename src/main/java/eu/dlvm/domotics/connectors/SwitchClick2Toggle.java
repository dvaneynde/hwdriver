package eu.dlvm.domotics.connectors;

import eu.dlvm.domotics.connectors.IOnOffToggleCapable.ActionType;

public class SwitchClick2Toggle extends Switch2OnOffToggle {

	public SwitchClick2Toggle(String name, String description) {
		super(name, description, null);
		map(ClickType.SINGLE, ActionType.TOGGLE);
	}

}
