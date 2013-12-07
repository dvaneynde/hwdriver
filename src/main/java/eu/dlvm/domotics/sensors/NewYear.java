package eu.dlvm.domotics.sensors;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.sensors.newyear.INewYearGadget;

public class NewYear extends Sensor {

	private long startTime;
	private long endTime;
	private boolean run;
	private List<INewYearGadget> gadgets;

	public NewYear(String name, long startTime, long endTime) {
		super(name, name, null, null);
		this.startTime = startTime;
		this.endTime = endTime;
		gadgets = new ArrayList<>();
	}

	public void addGadget(INewYearGadget g) {
		gadgets.add(g);
	}

	public synchronized void start() {
		run = true;
	}

	public synchronized void stop() {
		run = false;
	}

	private synchronized boolean needToRun(long currentTime) {
		if (run)
			return true;
		else
			return (currentTime >= startTime && currentTime <= endTime);
	}

	@Override
	public void loop(long currentTime, long sequence) {
		if (needToRun(currentTime)) {
			for (INewYearGadget g : gadgets) {
				g.loop(currentTime);
			}
		}
	}
}
