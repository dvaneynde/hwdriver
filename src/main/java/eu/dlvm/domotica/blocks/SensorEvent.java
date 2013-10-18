package eu.dlvm.domotica.blocks;


/**
 * An event in some {@link Sensor}, typically as a consequence of an input state
 * change. However, events are higher level abstractions than a mere input
 * channel state change.
 * 
 * @author dirk vaneynde
 * 
 */
public class SensorEvent {
	private Sensor source;
	private Object event;
	private String eventName;

	public SensorEvent(Sensor source, Object event) {
		this.source = source;
		this.event = event;
		this.eventName = event.toString();
	}

	public Sensor getSource() {
		return source;
	}

	public Object getEvent() {
		return event;
	}

	public String getEventName() {
		return eventName;
	}

	@Override
	public String toString() {
		return "SensorEvent [source=" + source + ", event=" + event
				+ ", eventName=" + eventName + "]";
	}

}
