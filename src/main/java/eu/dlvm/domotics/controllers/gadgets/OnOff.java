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
	private SortedSet<Command> commands;

	public class Command implements Comparable<Command> {
		public boolean state;
		public long timeSec;

		public Command(long timeSec, boolean state) {
			this.timeSec = timeSec;
			this.state = state;
		}

		@Override
		public int compareTo(Command o) {
			if (timeSec == o.timeSec)
				return 0;
			else
				return timeSec < o.timeSec ? -1 : 1;
		}
	}

	public OnOff() {
		this.actuators = new ArrayList<>();
		startTime = -1;
		commands = new TreeSet<>();
	}

	public OnOff(Actuator actuator) {
		this();
		actuators.add(actuator);
	}

	public void add(Actuator actuator) {
		actuators.add(actuator);
	}

	public void add(Command e) {
		commands.add(e);
	}

	private Command findLastEntryBefore(long time) {
		Command lastEntry = null;
		for (Command e : commands) {
			if ((e.timeSec * 1000 + startTime) <= time)
				lastEntry = e;
			else
				break;
		}
		return lastEntry;
	}

	@Override
	public void onBusy(long time) {
		if (startTime < 0)
			startTime = time;

		Command e = findLastEntryBefore(time);
		if (e != null) {
			for (Actuator lamp : actuators)
				lamp.onEvent(null, e.state ? EventType.ON : EventType.OFF);
		}

	}

	@Override
	public void onBefore() {
	}

	@Override
	public void onDone() {
	}

}
