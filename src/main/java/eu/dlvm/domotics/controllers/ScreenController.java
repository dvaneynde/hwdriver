package eu.dlvm.domotics.controllers;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.sensors.IAlarmListener;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.IThresholdListener.EventType;
import eu.dlvm.domotics.service.BlockInfo;

/**
 * Enables a source event to go through or not,
 * 
 * @author dirk
 */
public class ScreenController extends Controller implements IOnOffToggleCapable, IAlarmListener, IThresholdListener, IUserInterfaceAPI {
	private static final Logger log = Logger.getLogger(ScreenController.class);
	private boolean enabled;
	private Set<Screen> screens = new HashSet<>();

	public ScreenController(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui, ctx);
	}

	@Override
	public void onEvent(Block source, IAlarmListener.EventType event) {
		notifyListeners(event);
	}

	@Override
	public void onEvent(Sensor source, IThresholdListener.EventType event) {
		if (enabled)
			notifyListeners(event);
		else
			log.info(getName() + " has blocked event '" + event.toString() + "' from source '" + source.getName() + "'.");
	}

	@Override
	public void onEvent(ActionType action) {
		switch (action) {
		case OFF:
			off();
			break;
		case ON:
			on();
			break;
		case TOGGLE:
			toggle();
			break;
		default:
			break;
		}
	}

	@Override
	public void on() {
		log.info("Automatic mode for '" + getName() + "' is set.");
		enabled = true;
	}

	@Override
	public void off() {
		enabled = false;
		log.info("Manual mode for '" + getName() + "' is set.");
	}

	@Override
	public boolean toggle() {
		if (enabled)
			off();
		else
			on();
		return enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void loop(long currentTime, long sequence) {
	}

	@Override
	public BlockInfo getBlockInfo() {
		BlockInfo bi = new BlockInfo(this.getName(), this.getClass().getSimpleName(), getDescription());
		bi.setOn(isEnabled());
		bi.setStatus(isEnabled() ? "ON" : "OFF");
		return bi;
	}

	@Override
	public void update(String action) {
		try {
			ActionType at = ActionType.valueOf(action.toUpperCase());
			onEvent(at);
		} catch (IllegalArgumentException e) {
			log.warn("update(), ignored unknown action: " + action);
		}
	}

	public void registerListener(Screen listener) {
		screens.add(listener);
	}

	public void notifyListeners(IThresholdListener.EventType event) {
		switch (event) {
		case HIGH:
			for (Screen screen : screens) {
				screen.down();
			}
			break;
		case LOW:
			for (Screen screen : screens) {
				screen.up();
			}
			break;
		default:
			break;
		}
	}

	public void notifyListeners(IAlarmListener.EventType event) {
		switch (event) {
		case ALARM:
			for (Screen screen : screens) {
				screen.setProtect(true);
			}
			break;
		case SAFE:
			for (Screen screen : screens) {
				screen.setProtect(false);
			}
			break;
		default:
			break;
		}
	}
}
