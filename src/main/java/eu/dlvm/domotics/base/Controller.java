package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.LogCh;

//TODO geen sensor (dus geen IHwAccess), maar wel loop
/**
 * Controller drives Actuators, like Sensors, but without access to hardware.
 * <p>
 * Examples: NewYear is typical. Another is AllOnOff, which did not be separate
 * because Sensors could do the same (multiple listeners) but that did not map
 * to a 'All Off' button in the UI.
 */
public abstract class Controller extends Block implements IDomoticLoop {

	static Logger log = Logger.getLogger(Controller.class);

	private IDomoticContext ctx;

	public Controller(String name, String description, LogCh channel, IDomoticContext ctx) {
		super(name, description);
		this.ctx = ctx;
		ctx.addController(this);
	}

}
