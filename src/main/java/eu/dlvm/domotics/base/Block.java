package eu.dlvm.domotics.base;

import java.util.HashSet;
import java.util.Set;

import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;

/**
 * A block is a building block for anything from switches to lamps.
 * 
 * @author dirk vaneynde
 */
public abstract class Block {

	protected String name;
	protected String description;
	protected String uiGroup;
	private Set<IEventListener> listeners = new HashSet<>();

	private Block(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Block(String name, String description, String uiGroup) {
		this(name, description);
		this.uiGroup = uiGroup;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUiGroup() {
		return uiGroup;
	}

	public void registerListener(IEventListener listener) {
		listeners.add(listener);
	}

	protected void notifyListeners(EventType event) {
		for (IEventListener listener : listeners) {
			listener.onEvent(this, event);
		}
	}

	@Override
	public String toString() {
		return "Block [name=" + name + ", description=" + description + ", uiGroup=" + uiGroup + ", listeners="
				+ listeners + "]";
	}

}
