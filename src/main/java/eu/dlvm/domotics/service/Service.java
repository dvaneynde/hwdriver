package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotica.service.IDomoticSvc;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

//@Path("domo")
public class Service implements IDomoticSvc {

//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it!";
	}

//	@Path("actuators_txt")
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public String listActuatorsTxt() {
		StringBuffer sb = new StringBuffer();
		for (Actuator a : Domotic.singleton().getActuators())
			sb.append(a.getName()).append('\n');
		return sb.toString();
	}

//	@Path("actuators")
//	@Produces(MediaType.APPLICATION_JSON)
//	@GET
	public List<BlockInfo> listActuators() {
		List<BlockInfo> list = new ArrayList<>();
		for (Actuator a : Domotic.singleton().getActuators()) {
			BlockInfo aj = a.getActuatorInfo();
			list.add(aj);
		}
		return list;
	}

}
