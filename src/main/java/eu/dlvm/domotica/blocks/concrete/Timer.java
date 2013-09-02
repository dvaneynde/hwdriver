package eu.dlvm.domotica.blocks.concrete;

import java.util.Calendar;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.Sensor;
import eu.dlvm.domotica.blocks.SensorEvent;
import eu.dlvm.iohardware.LogCh;

public class Timer extends Sensor {

	static Logger log = Logger.getLogger(Timer.class);

	private long onTime, offTime;
	private boolean state;

	public Timer(String name, String description, LogCh channel,
			IDomoContext ctx) {
		super(name, description, channel, ctx);
		state = false;
		onTime = offTime = 0L;
	}

	private static int timeInDay(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		int timeInDay = timeInDay(c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE));
		return timeInDay;
	}

	private static int timeInDay(int hour, int minute) {
		return ((hour * 60) + minute) * 60000;
	}

	public void setOnTime(int hour, int minute) {
		onTime = timeInDay(hour, minute);
	}

	public void setOffTime(int hour, int minute) {
		offTime = timeInDay(hour, minute);
	}

	public boolean getStatus() {
		return state;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		long currentTimeInDay = timeInDay(currentTime);
		boolean state2 = state;
		if (onTime <= offTime) {
			state2 = (currentTimeInDay > onTime && currentTimeInDay < offTime);
		} else {
			state2 = !(currentTimeInDay > offTime && currentTimeInDay < onTime);
		}
		if (state2 != state) {
			state = state2;
			log.info("Timer '" + getName() + "' sends event '"
					+ (state ? "ON" : "OFF") + "'");
			notifyListeners(new SensorEvent(this, getStatus()));
		}
	}
}
