package eu.dlvm.domotica.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * TODO Beperking nu, per switch event slechts 1 uitgaand event mogelijk. Niet
 * mogelijk dus is een switch die de ene lamp aan doet en de andere uit.
 * <p>
 * Alternative to ISensorListener.
 * <p>
 * Voorbeelden:<br/>
 * inputevent==long_click --> sprayer "licht beneden", off operation<br/>
 * inputevent==single_click --> 1 switch, toggle operation
 */
public class OperationExecutor {
	static Logger log = Logger.getLogger(OperationExecutor.class);

	private Map<String, TargetsAndEvent> map = new HashMap<String, TargetsAndEvent>();

	private class TargetsAndEvent {
		String event;
		List<IMsg2Op> instances = new ArrayList<IMsg2Op>();
	}

	public void register(String inputEvent, IMsg2Op target, String targetEvent) {
		List<IMsg2Op> targets = new ArrayList<IMsg2Op>();
		register(inputEvent, targets, targetEvent);
	}

	// TODO kan weg als Sprayer dit afdekt,
	private void register(String inputEvent, List<IMsg2Op> targets,
			String targetEvent) {
		TargetsAndEvent t = map.get(inputEvent);
		if (t == null)
			t = new TargetsAndEvent();
		t.instances = targets;
		t.event = targetEvent;
		map.put(inputEvent, t);
	}

	public void send(String inputEvent, String sourceName) {
		TargetsAndEvent target = map.get(inputEvent);
		if (target == null) {
			log.warn("Received input-event '" + inputEvent
					+ "' from source '"+sourceName+"' but no target registered. Ignored.");
			return;
		}
		for (IMsg2Op iop : target.instances)
			iop.execute(target.event);
	}
}
