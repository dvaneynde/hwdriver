package eu.dlvm.domotics.controllers;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.events.EventType;

/**
 * Has two per-day times, at {@link #setOnTime(int, int)} an on signal is sent,
 * at {@link #setOffTime(int, int)} an off signal is sent.
 * <p>
 * Targets {@link IOnOffToggleCapable} listeners.
 * 
 * @author dirk
 */
public class Timer extends Controller {

	private static Logger logger = LoggerFactory.getLogger(Timer.class);
	protected int onTime, offTime;
	protected boolean state;

	/**
	 * ???
	 */
	public static int timeInDayMillis(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		int timeInDay = timeInDayMillis(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		return timeInDay;
	}

	/**
	 * 
	 * @param hours
	 * @param minutes
	 * @return time in ms since midnight
	 */
	public static int timeInDayMillis(int hours, int minutes) {
		return ((hours * 60) + minutes) * 60000;
	}

	public static int[] hourMinute(long time) {
		time /= 60000;
		int minute = (int) time % 60;
		int hour = (int) time / 60;
		return new int[] { hour, minute };
	}

	public static long getTimeMsSameDayAtHourMinute(long basetime, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(basetime);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	// timer usage interface
	public Timer(String name, String description, IDomoticContext ctx) {
		super(name, description, null, ctx);
		state = false;
		onTime = offTime = 0;
	}

	public void setOnTime(int hour, int minute) {
		onTime = timeInDayMillis(hour, minute);
	}

	public void setOffTime(int hour, int minute) {
		offTime = timeInDayMillis(hour, minute);
	}

	public boolean isOn() {
		return state;
	}
	
	public String getOnTimeAsString() {
		int[] times = hourMinute(onTime);
		return String.format("%02d:%02d", times[0], times[1]);
	}

	public String getOffTimeAsString() {
		int[] times = hourMinute(offTime);
		return String.format("%02d:%02d", times[0], times[1]);
	}

	@Override
	public void onEvent(Block source, EventType event) {
	}


	@Override
	public void loop(long currentTime, long sequence) {
		long currentTimeInDay = timeInDayMillis(currentTime);
		boolean state2 = state;
		if (onTime <= offTime) {
			state2 = (currentTimeInDay > onTime && currentTimeInDay < offTime);
		} else {
			state2 = !(currentTimeInDay > offTime && currentTimeInDay < onTime);
		}
		if (state2 != state) {
			state = state2;
			logger.info("Timer '" + getName() + "' sends event '" + (state ? "ON" : "OFF") + "'");
			notifyListeners(state ? EventType.ON : EventType.OFF);
		}
	}

}
