package eu.dlvm.domotics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
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
public class SunWindController extends Controller implements IEventListener, IUiCapableBlock {
	private static final Logger logger = LoggerFactory.getLogger(SunWindController.class);
	private boolean enabled;

	public SunWindController(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui, ctx);
	}


	public void on() {
		logger.info("Automatic mode for '" + getName() + "' is set.");
		enabled = true;
	}

	public void off() {
		enabled = false;
		logger.info("Manual mode for '" + getName() + "' is set.");
	}

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
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case OFF:
			off();
			break;
		case ON:
			on();
			break;
		case TOGGLE:
			toggle();
			break;
		case SAFE:
		case ALARM:
			// TODO these should be generated here, not come from WindSensor or LightGauge ! Goes together with parameters that must be here of course.
			notifyListeners(event);
			break;
		case LIGHT_HIGH:
		case LIGHT_LOW:
			// TODO these should be generated here, not come from WindSensor or LightGauge ! Goes together with parameters that must be here of course.
			if (enabled)
				notifyListeners(event);
			else
				logger.info(getName() + " has blocked event '" + event.toString() + "' from source '" + source.getName() + "'.");
			break;
		default:
			break;
		}
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = new UiInfo(this);
		bi.setOn(isEnabled());
		bi.setStatus(isEnabled() ? "ON" : "OFF");
		return bi;
	}

	@Override
	public void update(String eventName) {
		try {
			EventType eventType = EventType.valueOf(eventName.toUpperCase());
			onEvent(null, eventType);
		} catch (IllegalArgumentException e) {
			logger.warn("update(), ignored unknown action: " + eventName);
		}
	}


	@Override
	public void loop(long currentTime, long sequence) {
	}

}
