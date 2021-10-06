package eu.dlvm.domotics.utils;

import java.util.Calendar;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

public class OnceADayWithinPeriod extends OnceADay {

	private static Logger log = LoggerFactory.getLogger(OnceADayWithinPeriod.class);
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
//		System.out.println("c0="+c0.getTime()+"\n c="+c.getTime()+"\nc1="+c1.getTime());
//		System.out.println("c before c0="+c.before(c0));
//		System.out.println("c after c1="+c.after(c1));
		boolean canCheck = !(c.before(c0) || c.after(c1));
		if (log.isDebugEnabled())
			log.debug("can check=" + canCheck + ", currentTime=" + currentTime + ", this=" + toString());
		return canCheck;
	}

	@Override
	public void markDoneForToday() {
		super.markDoneForToday();
	}

	private Calendar createCalendarHourMinute(long currentTime, int winHour, int winMinutes) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		c.set(Calendar.HOUR_OF_DAY, winHour);
		c.set(Calendar.MINUTE, winMinutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	@Override
	public String toString() {
		return "OnceADayWithinPeriod [winStartHour=" + winStartHour + ", winStartMinutes=" + winStartMinutes + ", winEndHour=" + winEndHour + ", winEndMinutes=" + winEndMinutes + ", super="
				+ super.toString() + "]";
	}

}
