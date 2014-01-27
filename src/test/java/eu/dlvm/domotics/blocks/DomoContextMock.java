package eu.dlvm.domotics.blocks;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.IHardwareIO;

public class DomoContextMock implements IDomoticContext {

	public IHardwareIO hw;
	public List<Sensor> sensors = new ArrayList<Sensor>(64);
	public List<Actuator> actuators = new ArrayList<Actuator>(32);
	public List<Controller> controllers = new ArrayList<Controller>(32);

	public DomoContextMock(IHardwareIO hw) {
		this.hw = hw;
	}

	@Override
	public IHardwareIO getHw() {
		return hw;
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
		for (IUserInterfaceAPI aa : actuators) {
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

	public long getLoopSequence() {
		return 0;
	}
}
