package eu.dlvm.domotics.connectors;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Connector;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.sensors.IThresholdListener;

/**
 * @author dirk vaneynde
 */
public class ThresholdEvent2Screen extends Connector implements IThresholdListener {

	private static final Logger log = LoggerFactory.getLogger(ThresholdEvent2Screen.class);
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
				screen.toggleDown();
			}
			break;
		case LOW:
			for (Screen screen : screens) {
				screen.toggleUp();
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
