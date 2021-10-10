package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.blocks.DomoticMock;

public class TestTimer {

	@Test
	public void testTypicalDay() {
		Timer t = new Timer("timer", "timer test", new DomoticMock());
		t.setOnTime(22, 0);
		t.setOffTime(7, 30);
		assertFalse(t.isOn());
		
		Calendar c = GregorianCalendar.getInstance();
		c.set(2013, 8, 1, 0, 0);	// 1 september 2013, toen geschreven ;-)
		t.loop(c.getTimeInMillis());
		assertTrue(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 7);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertTrue(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 22);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertTrue(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE,59);
		t.loop(c.getTimeInMillis());
		assertTrue(t.isOn());
		
		c.set(Calendar.DAY_OF_MONTH, 2);
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertTrue(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
	}

	@Test
	public void testBoundaryOnIsOff() {
		Timer t = new Timer("timer", "timer test", new DomoticMock());
		t.setOnTime(10, 0);
		t.setOffTime(10, 0);
		assertFalse(t.isOn());
		
		Calendar c = GregorianCalendar.getInstance();
		c.set(2013, 8, 1, 0, 0);	// 1 september 2013, toen geschreven ;-)
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 7);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
		
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE,0);
		t.loop(c.getTimeInMillis());
		assertFalse(t.isOn());
	}

}
