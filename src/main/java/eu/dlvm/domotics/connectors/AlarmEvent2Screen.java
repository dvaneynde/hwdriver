package eu.dlvm.domotics.connectors;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Connector;
import eu.dlvm.domotics.sensors.IAlarmListener;

/**
 * @author dirk vaneynde
 */
public class AlarmEvent2Screen extends Connector implements IAlarmListener {

	private static final Logger LOG = LoggerFactory.getLogger(AlarmEvent2Screen.class);
	private Set<Screen> screens = new HashSet<>();

	/*
	 * Public API
	 */
	public AlarmEvent2Screen(String name, String description) {
		super(name, description, null);
	}

	/*
	 * Internal API
	 */
	public void registerListener(Screen screen) {
		screens.add(screen);
	}

	@Override
	public void onEvent(Block source, EventType event) {
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

	@Override
	public String toString() {
		return "AlarmEvent2Screen [screens=" + screens + "]";
	}
}
