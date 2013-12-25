package eu.dlvm.domotics.actuators;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.actuators.newyear.GSstate;
import eu.dlvm.domotics.actuators.newyear.INewYearGadget;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable;

public class NewYear extends Actuator implements IOnOffToggleCapable {

	private long startTimeMs;
	private long endTimeMs;
	private boolean manualRun, manual;
	private long actualStartMs = -1;
	private List<GadgetSet> gadgetSets;
	GadgetSet lastGS;

	public class GadgetSet {
		public int startMs, endMs;
		public GSstate state = GSstate.BEFORE;
		public List<INewYearGadget> gadgets = new ArrayList<>();
		// public GadgetSet setStartTime(int ms) {
		// this.startMs = ms;
		// return this;
		// }
		//
		// public GadgetSet setEndTime(int ms) {
		// this.endMs = ms;
		// return this;
		// }
		//
		// public GadgetSet addGadget(INewYearGadget g) {
		// gadgets.add(g);
		// return this;
		// }
	}

	public NewYear(String name, long startTimeMs, long endTimeMs, IHardwareAccess ctx) {
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
		manualRun = true;
		manual = true;
	}

	public synchronized void stop() {
		manualRun = false;
		manual = true;
	}

	private synchronized boolean needToRun(long currentTime) {
		if (manual)
			return manualRun;
		else
			return (currentTime >= startTimeMs && currentTime <= endTimeMs);
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
	public void initializeOutput(RememberedOutput ro) {
	}

	@Override
	public BlockInfo getActuatorInfo() {
		return null;
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
			if (manualRun)
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
}
