package eu.dlvm.domotics.events;

import eu.dlvm.domotics.base.Block;

/**
 * Something with a name that listens for events.
 * 
 * @author dirk
 */
public interface IEventListener {
	public String getName();
	public void onEvent(Block source, EventType event);
}
