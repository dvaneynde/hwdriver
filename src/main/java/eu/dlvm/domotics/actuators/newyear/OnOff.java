package eu.dlvm.domotics.actuators.newyear;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.dlvm.domotics.mappers.IOnOffToggleCapable;

/**
 * Lamp die op bepaald moment on/off gaat, eventueel meerdere keren.
 * 
 * @author dirkv
 * 
 */
public class OnOff implements INewYearGadget {

	private List<IOnOffToggleCapable> lamps;
	private long startTime;
	private SortedSet<Event> entries;

	public class Event implements Comparable<Event> {
		public boolean state;
		public long timeSec;
		public Event(long timeSec, boolean state) {
			this.timeSec = timeSec; this.state = state;
		}
		@Override
		public int compareTo(Event o) {
			if (timeSec == o.timeSec)
				return 0;
			else
				return timeSec < o.timeSec ? -1 : 1;
		}
	}

	public OnOff() {
		this.lamps = new ArrayList<>();
		startTime = -1;
		entries = new TreeSet<>();
	}

	public OnOff(IOnOffToggleCapable lamp) {
		this();
		lamps.add(lamp);
	}

	public void add(IOnOffToggleCapable oot) {
		lamps.add(oot);
	}

	public void add(Event e) {
		entries.add(e);
	}

	private Event findLastEntryBefore(long time) {
		Event lastEntry = null;
		for (Event e : entries) {
			if ((e.timeSec * 1000 + startTime) <= time)
				lastEntry = e;
			else
				break;
		}
		return lastEntry;
	}

	@Override
	public void loop2(long time, GSstate state) {
		if (startTime < 0)
			startTime = time;

		Event e = findLastEntryBefore(time);
		if (e != null) {
			for (IOnOffToggleCapable lamp : lamps)
				lamp.onEvent(e.state ? IOnOffToggleCapable.ActionType.ON : IOnOffToggleCapable.ActionType.OFF);
		}

	}
}
