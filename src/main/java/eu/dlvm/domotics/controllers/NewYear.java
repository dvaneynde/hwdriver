package eu.dlvm.domotics.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.controllers.newyear.GSstate;
import eu.dlvm.domotics.controllers.newyear.INewYearGadget;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable;

public class NewYear extends Controller implements IOnOffToggleCapable {

	static Logger LOG = Logger.getLogger(NewYear.class);
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
		super(name, name, null, null, ctx);
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
		LOG.info(getName() + " started.");
		actualStartMs = -1;
		running = true;
		manual = true;
	}

	public synchronized void stop() {
		LOG.info(getName() + " stopped.");
		running = false;
		manual = true;
	}

	private synchronized boolean needToRun(long currentTime) {
		if (!manual)
			running = (currentTime >= startTimeMs && currentTime <= endTimeMs);
		return running;
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
	public void onEvent(ActionType action) {
		switch (action) {
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
		}
	}

	public long getStartTimeMs() {
		return startTimeMs;
	}

	public long getEndTimeMs() {
		return endTimeMs;
	}

	@Override
	public void on() {
		onEvent(ActionType.ON);
	}

	@Override
	public void off() {
		onEvent(ActionType.OFF);
	}

	@Override
	public boolean toggle() {
		onEvent(ActionType.TOGGLE);
		// TODO niet correct... return false
		return false;
	}

	@Override
	public BlockInfo getBlockInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(String action) {
		// TODO Auto-generated method stub
		
	}

}
