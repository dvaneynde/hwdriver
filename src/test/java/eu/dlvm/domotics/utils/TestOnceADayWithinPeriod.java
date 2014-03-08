package eu.dlvm.domotics.utils;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import eu.dlvm.domotics.utils.OnceADayWithinPeriod;

public class TestOnceADayWithinPeriod {

	@Test
	public void testNormalCase() {
		OnceADayWithinPeriod t = new OnceADayWithinPeriod(2, 0, 4, 0);
		Calendar now = Calendar.getInstance();
		now.set(2014, 2, 8, 1, 0, 30);
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		t.markDoneForToday(now.getTimeInMillis());
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));

		now.set(2014, 2, 8, 2, 0, 30);
		assertTrue(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		t.markDoneForToday(now.getTimeInMillis());
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
		t.markDoneForToday(now.getTimeInMillis());
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));

		now.set(2014, 2, 8, 4, 1, 30);
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		t.markDoneForToday(now.getTimeInMillis());
		assertFalse(t.canCheckForToday(now.getTimeInMillis()));
		assertFalse(t.checkDoneForToday(now.getTimeInMillis()));
		
	}
}
