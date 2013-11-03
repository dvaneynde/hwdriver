package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.LogCh;

/**
 * TODO can go away, just package hints to it being a sensor, i.e. without listener or being triggered somewhere.
 * Sensors sense input from hardware, or elsewhere. 
 * <p>
 * They have at least one input channel. They transform simple on or off into
 * higher level events, such as DoubleClick or SingleClick.
 * <p>
 * Only Sensors must read data from hardware. This to avoid difficult to find bugs.
 * @author Dirk Vaneynde
 */
public abstract class Sensor extends BlockWithHardwareAccess {

	static Logger log = Logger.getLogger(Sensor.class);

	private LogCh channel;
	/**
	 * Create a Sensor as a Block, and add it to the Control of Blocks.
	 * 
	 * @param ctrl
	 * @param name
	 * @param description
	 */
	public Sensor(String name, String description, LogCh channel, IHardwareAccess ctx) {
		super(name, description, ctx);
		this.channel = channel;
		ctx.addSensor(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on.
	 * TODO move to IHardwareAccess
	 */
	public LogCh getChannel() {
		return channel;
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
	 * TODO move to IHardwareAccess
	 */
	public abstract void loop(long currentTime, long sequence);

	@Override
	public String toString() {
		return "Sensor name='" + name + "', description='" + description + "'";
	}

}
