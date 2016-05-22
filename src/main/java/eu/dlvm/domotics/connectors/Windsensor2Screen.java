package eu.dlvm.domotics.connectors;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Connector;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.WindSensor;

/**
 * Connects a Down and Up {@link Switch} to one or more {@link Screen}'s to go
 * down or up. It will only react on one specific {@link ClickType} event.
 * 
 * @author dirk vaneynde
 * 
 */
public class Windsensor2Screen extends Connector implements IThresholdListener {

	private static final Logger log = Logger.getLogger(Windsensor2Screen.class);
	private Set<Screen> screens = new HashSet<>();

	/*
	 * Public API
	 */
	public Windsensor2Screen(String name, String description) {
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
		if (source instanceof WindSensor)
			switch (event) {
			case HIGH:
				for (Screen screen : screens) {
					screen.setProtect(true);
				}
				break;
			case LOW:
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
		return "Windsensor2Screen [screens=" + screens + "]";
	}

}
