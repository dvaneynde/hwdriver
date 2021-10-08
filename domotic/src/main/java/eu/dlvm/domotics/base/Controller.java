package eu.dlvm.domotics.base;

import eu.dlvm.domotics.events.IEventListener;

/**
 * Controller drive Actuators or other Controllers, like Sensors do, but without
 * access to hardware.
 * <p>
 * TODO remove IUICapableBlock here?
 */
public abstract class Controller extends Block implements IDomoticLoop, IEventListener {

	public Controller(String name, String description, String ui, IDomoticBuilder builder) {
		super(name, description, ui);
		builder.addController(this);
	}

}
