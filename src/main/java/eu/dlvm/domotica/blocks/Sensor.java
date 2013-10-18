package eu.dlvm.domotica.blocks;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.LogCh;

/**
 * Sensors sense input.
 * <p>
 * They have at least one input channel. They transform simple on or off into
 * higher level events, such as DoubleClick or SingleClick.
 * <p>
 * Only Sensors must read data from hardware. This to avoid difficult to find bugs.
 * @author Dirk Vaneynde
 * 
 */
public abstract class Sensor extends BlockWithContext {

	static Logger log = Logger.getLogger(Sensor.class);

	private LogCh channel;
	protected Set<ISensorListener> listeners = new HashSet<ISensorListener>();
	protected OperationExecutor opex = new OperationExecutor();
	/**
	 * Create a Sensor as a Block, and add it to the Control of Blocks.
	 * 
	 * @param ctrl
	 * @param name
	 * @param description
	 */
	public Sensor(String name, String description, LogCh channel, IDomoContext ctx) {
		super(name, description, ctx);
		this.channel = channel;
		ctx.addSensor(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on.
	 */
	public LogCh getChannel() {
		return channel;
	}

	/**
	 * Blocks that are interested in Sensor events. See also {@link ISensorListener#notify(SensorEvent)}.
	 * @param l Object to be notified.
	 */
	public void registerListener(ISensorListener l) {
		listeners.add(l);
	}
	
	public OperationExecutor getOpEx() {
		return opex;
	}

	protected void notifyListeners(SensorEvent e) {
		for (ISensorListener l : listeners) {
			log.debug("notify switch listener, event=" + e + ", listener=" + l);
			l.notify(e);
		}
		opex.send(e.getEventName(), getName());
	}

	/**
	 * Sensor should check its input and/or timeouts etc., in other words a run
	 * is triggered through the logic starting from the Sensor.
	 * 
	 * @param currentTime
	 *            Timestamp at which this loop is called. The same for each
	 *            loop.
	 * @param sequence
	 *            A number that increments with each loop. Useful to detect
	 *            being called twice - which is forbidden.
	 */
	public abstract void loop(long currentTime, long sequence);

	@Override
	public String toString() {
		return "Sensor name='" + name + "', description='" + description + "'";
	}

}
