package eu.dlvm.domotics.controllers;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.events.EventType;

/**
 * Has two per-day times, at {@link #setOnTime(int, int)} an ON event is sent,
 * at {@link #setOffTime(int, int)} an OFF event is sent.
 *
 * @author dirk
 */
public class Timer extends Controller {

	private static Logger logger = LoggerFactory.getLogger(Timer.class);

	protected int onTimeHours, onTimeMinutes, offTimeHours, offTimeMinutes;

	// time in ms since midnight
	protected int onTimeMs, offTimeMs;
	protected boolean state;

	private static boolean loggedOnce = false;

	/**
	 * @param basetime
	 * @return aantal ms. sinds begin van de dag
	 */
	public static int timeInDayMillis(long basetime) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(basetime);
		if (!loggedOnce) {
			loggedOnce = true;
			logger.info("timeInDayMillis: timezone=" + c.getTimeZone().getDisplayName());
		}
		int timeInDay = timeInDayMillis(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		return timeInDay;
	}

	/**
	 * @param hours
	 *            in the day
	 * @param minutes
	 *            in the hour
	 * @return time in ms since midnight
	 */
	public static int timeInDayMillis(int hours, int minutes) {
		return ((hours * 60) + minutes) * 60 * 1000;
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
	public Timer(String name, String description, IDomoticBuilder ctx) {
		super(name, description, null, ctx);
		state = false;
		onTimeMs = offTimeMs = 0;
	}

	public void setOnTime(int hoursOfDay, int minutesInHour) {
		onTimeHours = hoursOfDay;
		onTimeMinutes = minutesInHour;
		onTimeMs = timeInDayMillis(hoursOfDay, minutesInHour);
	}

	public void setOffTime(int hoursOfDay, int minutesInHour) {
		offTimeHours = hoursOfDay;
		offTimeMinutes = minutesInHour;
		offTimeMs = timeInDayMillis(hoursOfDay, minutesInHour);
	}

	public boolean isOn() {
		return state;
	}

	public String getHoursMinutesInDayAsString(long time) {
		int[] times = hourMinute(time);
		return String.format("%02d:%02d", times[0], times[1]);
	}

	public String getOnTimeAsString() {
		// int[] times = hourMinute(onTimeMs);
		return String.format("%02d:%02d", onTimeHours, onTimeMinutes);
	}

	public String getOffTimeAsString() {
		// int[] times = hourMinute(offTimeMs);
		return String.format("%02d:%02d", offTimeHours, offTimeMinutes);
	}

	@Override
	public void onEvent(Block source, EventType event) {
	}

	@Override
	public void loop(long currentTime) {
		long currentTimeInDay = timeInDayMillis(currentTime);
		boolean state2;
		boolean onTimeBeforeOffTime = (onTimeMs <= offTimeMs);
		if (onTimeBeforeOffTime) {
			state2 = (currentTimeInDay > onTimeMs && currentTimeInDay < offTimeMs);
		} else {
			state2 = !(currentTimeInDay > offTimeMs && currentTimeInDay < onTimeMs);
		}
		if (state2 != state) {
			state = state2;
			logger.info("Timer '" + getName() + "' sends event '" + (state ? "ON" : "OFF") + "' because current:"
					+ getHoursMinutesInDayAsString(currentTime) + ", on:" + getOnTimeAsString() + ", off:"
					+ getOffTimeAsString());
			notifyListeners(state ? EventType.ON : EventType.OFF);
		}
	}

	@Override
	public String toString() {
		return "Timer on:" + getOnTimeAsString() + " off:" + getOffTimeAsString() + " state=" + state + "]";
	}
}
