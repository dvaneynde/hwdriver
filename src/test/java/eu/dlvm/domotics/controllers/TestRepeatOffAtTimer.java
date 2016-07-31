package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;

public class TestRepeatOffAtTimer {
	boolean lastOffCalled = false;

	@Test
	public void testTypicalDay() {
		RepeatOffAtTimer t = new RepeatOffAtTimer("timer", "timer test", new DomoContextMock(null), 60);
		t.register(new IOnOffToggleCapable() {

			@Override
			public boolean toggle() {
				fail();
				return false;
			}

			@Override
			public void onEvent(ActionType action) {
				if (action == ActionType.OFF)
					off();
				else
					fail();
			}

			@Override
			public void on() {
				fail();
			}

			@Override
			public void off() {
				lastOffCalled = true;
			}
		});
		t.setOnTime(7, 30);
		t.setOffTime(8, 30);
		assertFalse(t.isOn());

		Calendar c = GregorianCalendar.getInstance();
		c.set(2016, 4, 21, 0, 0); // 21 mei 2016, toegevoegd omdat kinderen
									// altijd licht in garage laten branden als
									// ze 's morgens naar school vertrekken ;-)
		t.loop(c.getTimeInMillis(), 0);
		assertFalse(t.isOn());

		c.set(Calendar.HOUR_OF_DAY, 7);
		c.set(Calendar.MINUTE, 0);
		t.loop(c.getTimeInMillis(), 0);
		assertFalse(t.isOn());

		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		long time = c.getTimeInMillis();
		t.loop(time, 0);
		assertTrue(t.isOn());
		assertTrue(lastOffCalled);
		lastOffCalled = false;
		t.loop(time + 59000, 0);
		assertTrue(t.isOn());
		assertFalse(lastOffCalled);
		t.loop(time + 60000, 0);
		assertTrue(t.isOn());
		assertTrue(lastOffCalled);
		lastOffCalled = false;
		t.loop(time + 60500, 0);
		assertTrue(t.isOn());
		assertFalse(lastOffCalled);

		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 31);
		t.loop(c.getTimeInMillis(), 0);
		assertFalse(t.isOn());
		assertFalse(lastOffCalled);
	}
}
