package eu.dlvm.domotics.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.controllers.newyear.GSstate;
import eu.dlvm.domotics.controllers.newyear.INewYearGadget;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.UiInfo;

public class NewYear extends Controller implements IEventListener {

	static Logger logger = LoggerFactory.getLogger(NewYear.class);
	private long startTimeMs;
	private long endTimeMs;
	private boolean running, manual;
	private long actualStartMs = -1;
	private List<GadgetSet> gadgetSets;
	GadgetSet lastGS;

	public class GadgetSet {
		public int startMs, endMs;
		public GSstate state = GSstate.BEFORE;
		public List<INewYearGadget> gadgets = new ArrayList<>();
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
			GadgetSet e = selectEntry(currentTime - actualStartMs);
			if (e != lastGS && lastGS != null) {
				if (lastGS.state == GSstate.BUSY) {
					lastGS.state = GSstate.LAST;
					for (INewYearGadget g : lastGS.gadgets)
						g.loop2(currentTime - actualStartMs - e.startMs, lastGS.state);
				} else
					lastGS.state = GSstate.DONE;
			}
			if (e != null) {
				if (e.state == GSstate.BEFORE)
					e.state = GSstate.FIRST;
				else if (e.state == GSstate.FIRST)
					e.state = GSstate.BUSY;
				for (INewYearGadget g : e.gadgets)
					g.loop2(currentTime - actualStartMs - e.startMs, e.state);
			}
		}
	}

	@Override
	public UiInfo getUiInfo() {
		return null;
	}

	@Override
	public void update(String action) {
	}

}
