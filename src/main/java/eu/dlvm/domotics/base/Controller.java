package eu.dlvm.domotics.base;

import org.apache.log4j.Logger;

/**
 * Controller drives Actuators, like Sensors, but without access to hardware.
 * <p>
 * Examples: NewYear is typical. Another is AllOnOff, which did not be separate
 * because Sensors could do the same (multiple listeners) but that did not map
 * to a 'All Off' button in the UI.
 */
public abstract class Controller extends Block implements IDomoticLoop, IUserInterfaceAPI {

	static Logger log = Logger.getLogger(Controller.class);

	public Controller(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui);
		ctx.addController(this);
	}

}
