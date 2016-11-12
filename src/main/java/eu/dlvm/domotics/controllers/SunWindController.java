package eu.dlvm.domotics.controllers;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.sensors.IAlarmListener;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.service.UiInfo;

/**
 * Enables a source event to go through or not,
 * <p>
 * <ul>
 * <li>When 'on' only the first HIGH or LOW are transmitted, and all ALARM/SAFE
 * events.</li>
 * <li>When 'off' all the ALARM/SAFE events are transmitted, but no others.</li>
 * <li>When going from ALARM to SAFE the next HIGH or LOW must be transmitted
 * again.</li>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * @author dirk
 */
public class SunWindController extends Controller implements IOnOffToggleCapable, IAlarmListener, IThresholdListener, IUiCapableBlock {
	private static final Logger log = LoggerFactory.getLogger(SunWindController.class);
	private boolean enabled;
	private IThresholdListener.EventType lastThresholdEvent;
	private IAlarmListener.EventType lastAlarmEvent;
	private Set<Screen> screens = new HashSet<>();

	public SunWindController(String name, String description, String ui, IDomoticContext ctx) {
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
			log.debug(getName() + " has blocked event '" + event.toString() + "' from source '" + source.getName() + "'.");
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
		lastThresholdEvent = null;
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
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(this);
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
		if (event == lastThresholdEvent)
			return;
		log.info("Passing threshold event '" + event.toString() + "' to screens, enabled=" + enabled + ", last event was " + lastThresholdEvent);
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
		}
		lastThresholdEvent = event;
	}

	public void notifyListeners(IAlarmListener.EventType event) {
		/*
		 * TODO zou niet moeten voor up, misschien wel voor down? if (event ==
		 * lastAlarmEvent) return;
		 */
		log.debug("Passing alarm event '" + event.toString() + "' to screens, enabled=" + enabled + ", last event was " + lastAlarmEvent);
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
			if (lastAlarmEvent == IAlarmListener.EventType.ALARM)
				lastThresholdEvent = null;
			break;
		}
		lastAlarmEvent = event;
	}
}
