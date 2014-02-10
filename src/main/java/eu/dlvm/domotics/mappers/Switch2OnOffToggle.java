package eu.dlvm.domotics.mappers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable.ActionType;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;

/**
 * Keeps one or more mappings, each mapping maps one incoming
 * {@link ISwitchListener.ClickType} event to one
 * {@link IOnOffToggleCapable.ActionType} event. Whenever an event comes in, the
 * corresponding event is sent to all listeners.
 * 
 * @author dirkv
 * 
 */
public class Switch2OnOffToggle extends Block implements ISwitchListener {

	static Logger log = Logger.getLogger(Switch2OnOffToggle.class);

	private Map<ClickType, IOnOffToggleCapable.ActionType> mappings;
	private Set<IOnOffToggleCapable> listeners = new HashSet<>();
	
	public Switch2OnOffToggle(String name, String description) {
		super(name, description, null);
		mappings = new HashMap<>(3);
	}

	public void map(ClickType click, IOnOffToggleCapable.ActionType action) {
		mappings.put(click, action);
	}
	
	public void registerListener(IOnOffToggleCapable listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners(ActionType action) {
		for (IOnOffToggleCapable l:listeners)
			l.onEvent(action);
	}
	
	@Override
	public void onEvent(Switch source, ClickType click) {
		IOnOffToggleCapable.ActionType action = mappings.get(click);
		if (action == null) {
			log.debug("Received event for which no action is registered. Name="+getName()+", event received=" + click + ", from switch=" + source);
			return;
		}
		notifyListeners(action);
	}
}
