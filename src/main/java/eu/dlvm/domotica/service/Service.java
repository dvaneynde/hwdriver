package eu.dlvm.domotica.service;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotica.blocks.Actuator;
import eu.dlvm.domotica.blocks.Domotic;

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
