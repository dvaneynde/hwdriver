package eu.dlvm.domotics.sensors;

import java.util.ArrayDeque;
import java.util.Iterator;

// TODO move to util package
public class RingBuffer<T> implements Iterable<T> {

	public RingBuffer(int capacity) {
		this.capacity = capacity;
		dequeue = new ArrayDeque<>(capacity);
	}

	public boolean filled() {
		return (dequeue.size() == capacity);
	}

	public T last() {
		return dequeue.peekLast();
	}
	
	public void add(T t) {
		if (filled())
			dequeue.removeFirst();
		dequeue.add(t);
	}

	@Override
	public Iterator<T> iterator() {
		return dequeue.iterator();
	}

	private ArrayDeque<T> dequeue;
	private int capacity;
}
