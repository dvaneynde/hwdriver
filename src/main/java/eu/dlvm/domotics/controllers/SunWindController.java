package eu.dlvm.domotics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoOnOff;

/**
 * Enables a source event to go through or not.
 * <p>
 * <ul>
 * <li>When 'on' only the first HIGH or LOW are transmitted, besides all
 * ALARM/SAFE events.</li>
 * <li>When 'off' all the ALARM/SAFE events are transmitted, but no others.</li>
 * </ul>
 * 
 * @author dirk
 */
public class SunWindController extends Controller implements IEventListener, IUiCapableBlock {
	private static final Logger logger = LoggerFactory.getLogger(SunWindController.class);

	private enum LightState {
		Init, WasDark, WasLight
	}

	private LightState lightState = LightState.Init;
	private boolean enabled;

	public SunWindController(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui, ctx);
	}

	public void on() {
		logger.info("Automatic mode for '" + getName() + "' is set.");
		enabled = true;
		lightState = LightState.Init;
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
			if (enabled) {
				switch (lightState) {
				case Init:
				case WasDark:
					notifyListeners(event);
					lightState = LightState.WasLight;
					break;
				case WasLight:
					break;
				}
			}
			break;
		case LIGHT_LOW:
			if (enabled) {
				switch (lightState) {
				case Init:
				case WasLight:
					notifyListeners(event);
					lightState = LightState.WasDark;
					break;
				case WasDark:
					break;
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, null, isEnabled());
		//bi.setStatus(isEnabled() ? "ON" : "OFF");
		return uiInfo;
	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on Lamp '" + getName() + "' got unsupported action '" + action + ".");

	}

	@Override
	public void loop(long currentTime, long sequence) {
	}

}
