package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotica.service.IDomoticSvc;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

public class Service implements IDomoticSvc {

	@Override
	public String getIt() {
		return "Got it!";
	}

	@Override
	public String listActuatorsTxt() {
		StringBuffer sb = new StringBuffer();
		for (Actuator a : Domotic.singleton().getActuators())
			sb.append(a.getName()).append('\n');
		return sb.toString();
	}

	@Override
	public List<BlockInfo> listActuators() {
		List<BlockInfo> list = new ArrayList<>();
		for (Actuator a : Domotic.singleton().getActuators()) {
			BlockInfo aj = a.getActuatorInfo();
			if (aj != null)
				list.add(aj);
		}
		return list;
	}

}
