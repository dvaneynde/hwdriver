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
		LowLight, HighLight
	};
	private LightState lightState = LightState.LowLight;

	private enum WindState {
		Safe, Alarm
	};
	private WindState windState = WindState.Safe;

	private boolean enabled;

	
	public SunWindController(String name, String description, String ui, IDomoticContext ctx) {
		super(name, description, ui, ctx);
	}

	public void on() {
		logger.info("Automatic mode for '" + getName() + "' is set.");
		enabled = true;
		if (lightState == LightState.HighLight && windState==WindState.Safe){
			notifyListeners(EventType.DOWN);
			logger.info("Sun high, wind low, so screens Down after going to automatic mode.");
		} else if (lightState==LightState.LowLight && windState== WindState.Safe){
			notifyListeners(EventType.UP);
			logger.info("Sun low, so screens Up after going to automatic mode.");
		}
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
			windState = WindState.Safe;
			if (enabled && lightState == LightState.HighLight) {
				notifyListeners(event);
				notifyListeners(EventType.DOWN);
				logger.info("Got wind is Safe event, Sun is High and I'm enabled, so screens can go Down.");
			}
			break;
		case ALARM:
			// TODO these should be generated here, not come from WindSensor or
			// LightGauge !
			// Goes together with parameters that must be here of course.
			windState = WindState.Alarm;
			notifyListeners(event);
			logger.info("Got wind Alarm, so screens must go up.");
			break;
		case LIGHT_HIGH:
			lightState = LightState.HighLight;
			if (enabled && windState == WindState.Safe) {
				notifyListeners(EventType.DOWN);
				logger.info("Got Sun High event, wind is Safe and I'm enabled, so screens go Down.");
			}
			break;
		case LIGHT_LOW:
			lightState = LightState.LowLight;
			if (enabled && windState == WindState.Safe) {
				notifyListeners(EventType.UP);
				logger.info("Got Sun Low event, wind is Safe and I'm enabled, so screens go Up.");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, null, isEnabled());
		// bi.setStatus(isEnabled() ? "ON" : "OFF");
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

	@Override
	public String toString() {
		return "SunWindController [lightState=" + lightState + ", windState=" + windState + ", enabled=" + enabled
				+ ", name=" + name + "]";
	}
}
