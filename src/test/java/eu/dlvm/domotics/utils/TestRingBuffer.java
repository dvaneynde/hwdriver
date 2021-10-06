package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import eu.dlvm.domotics.sensors.RingBuffer;

public class TestRingBuffer {

	@Test
	public void testNotFilled() {
		RingBuffer<Integer> buf = new RingBuffer<>(10);
		for (int i = 0; i < 5; i++) {
			buf.add(new Integer(i));
		}
		assertFalse(buf.filled());
		assertEquals(Integer.valueOf(4), buf.last());

		Iterator<Integer> iter = buf.iterator();
		for (int i = 0; i < 5; i++) {
			assertTrue(iter.hasNext());
			assertEquals(Integer.valueOf(i), iter.next());
		}
		assertFalse(iter.hasNext());
		try {
			iter.next();
			fail("Should throw exception.");
		} catch (NoSuchElementException e) {
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void testFilled() {
		RingBuffer<Integer> buf = new RingBuffer<>(10);
		for (int i = 0; i < 9; i++) {
			buf.add(new Integer(i));
		}
		assertEquals(Integer.valueOf(8), buf.last());
		assertFalse(buf.filled());

		buf.add(new Integer(9));
		assertEquals(Integer.valueOf(9), buf.last());
		assertTrue(buf.filled());

		buf.add(new Integer(10));
		assertEquals(Integer.valueOf(10), buf.last());
		assertTrue(buf.filled());

		buf.add(new Integer(11));
		assertEquals(Integer.valueOf(11), buf.last());
		assertTrue(buf.filled());

		Iterator<Integer> iter = buf.iterator();
		for (int i = 2; i < 12; i++) {
			assertTrue(iter.hasNext());
			assertEquals(Integer.valueOf(i), iter.next());
		}
		assertFalse(iter.hasNext());
	}
}
