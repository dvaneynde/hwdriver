package eu.dlvm.domotics.controllers.gadgets;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of {@link IGadget} with a start and end time. Times are relative to
 * {@link GadgetController} start time, so if {@link #startMs} is 1000 then this
 * GadgetSet will start 1 second after the Controller becomes active.
 * 
 * @author dirk
 *
 */
public class GadgetSet {
	public int startMs, endMs;
	public GadgetState state = GadgetState.BEFORE;
	public List<IGadget> gadgets = new ArrayList<>();

	public boolean isActive(GadgetSet e, long msSinceStart) {
		return (e.startMs <= msSinceStart && e.endMs >= msSinceStart);
	}

	@Override
	public String toString() {
		return "GadgetSet [startMs=" + startMs + ", endMs=" + endMs + ", state=" + state + ", gadgets=" + gadgets + "]";
	}

}
