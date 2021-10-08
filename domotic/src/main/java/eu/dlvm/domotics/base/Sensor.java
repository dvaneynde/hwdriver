package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensors sense input from hardware.
 * <p>
 * They have at least one input channel. They transform simple on or off into
 * higher level events, such as DoubleClick or SingleClick.
 * <p>
 * Only Sensors can read data from hardware. This to avoid difficult to find
 * bugs.
 * 
 * @author Dirk Vaneynde
 */
public abstract class Sensor extends Block implements IDomoticLoop {

	static Logger log = LoggerFactory.getLogger(Sensor.class);

	private String channel;
	private IHardwareReader reader;

	/**
	 * Create a Sensor as a Block, and add it to the Control of Blocks.
	 * @param name
	 * @param description
	 */
	public Sensor(String name, String description, String channel, IHardwareReader reader, IDomoticBuilder builder) {
		this(name, description, null, channel, reader, builder);
	}

	public Sensor(String name, String description, String ui, String channel, IHardwareReader reader, IDomoticBuilder builder) {
		super(name, description, ui);
		this.reader = reader;
		this.channel = channel;
		builder.addSensor(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on. TODO move to
	 *         IHardwareAccess
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareReader getHwReader() {
		return reader;
	}

	@Override
	public String toString() {
		return "Sensor name='" + name + "', description='" + description + "'";
	}

}
