package eu.dlvm.domotics.base;

/**
 * Connectors are very simple, they listen for events, send events somewhere
 * else, might map events.
 * <p>
 * Connectors do <b>not</b> have state, nor do they loop - they only react to
 * events.
 * 
 * @author dirk
 * 
 */
public class Connector extends Block {

	public Connector(String name, String description, String ui) {
		super(name, description, ui);
	}

}
