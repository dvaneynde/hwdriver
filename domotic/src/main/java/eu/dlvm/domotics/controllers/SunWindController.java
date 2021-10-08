package eu.dlvm.domotics.controllers;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotic.sensor.sun.SunHeightAzimuth;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticBuilder;
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

	private double azimuthStart;
	private double azimuthEnd;
	private boolean withinAzimuth;
	private boolean initialisedWithinAzimuth = false;

	private Queue<EventType> unfinishdelayedEventsedEvents = new LinkedList<EventType>();

	private enum LightState {
		LowLight, HighLight
	};

	private LightState lightState = LightState.LowLight;

	private enum WindState {
		Safe, Alarm
	};

	private WindState windState = WindState.Safe;
	private boolean enabled;

	/**
	 * @param name
	 * @param description
	 * @param azimuthStart
	 *            degrees relative to south to start, positive is to east
	 *            (counter clockwise)
	 * @param azimuthEnd
	 *            degrees relative to south to stop, negative is to west
	 *            (clockwise)
	 * @param ui
	 * @param ctx
	 */
	public SunWindController(String name, String description, double azimuthStart, double azimuthEnd, String ui,
			IDomoticBuilder ctx) {
		super(name, description, ui, ctx);
		if (azimuthStart < azimuthEnd)
			throw new IllegalArgumentException(
					"Azimuth start must be larger than azimuth end; goes from say +45 to -45.");
		this.azimuthStart = azimuthStart;
		this.azimuthEnd = azimuthEnd;
		logger.info(
				"Add Sun Wind Controller '" + getName() + "' azimuth is (" + azimuthStart + ".." + azimuthEnd + ").");
	}

	public SunWindController(String name, String description, String ui, IDomoticBuilder ctx) {
		this(name, description, +180, -180, ui, ctx);
		logger.info("Sun Wind Controller '" + getName() + "' got default azimuths.");
	}

	boolean withinAzimuthChanged(long time) {
		boolean newWithin = SunWindController.withinAzimuth(time, azimuthStart, azimuthEnd, withinAzimuth);
		boolean changed = (newWithin != withinAzimuth || !initialisedWithinAzimuth);
		initialisedWithinAzimuth = true;
		withinAzimuth = newWithin;
		return changed;
	}

	public static boolean withinAzimuth(long time, double azimuthStart, double azimuthEnd, boolean currentWithinForLogging) {
		boolean result = false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		double dHour = (double) hour + ((double) minutes / 60);

		double height = SunHeightAzimuth.hoogtehoek(dayOfYear, dHour);
		if (height > 0) {
			double azimuth = SunHeightAzimuth.azimuth(180, 0);
			// is between, say 45 and -45?
			result = (azimuth <= azimuthStart && azimuth >= azimuthEnd);
			if (result) {
				result = true;
				if (result != currentWithinForLogging)
					logger.info(result ? "Azimuth is within range"
							: "Azimuth left range" + " azimuth=" + azimuth + " height=" + height + " startAzimuth="
									+ azimuthStart + " endAzimuth=" + azimuthEnd);
			}
		}
		return result;
	}

	public void on() {
		logger.info("Automatic mode for '" + getName() + "' is set.");
		enabled = true;
		if (withinAzimuth) {
			if (lightState == LightState.HighLight && windState == WindState.Safe) {
				logger.info("Sun high, wind low, so screens Down after going to automatic mode.");
				notifyListeners(EventType.DOWN);
			} else if (lightState == LightState.LowLight && windState == WindState.Safe) {
				logger.info("Sun low, so screens Up after going to automatic mode.");
				notifyListeners(EventType.UP);
			}
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
			if (windState == WindState.Safe)
				break;
			windState = WindState.Safe;
			unfinishdelayedEventsedEvents.add(EventType.SAFE);
			if (enabled && withinAzimuth && lightState == LightState.HighLight) {
				unfinishdelayedEventsedEvents.add(EventType.DOWN);
				logger.info("Scheduled: Got wind is Safe event, Sun is High and I'm enabled, so screens can go Down.");
			}
			break;
		case ALARM:
			if (windState == WindState.Alarm)
				break;
			unfinishdelayedEventsedEvents.clear();
			windState = WindState.Alarm;
			logger.info("Got wind Alarm, so screens must go up.");
			notifyListeners(event);
			break;
		case LIGHT_HIGH:
			lightState = LightState.HighLight;
			if (enabled && withinAzimuth && windState == WindState.Safe) {
				logger.info("Got Sun High event, wind is Safe and I'm enabled, so screens go Down.");
				notifyListeners(EventType.DOWN);
			}
			break;
		case LIGHT_LOW:
			lightState = LightState.LowLight;
			if (enabled && windState == WindState.Safe) {
				logger.info("Got Sun Low event, wind is Safe and I'm enabled, so screens go Up.");
				notifyListeners(EventType.UP);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, null, isEnabled());
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
	public void loop(long currentTime) {
		if (withinAzimuthChanged(currentTime)) {
			if (withinAzimuth && (lightState == LightState.HighLight) && (windState == WindState.Safe)) {
				logger.info("Within azimuth range, light is High, wind is Safe and I'm enabled, so screens go Down.");
				notifyListeners(EventType.DOWN);
			} else if (!withinAzimuth && (windState == WindState.Safe)) {
				logger.info("Outside azimuth range, wind is Safe and I'm enabled, so screens go Up.");
				notifyListeners(EventType.UP);
			}
		} else if (!unfinishdelayedEventsedEvents.isEmpty()) {
			EventType e = unfinishdelayedEventsedEvents.poll();
			logger.info("Sending delayed event: "+e);
			notifyListeners(e);
		}
	}

	@Override
	public String toString() {
		return "SunWindController [azimuthStart=" + azimuthStart + ", azimuthEnd=" + azimuthEnd + ", withinAzimuth="
				+ withinAzimuth + ", lightState=" + lightState + ", windState=" + windState + ", enabled=" + enabled
				+ "]";
	}
}
