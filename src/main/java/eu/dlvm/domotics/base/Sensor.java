package eu.dlvm.domotics.base;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

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

	private IDomoticContext ctx;
	private LogCh channel;
	

	/**
	 * Create a Sensor as a Block, and add it to the Control of Blocks.
	 * 
	 * @param ctrl
	 * @param name
	 * @param description
	 */
	public Sensor(String name, String description, LogCh channel, IDomoticContext ctx) {
		this(name, description, null, channel, ctx);
	}

	public Sensor(String name, String description, String ui, LogCh channel, IDomoticContext ctx) {
		super(name, description, ui);
		this.ctx = ctx;
		this.channel = channel;
		ctx.addSensor(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on. TODO move to
	 *         IHardwareAccess
	 */
	public LogCh getChannel() {
		return channel;
	}

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw() {
		return ctx.getHw();
	}

	@Override
	public String toString() {
		return "Sensor name='" + name + "', description='" + description + "'";
	}

}
