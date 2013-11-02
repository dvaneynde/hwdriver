package eu.dlvm.domotica.blocks.concrete;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.concrete.IOnOffToggleListener.ActionType;

/**
 * Keeps one or more mappings, each mapping maps one incoming
 * {@link ISwitchListener.ClickType} event to one
 * {@link IOnOffToggleListener.ActionType} event. Whenever an event comes in, the
 * corresponding event is sent to all listeners.
 * 
 * @author dirkv
 * 
 */
public class Switch2OnOffToggle extends Block implements ISwitchListener {

	static Logger log = Logger.getLogger(Switch2OnOffToggle.class);

	private Map<ClickType, IOnOffToggleListener.ActionType> mappings;
	private Set<IOnOffToggleListener> listeners = new HashSet<>();
	
	public Switch2OnOffToggle(String name, String description) {
		super(name, description);
		mappings = new HashMap<>(3);
	}

	public void map(ClickType click, IOnOffToggleListener.ActionType action) {
		mappings.put(click, action);
	}
	
	public void registerListener(IOnOffToggleListener listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners(ActionType action) {
		for (IOnOffToggleListener l:listeners)
			l.onEvent(this, action);
	}
	
	@Override
	public void onEvent(Switch source, ClickType click) {
		IOnOffToggleListener.ActionType action = mappings.get(click);
		if (action == null) {
			log.error("Received unexpected event. This is a configuration error. Event received=" + click + " from switch=" + source);
			return;
		}
		notifyListeners(action);
	}
}
