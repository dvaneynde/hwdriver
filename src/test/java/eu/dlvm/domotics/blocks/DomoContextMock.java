package eu.dlvm.domotics.blocks;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.IHardwareIO;

public class DomoContextMock implements IHardwareAccess {

	public IHardwareIO hw;
	public List<Sensor> sensors = new ArrayList<Sensor>(64);
	public List<Actuator> actuators = new ArrayList<Actuator>(32);

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
		for (Actuator aa : actuators) {
			if (aa == a) {
				assert (false);
				return;
			}
		}
		actuators.add(a);
	}
}
