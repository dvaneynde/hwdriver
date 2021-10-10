package eu.dlvm.domotics.blocks;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.*;

public class DomoticMock implements IDomoticBuilder {

	public List<Sensor> sensors = new ArrayList<Sensor>(64);
	public List<Actuator> actuators = new ArrayList<Actuator>(32);
	public List<Controller> controllers = new ArrayList<Controller>(32);

	public DomoticMock() {
	}

	@Override
	public void addSensor(Sensor s) {
		for (Sensor ss : sensors) {
			if (ss == s) {
				assert (false);
				return;
			}
		}
		sensors.add(s);
	}

	@Override
	public void addActuator(Actuator a) {
		for (Actuator aa : actuators) {
			if (aa == a) {
				assert (false);
				return;
			}
		}
		actuators.add(a);
	}

	@Override
	public void addController(Controller a) {
		for (Controller aa : controllers) {
			if (aa == a) {
				assert (false);
				return;
			}
		}
		controllers.add(a);
	}
}
