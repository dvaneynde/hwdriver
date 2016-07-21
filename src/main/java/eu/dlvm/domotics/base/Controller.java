package eu.dlvm.domotics.base;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Controller drives Actuators, like Sensors, but without access to hardware.
 * <p>
 * Examples: NewYear is typical. Another is AllOnOff, which did not be separate
 * because Sensors could do the same (multiple listeners) but that did not map
 * to a 'All Off' button in the UI.
 */
public abstract class Controller extends Block implements IDomoticLoop, IUserInterfaceAPI {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);

	public Controller(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui);
		ctx.addController(this);
	}

}
