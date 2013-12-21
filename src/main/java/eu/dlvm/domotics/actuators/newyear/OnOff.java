package eu.dlvm.domotics.actuators.newyear;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.dlvm.domotics.actuators.Lamp;

/**
 * Lamp die op bepaald moment on/off gaat, eventueel meerdere keren.
 * 
 * @author dirkv
 * 
 */
public class OnOff implements INewYearGadget {

	private List<Lamp> lamps;
	private long startTime;
	private SortedSet<Entry> entries;

	public class Entry implements Comparable<Entry> {
		public boolean state;
		public long timeSec;
		public Entry(long timeSec, boolean state) {
			this.timeSec = timeSec; this.state = state;
		}
		@Override
		public int compareTo(Entry o) {
			if (timeSec == o.timeSec)
				return 0;
			else
				return timeSec < o.timeSec ? -1 : 1;
		}
	}

	public OnOff(Lamp lamp) {
		this.lamps = new ArrayList<>();
		lamps.add(lamp);
		startTime = -1;
		entries = new TreeSet<>();
	}

	public void add(Entry e) {
		entries.add(e);
	}

	private Entry findLastEntryBefore(long time) {
		Entry lastEntry = null;
		for (Entry e : entries) {
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

		Entry e = findLastEntryBefore(time);
		if (e != null) {
			for (Lamp lamp : lamps)
				lamp.setOn(e.state);
		}

	}
}
