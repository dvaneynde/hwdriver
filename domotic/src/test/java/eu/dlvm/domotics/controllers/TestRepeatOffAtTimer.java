package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;

public class TestRepeatOffAtTimer {
	boolean lastOffCalled = false;

	@Test
	public void testTypicalDay() {
        DomoticMock domoticContext = new DomoticMock();
		RepeatOffAtTimer t = new RepeatOffAtTimer("timer", "timer test", domoticContext, 60);
		t.registerListener(new Actuator("test", "test", null, null, null, domoticContext) {

			public void onEvent(Block source, EventType event) {
				if (event == EventType.OFF)
					lastOffCalled = true;
				else
					fail();
			}

			@Override
			public void loop(long currentTime) {
			}

			@Override
			public void initializeOutput(RememberedOutput ro) {
			}
		});
		
		t.setOnTime(7, 30);
		t.setOffTime(8, 30);
		assertFalse(t.isOn());

		Calendar c = GregorianCalendar.getInstance();
		c.set(2016, 4, 21, 0, 0); // 21 mei 2016, toegevoegd omdat kinderen
									// altijd licht in garage laten branden als
									// ze 's morgens naar school vertrekken ;-)
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());

		c.set(Calendar.HOUR_OF_DAY, 7);
		c.set(Calendar.MINUTE, 0);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());

		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		long time = c.getTimeInMillis();
		t.loop(time);
		assertTrue(t.isOn());
		assertTrue(lastOffCalled);
		lastOffCalled = false;
		t.loop(time + 59000);
		assertTrue(t.isOn());
		assertFalse(lastOffCalled);
		t.loop(time + 60000);
		assertTrue(t.isOn());
		assertTrue(lastOffCalled);
		lastOffCalled = false;
		t.loop(time + 60500);
		assertTrue(t.isOn());
		assertFalse(lastOffCalled);

		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 31);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
		assertFalse(lastOffCalled);
	}
}
