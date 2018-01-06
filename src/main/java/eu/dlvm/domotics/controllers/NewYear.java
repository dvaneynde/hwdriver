package eu.dlvm.domotics.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.controllers.gadgets.GSstate;
import eu.dlvm.domotics.controllers.gadgets.IGadget;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

public class NewYear extends Controller implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(NewYear.class);
	private long startTimeMs;
	private long endTimeMs;
	private boolean running, manual;
	private long actualStartMs = -1;
	private List<GadgetSet> gadgetSets;
	GadgetSet lastGadgetSet;

	public class GadgetSet {
		public int startMs, endMs;
		public GSstate state = GSstate.BEFORE;
		public List<IGadget> gadgets = new ArrayList<>();
	}

	public NewYear(String name, long startTimeMs, long endTimeMs, IDomoticContext ctx) {
		super(name, name, null, ctx);
		this.startTimeMs = startTimeMs;
		this.endTimeMs = endTimeMs;
		gadgetSets = new ArrayList<>();
	}

	public void addEntry(GadgetSet e) {
		gadgetSets.add(e);
	}

	public GadgetSet selectEntry(long msSinceStart) {
		for (GadgetSet e : gadgetSets) {
			if (e.startMs <= msSinceStart && e.endMs >= msSinceStart)
				return e;
		}
		return null;
	}

	public synchronized void start() {
		logger.info(getName() + " started.");
		actualStartMs = -1;
		running = true;
		manual = true;
	}

	public synchronized void stop() {
		logger.info(getName() + " stopped.");
		running = false;
		manual = true;
	}

	private synchronized boolean needToRun(long currentTime) {
		if (!manual)
			running = (currentTime >= startTimeMs && currentTime <= endTimeMs);
		return running;
	}

	public void on() {
		onEvent(this, EventType.ON);
	}

	public void off() {
		onEvent(this, EventType.OFF);
	}

	public boolean toggle() {
		onEvent(this, EventType.TOGGLE);
		return running;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			start();
			break;
		case OFF:
			stop();
			break;
		case TOGGLE:
			if (running)
				stop();
			else
				start();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	public long getStartTimeMs() {
		return startTimeMs;
	}

	public long getEndTimeMs() {
		return endTimeMs;
	}



	@Override
	public void loop(long currentTime, long sequence) {
		if (needToRun(currentTime)) {
			if (actualStartMs == -1)
				actualStartMs = currentTime;
			GadgetSet gadgetSet = selectEntry(currentTime - actualStartMs);
			if (gadgetSet != lastGadgetSet && lastGadgetSet != null) {
				// Changed set, and not first one.
				if (lastGadgetSet.state == GSstate.BUSY) {
					lastGadgetSet.state = GSstate.LAST;
					for (IGadget g : lastGadgetSet.gadgets)
						g.loop2(currentTime - actualStartMs - gadgetSet.startMs, lastGadgetSet.state);
				} else
					lastGadgetSet.state = GSstate.DONE;
			}
			if (gadgetSet != null) {
				if (gadgetSet.state == GSstate.BEFORE)
					gadgetSet.state = GSstate.FIRST;
				else if (gadgetSet.state == GSstate.FIRST)
					gadgetSet.state = GSstate.BUSY;
				for (IGadget g : gadgetSet.gadgets)
					g.loop2(currentTime - actualStartMs - gadgetSet.startMs, gadgetSet.state);
			}
		}
	}

	@Override
	public UiInfo getUiInfo() {
		return null;
	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on NewYear '" + getName() + "' got unsupported action '" + action + ".");
	}

}
