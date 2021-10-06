package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

public class TestOnceADayWithinPeriod {

	@Test
	public void testNormalCase() {
		OnceADayWithinPeriod t = new OnceADayWithinPeriod(2, 0, 4, 0);
		Calendar now = Calendar.getInstance();
		now.set(2014, 2, 8, 1, 0, 30);
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));

		now.set(2014, 2, 8, 2, 0, 30);
		assertTrue(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		t.markDoneForToday();
		assertTrue(t.canCheckForToday(now.getTimeInMillis()));
		assertTrue(t.checkDoneForToday(now.getTimeInMillis()));

	}

	@Test
	public void testTooLate() {
		OnceADayWithinPeriod t = new OnceADayWithinPeriod(2, 0, 4, 0);
		Calendar now = Calendar.getInstance();
		now.set(2014, 2, 8, 1, 0, 30);
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));

		now.set(2014, 2, 8, 4, 1, 30);
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		t.markDoneForToday();
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertTrue(t.checkDoneForToday(now.getTimeInMillis()));

	}

	@Test
	public void testFromReality() {
		/*
		 * 2014-03-08 20:17:06 utils.OnceADayWithinPeriod [DEBUG] - can check=false, currentTime=1394306226907, this=O
nceADayWithinPeriod [winStartHour=20, winStartMinutes=17, winEndHour=20, winEndMinutes=18, super=OnceADay [
today=null, doneForToday=false]]
		 */
		OnceADayWithinPeriod t = new OnceADayWithinPeriod(20, 17, 20, 18);
		//Calendar now = Calendar.getInstance();
		//now.setTimeInMillis(1394306226907L);
		//System.out.println("now="+now);
		long currentTime = 1394306226907L;
		boolean b = t.canCheckForToday(currentTime);
		assertTrue(b);
		assertFalse(t.checkDoneForToday(currentTime));
		System.out.println("checker ="+t);

	}
}
