package eu.dlvm.domotics.base;

import eu.dlvm.domotics.events.IEventListener;

/**
 * Controller drive Actuators or other Controllers, like Sensors do, but without
 * access to hardware.
 * <p>
 * TODO remove IUICapableBlock here?
 */
public abstract class Controller extends Block implements IDomoticLoop, IEventListener, IUiCapableBlock {

	public Controller(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui);
		ctx.addController(this);
	}

}
