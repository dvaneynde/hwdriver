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
	private int durationMs;
	private List<IGadget> gadgets = new ArrayList<>();

	public GadgetSet(int durationMs) {
		this.durationMs = durationMs;
	}
	
	public int getDurationMs() {
		return durationMs;
	}

	public List<IGadget> getGadgets() {
		return gadgets;
	}

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
