package eu.dlvm.domotics.actuators;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.actuators.newyear.INewYearGadget;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.mappers.IOnOffToggleListener;

public class NewYear extends Actuator implements IOnOffToggleListener {

	private long startTimeMs;
	private long endTimeMs;
	private boolean manualRun, manual;
	private List<INewYearGadget> gadgets;
	// TODO manual zou na 1 dag moeten reset worden ?

	public NewYear(String name, long startTimeMs, long endTimeMs, IHardwareAccess ctx) {
		super(name, name, null, ctx);
		this.startTimeMs = startTimeMs;
		this.endTimeMs = endTimeMs;
		gadgets = new ArrayList<>();
	}

	public void addGadget(INewYearGadget g) {
		gadgets.add(g);
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
			for (INewYearGadget g : gadgets) {
				g.loop(currentTime);
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
	public void onEvent(Block source, ActionType action) {
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
}
