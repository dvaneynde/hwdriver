package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestOnceADay {

	@Test
	public void test() {
		OnceADay t = new OnceADay();
		assertFalse(t.doneForToday);
		assertFalse(t.checkDoneForToday(System.currentTimeMillis()));
		assertFalse(t.doneForToday);
		t.markDoneForToday();
		assertTrue(t.checkDoneForToday(System.currentTimeMillis()));
		assertTrue(t.doneForToday);
		System.out.println("today="+t.today);
	}

}
