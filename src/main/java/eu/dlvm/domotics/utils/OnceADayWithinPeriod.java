package eu.dlvm.domotics.utils;

import java.util.Calendar;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.Domotic;

public class OnceADayWithinPeriod extends OnceADay {

	private static Logger log = Logger.getLogger(OnceADayWithinPeriod.class);
	private int winStartHour, winStartMinutes, winEndHour, winEndMinutes;

	public OnceADayWithinPeriod(int winStartHour, int winStartMinutes, int winEndHours, int winEndMinutes) {
		this.winStartHour = winStartHour;
		this.winStartMinutes = winStartMinutes;
		this.winEndHour = winEndHours;
		this.winEndMinutes = winEndMinutes;
	}

	public boolean canCheckForToday(long currentTime) {
		// check time limits; if later then doneForToday=true
		Calendar c0 = createCalendarHourMinute(currentTime, winStartHour, winStartMinutes);
		Calendar c1 = createCalendarHourMinute(currentTime, winEndHour, winEndMinutes);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		return !(c.before(c0) || c.after(c1));
	}

	@Override
	public void markDoneForToday(long currentTime) {
		if (!canCheckForToday(currentTime)) {
			log.error("markDoneForToday() called outside time window. Ignored.");
			return;
		}
		super.markDoneForToday(currentTime);
	}
	
	private Calendar createCalendarHourMinute(long currentTime, int winHour, int winMinutes) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		c.set(Calendar.HOUR, winHour);
		c.set(Calendar.MINUTE, winMinutes);
		return c;
	}
}
