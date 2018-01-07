package eu.dlvm.domotics.controllers.gadgets;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.events.EventType;

/**
 * Lamp die op bepaald moment on/off gaat, eventueel meerdere keren.
 * 
 * @author dirkv
 * 
 */
public class OnOff implements IGadget {

	private List<Actuator> actuators;
	private long startTime;
	private SortedSet<TodoEvent> entries;

	// TODO how does this work, proper name...
	public class TodoEvent implements Comparable<TodoEvent> {
		public boolean state;
		public long timeSec;

		public TodoEvent(long timeSec, boolean state) {
			this.timeSec = timeSec;
			this.state = state;
		}

		@Override
		public int compareTo(TodoEvent o) {
			if (timeSec == o.timeSec)
				return 0;
			else
				return timeSec < o.timeSec ? -1 : 1;
		}
	}

	public OnOff() {
		this.actuators = new ArrayList<>();
		startTime = -1;
		entries = new TreeSet<>();
	}

	public OnOff(Actuator actuator) {
		this();
		actuators.add(actuator);
	}

	public void add(Actuator actuator) {
		actuators.add(actuator);
	}

	public void add(TodoEvent e) {
		entries.add(e);
	}

	private TodoEvent findLastEntryBefore(long time) {
		TodoEvent lastEntry = null;
		for (TodoEvent e : entries) {
			if ((e.timeSec * 1000 + startTime) <= time)
				lastEntry = e;
			else
				break;
		}
		return lastEntry;
	}

	@Override
	public void loop2(long time, GadgetState state) {
		if (startTime < 0)
			startTime = time;

		TodoEvent e = findLastEntryBefore(time);
		if (e != null) {
			for (Actuator lamp : actuators)
				lamp.onEvent(null, e.state ? EventType.ON : EventType.OFF);
		}

	}

	@Override
	public void onBefore(long time) {
	}

	@Override
	public void onDone(long time) {
	}

}
