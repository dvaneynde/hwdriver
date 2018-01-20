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
 * TODO test program
 * @author dirkv
 * 
 */
public class OnOff implements IGadget {

	private List<Actuator> actuators;
	private SortedSet<Command> commands;
	private Command lastCommand;

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
		commands = new TreeSet<>();
	}

	// @Deprecated
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

	private Command findCurrentEntry(long time) {
		Command currentEntry = null;
		for (Command e : commands) {
			if (time >= (e.timeSec * 1000))
				currentEntry = e;
			else
				break;
		}
		return currentEntry;
	}

	@Override
	public void onBusy(long time) {
		Command currentCommand = findCurrentEntry(time);
		if (currentCommand != lastCommand) {
			for (Actuator lamp : actuators)
				lamp.onEvent(null, currentCommand.state ? EventType.ON : EventType.OFF);
			lastCommand = currentCommand;
		}
	}

	@Override
	public void onBefore() {
		lastCommand = null;
		// TODO save state of actuators
	}

	@Override
	public void onDone() {
		// TODO restore state is waarschijnlijk iets dat gadgetset of gadgetcontroller moet doen?
	}

}
