package eu.dlvm.domotics.base;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Controller drive Actuators or other Controllers, like Sensors do, but without access to hardware.
 */
public abstract class Controller extends Block implements IDomoticLoop, IUiCapableBlock {

	private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

	public Controller(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui);
		ctx.addController(this);
	}

}
