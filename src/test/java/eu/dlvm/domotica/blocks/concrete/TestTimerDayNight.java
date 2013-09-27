package eu.dlvm.domotica.blocks.concrete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.Ignore;
import org.junit.Test;

import eu.dlvm.domotica.blocks.DomoContextMock;
import eu.dlvm.domotica.utils.OpenWeatherMap;
import eu.dlvm.iohardware.LogCh;

public class TestTimerDayNight {

	// TODO fixed day for basetime !

	public class OwmTest extends OpenWeatherMap {
		Info[] infos;
		int seq;

		public OwmTest(long basetime) {
			infos = new Info[5];
			infos[0] = null;
			infos[1] = null;
			infos[2] = new Info();
			infos[2].sunrise_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(
					basetime, 7, 54) / 1000L;
			infos[2].sunset_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(
					basetime, 17, 38) / 1000L;
			infos[3] = null;
			infos[4] = new Info();
			infos[4].sunrise_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(
					basetime, 7, 56) / 1000L;
			infos[4].sunset_sec = TimerDayNight.getTimeMsSameDayAtHourMinute(
					basetime, 17, 35) / 1000L;
			seq = 0;
		}

		public Info getWeatherReport() {
			if (seq >= infos.length)
				fail("getWeatherReport should not have been called anymore.");
			return infos[seq++];
		}
	}

	@Ignore
	@Test
	public void testCheckChangedDay() {
		fail("Not yet implemented");
	}

	@Test
	public void testAllOk() {
		int seq = 0;
		
		//long basetime = System.currentTimeMillis();
		long basetime = 0L;
		TimerDayNight t = new TimerDayNight("timerDayNigth",
				"timer day and night", new LogCh("tdn"), new DomoContextMock(
						null));
		TestTimerDayNight.OwmTest owmt = new TestTimerDayNight.OwmTest(basetime);
		t.setOpenWeatherMap(owmt);
		t.setOffTime(8, 0);
		t.setOnTime(6, 30);
		assertFalse(t.getStatus());
		assertFalse(t.isTimesUpdatedForToday());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(0, owmt.seq);

		// Eerste request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 0),
				seq++);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.getStatus());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(1, owmt.seq);

		// Binnen 5 minuten wachttijd van internet request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 4),
				seq++);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.getStatus());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(1, owmt.seq);

		// Tweede request
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 5),
				seq++);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.getStatus());
		assertEquals("08:00", t.getOffTimeAsString());
		assertEquals("06:30", t.getOnTimeAsString());
		assertEquals(2, owmt.seq);

		// En nu resultaat van internet
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 4, 10),
				seq++);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.getStatus());
		assertEquals("07:24", t.getOffTimeAsString());
		assertEquals("18:08", t.getOnTimeAsString());
		assertEquals(3, owmt.seq);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 10, 0),
				seq++);
		assertFalse(t.getStatus());
		assertEquals("07:24", t.getOffTimeAsString());
		assertEquals("18:08", t.getOnTimeAsString());
		assertEquals(3, owmt.seq);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 18, 9),
				seq++);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.getStatus());
		assertEquals("07:24", t.getOffTimeAsString());
		assertEquals("18:08", t.getOnTimeAsString());
		assertEquals(3, owmt.seq);

		// simulate next day, and internet provider gives no result yet
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(basetime + 24 * 60 * 60 * 1000L);
		basetime = c.getTimeInMillis();
		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 0),
				seq++);
		assertFalse(t.isTimesUpdatedForToday());
		assertTrue(t.getStatus());
		assertEquals("07:24", t.getOffTimeAsString());
		assertEquals("18:08", t.getOnTimeAsString());
		assertEquals(4, owmt.seq);

		t.loop(TimerDayNight.getTimeMsSameDayAtHourMinute(basetime, 0, 5),
				seq++);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.getStatus());
		assertEquals("07:26", t.getOffTimeAsString());
		assertEquals("18:05", t.getOnTimeAsString());
		assertEquals(5, owmt.seq);
	}
}
