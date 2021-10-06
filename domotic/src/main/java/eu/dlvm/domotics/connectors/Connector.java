package eu.dlvm.domotics.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;

/**
 * For now just one mapping.
 * <p>
 * Connectors are very simple, they listen for events, map them to another
 * srcEvent and send it. Connectors do <b>not</b> have state, nor do they loop -
 * they only react to events.
 * 
 * @author dirk
 * 
 */
public class Connector implements IEventListener {

	private static final Logger logger = LoggerFactory.getLogger(Connector.class);

	public Connector(EventType fromEvent, IEventListener to, EventType toEvent, String debugSrcName) {
		this.fromEvent = fromEvent;
		this.to = to;
		this.toEvent = toEvent;
		this.name = debugSrcName + "_" + fromEvent.getAlias() + "_to_" + to.getName() + "_" + toEvent.getAlias();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		if (event.equals(fromEvent)) {
			to.onEvent(source, toEvent);
			if (logger.isDebugEnabled())
				logger.debug("Transferred srcEvent " + event + " from " + source.getName() + " as " + toEvent + " on " + to.getName() + " (connector name="
						+ name + ").");
		}
	}

	// ============ Implementation

	private String name;
	private EventType fromEvent, toEvent;
	private IEventListener to;

}
