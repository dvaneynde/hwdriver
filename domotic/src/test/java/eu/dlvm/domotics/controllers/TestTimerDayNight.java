package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.utils.OpenWeatherMap;

public class TestTimerDayNight {

	// TODO fixed day for basetime !

	public class OwmTest extends OpenWeatherMap {
		Info[] infos;
		int callseqnr;

		public OwmTest(long basetime) {
			infos = new Info[5];
			infos[0] = null;
			infos[1] = null;
			infos[2] = new Info();
			infos[2].sunrise_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 7, 54) / 1000L;
			infos[2].sunset_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 17, 38) / 1000L;
			infos[3] = null;
			infos[4] = new Info();
			infos[4].sunrise_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 7, 56) / 1000L;
			infos[4].sunset_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 17, 35) / 1000L;
			callseqnr = 0;
		}

		public Info getWeatherReport() {
			if (callseqnr >= infos.length)
				fail("getWeatherReport should not have been called anymore.");
			return infos[callseqnr++];
		}
	}

	@Test
	public void testCheckChangedDay() {
		long day0 = new GregorianCalendar(2014, 0, 1, 0, 1, 0).getTime().getTime();
		long day1 = new GregorianCalendar(2014, 0, 2, 0, 0, 10).getTime().getTime();

		TimerDayNight t = new TimerDayNight("test", "test", new DomoticMock());
		t.checktTimesUpdatedForToday(day0);
		assertFalse(t.isTimesUpdatedForToday());

		day0 += 100;
		t.checktTimesUpdatedForToday(day0);
		assertFalse(t.isTimesUpdatedForToday());

		t.setTimesUpdatedForToday(true);
		day0 += 100;
		t.checktTimesUpdatedForToday(day0);
		assertTrue(t.isTimesUpdatedForToday());

		t.checktTimesUpdatedForToday(day1);
		assertFalse(t.isTimesUpdatedForToday());
		day1 += 1 * 3600 * 10000; // 1 hour later
		t.checktTimesUpdatedForToday(day1);
		assertFalse(t.isTimesUpdatedForToday());

		t.setTimesUpdatedForToday(true);
		day1 += 1 * 3600 * 10000; // 1 hour later
		t.checktTimesUpdatedForToday(day1);
		assertTrue(t.isTimesUpdatedForToday());
	}

	@Test
	public void testAllOk() {
		// long basetime = System.currentTimeMillis();
		long basetime = 0L;
		TimerDayNight t = new TimerDayNight("timerDayNigth", "timer day and night", new DomoticMock());
		TestTimerDayNight.OwmTest owmt = new TestTimerDayNight.OwmTest(basetime);
		t.setOpenWeatherMap(owmt);
		t.setOffTime(8, 0);
		t.setOnTime(6, 30);
		assertFalse(t.isOn());
		assertFalse(t.isTimesUpdatedForToday());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(0, owmt.callseqnr);

		// Eerste request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 0));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 0) + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isOn());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Binnen 5 minuten wachttijd van internet request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 4));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 4) + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isOn());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Tweede request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 5));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 5) + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isOn());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(2, owmt.callseqnr);

		// En nu resultaat van internet
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 10));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 10) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isOn());
		assertEquals("07:39", t.getOffTimeAsString());
		assertEquals("17:53", t.getOnTimeAsString());
		assertEquals(3, owmt.callseqnr);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 10, 0));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 10, 0) + 50);
		assertFalse(t.isOn());
		assertEquals("07:39", t.getOffTimeAsString());
		assertEquals("17:53", t.getOnTimeAsString());
		assertEquals(3, owmt.callseqnr);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 18, 9));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 18, 9) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isOn());
		assertEquals("07:39", t.getOffTimeAsString());
		assertEquals("17:53", t.getOnTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// simulate next day, and internet provider gives no result yet
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(basetime + 24 * 60 * 60 * 1000L);
		basetime = c.getTimeInMillis();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 0));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 0) + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertTrue(t.isOn());
		assertEquals("07:39", t.getOffTimeAsString());
		assertEquals("17:53", t.getOnTimeAsString());
		assertEquals(4, owmt.callseqnr);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 5));
		sleepwell();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 5) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isOn());
		assertEquals("07:41", t.getOffTimeAsString());
		assertEquals("17:50", t.getOnTimeAsString());
		assertEquals(5, owmt.callseqnr);
	}
	
	// Nodig omdat async code anders niet afgehandeld is
	public static void sleepwell() {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
