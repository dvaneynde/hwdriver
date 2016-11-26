package eu.dlvm.domotics.events;

import eu.dlvm.domotics.base.Block;

/**
 * 
 * @author dirk
 *
 */
public interface IEventListener {
	public void onEvent(Block source, EventType event);
}
