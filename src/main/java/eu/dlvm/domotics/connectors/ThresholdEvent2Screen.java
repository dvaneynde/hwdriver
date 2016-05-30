package eu.dlvm.domotics.connectors;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Connector;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.sensors.IThresholdListener;

/**
 * @author dirk vaneynde
 */
public class ThresholdEvent2Screen extends Connector implements IThresholdListener {

	private static final Logger log = Logger.getLogger(ThresholdEvent2Screen.class);
	private Set<Screen> screens = new HashSet<>();

	/*
	 * Public API
	 */
	public ThresholdEvent2Screen(String name, String description) {
		super(name, description, null);
	}

	/*
	 * Internal API
	 */
	public void registerListener(Screen screen) {
		screens.add(screen);
	}

	@Override
	public void onEvent(Sensor source, EventType event) {
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

	@Override
	public String toString() {
		return "ThresholdEvent2Screen [screens=" + screens + "]";
	}

}
