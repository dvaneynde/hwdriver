package eu.dlvm.domotics.controllers.gadgets;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of {@link IGadget} that when started should take no longer than {@link #durationMs}
 * time.
 * 
 * @author dirk
 *
 */
public class GadgetSet {
	public int durationMs;
	//public GadgetState state = GadgetState.BEFORE;
	public List<IGadget> gadgets = new ArrayList<>();

	public void onBefore() {
		for (IGadget g : gadgets)
			g.onBefore();
	}
	public void onBusy(long relativeTimeGadgetSet) {
		for (IGadget g : gadgets)
			g.onBusy(relativeTimeGadgetSet);
	}
	public void onDone() {
		for (IGadget g : gadgets)
			g.onDone();
	}
}
