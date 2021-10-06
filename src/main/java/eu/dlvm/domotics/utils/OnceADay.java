package eu.dlvm.domotics.utils;

import java.util.Calendar;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Helper, checks that something ('thingie') is done only once a day.
 * <p>
 * Timezone used to determine current 'day' is default one.
 * 
 * @author dirkv
 * 
 */
public class OnceADay {

	private static Logger log = LoggerFactory.getLogger(OnceADay.class);

	protected Calendar today;
	protected boolean doneForToday;

	/**
	 * Check if you did your thingie for today.
	 * 
	 * @param currentTime
	 *            determines 'today'
	 * @return if last markDoneForToday() call was not 'today', then return
	 *         false, true otherwise
	 */
	public boolean checkDoneForToday(long currentTime) {
		if (doneForToday || today == null) {
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(currentTime);
			if (today == null || !sameday(now, today)) {
				today = now;
				doneForToday = false;
			}
		}
		if (log.isDebugEnabled())
			log.debug("checkDoneForToday() currentTime=" + currentTime + ", done=" + doneForToday + ", this=" + this);
		return doneForToday;
	}

	/**
	 * Call this when you have done your thingie for today.
	 * 
	 * @param currentTimeMillis
	 *            Not used.
	 */
	public void markDoneForToday() {
		doneForToday = true;
		if (log.isDebugEnabled())
			log.debug("marked done for today, this=" + toString());
	}

	private static boolean sameday(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public String toString() {
		return "OnceADay [doneForToday=" + doneForToday + ", today=" + today + "]";
	}

}
