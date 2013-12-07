package eu.dlvm.domotics.sensors.newyear;

import eu.dlvm.domotics.actuators.DimmedLamp;

public class Sinus implements INewYearGadget {
	private int cycleTime;
	private long startTime = -1;
	private DimmedLamp lamp;

	public Sinus(int cycleTime, DimmedLamp lamp) {
		this.cycleTime = cycleTime;
		this.lamp = lamp;
	}

	public double calcValue(long time) {
		if (startTime < 0L)
			return 0;
		double val = Math.sin((time - startTime) * 2.0D * Math.PI / cycleTime) / 2.0 + 0.5;
		return val;
	}

	@Override
	public void loop(long time) {
		if (startTime < 0L)
			startTime = time;
		lamp.on((int) (calcValue(time) * 100));
	}

	public static void main(String[] args) {
		Sinus s = new Sinus(2000, null);
		for (long time = 0L; time <= 2000; time += 100) {
			System.out.println(Math.round(100 * s.calcValue(time)));
		}
	}
}