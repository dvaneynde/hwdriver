package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Singleton;



import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service_impl.DataCollector;
import eu.dlvm.domotics.service_impl.Quickie;
import eu.dlvm.domotics.service_impl.QuickieService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("")
public class RestService {

	private static Logger Log = LoggerFactory.getLogger(RestService.class);
	private static int countInstances = 0;
	private QuickieService qSvc;

	public RestService() {
		countInstances++;
		Log.info("Count instances: " + countInstances);
		qSvc = new QuickieService();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it! This is the prefix for all REST functions.";
	}

	@Path("ping/{token}")
	@GET
	public String ping(@PathParam("token") String token) {
		return token + " - " + new java.util.Date();
	}

	@Path("shutdown")
	@GET
	public void shutdown() {
		Domotic.singleton().requestStop();
		Log.info("Shutdown of domotic requested.");
	}

	@Path("screenRobotUpdate")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateScreenRobot(ScreenRobotUpdateInfo info) {
		Log.info("Got screenRobot update, info='" + info + "'");
		return Boolean.toString(info.isRobotOn());
	}

	@Path("screenRobotUpdateText")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateScreenRobot(String info) {
		Log.info("Got screenRobot update, string='" + info + "'");
		//return "OK - robot is "+(info.isRobotOn()?"ON":"OFF")+".\n";
		//return "\"OK, got '"+info+"'\"";
		return "\"OK\"";
	}

	// TODO rename
	@Path("actuators_txt")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listActuatorsTxt() {
		StringBuffer sb = new StringBuffer();
		for (IUserInterfaceAPI a : Domotic.singleton().getUiCapableBlocks())
			sb.append(a.getName()).append('\n');
		return sb.toString();
	}

	// TODO rename
	// TODO zou het kunnen dat getBlockInfo 2 keer per browser refresh wordt opgeroepen? 2 verschillende threads...
	@Path("actuators")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<BlockInfo> listActuators() {
		List<BlockInfo> list = new ArrayList<>();
		try {
			for (IUserInterfaceAPI a : Domotic.singleton().getUiCapableBlocks()) {
				BlockInfo aj = a.getBlockInfo();
				if (aj != null)
					list.add(aj);
			}
		} catch (Throwable e) {
			Log.warn("listActuators() failed", e);
			list.clear();
		}
		Log.debug("listActuators() returns: " + list);
		return list;
	}

	@Path("groups")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Map<String, Boolean> getGroups2Status() {
		return DataCollector.getGroup2Status();
	}

	/**
	 * TODO return result must be new state of Block
	 * 
	 * @param name
	 * @param action
	 *            on, off or integer which is level
	 */
	@Path("act/{name}/{action}")
	@GET
	public void updateActuator(@PathParam("name") String name, @PathParam("action") String action) {
		// TODO debug
		Log.info("Domotic API: got update actuator '" + name + "' action='" + action + "'");
		IUserInterfaceAPI act = Domotic.singleton().findUiCapable(name);
		if (act == null) {
			// TODO iets terugsturen?
			Log.warn("Could not find actuator " + name);
			return;
		}
		act.update(action);
	}

	@Path("quickies")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String quickies() {
		return qSvc.listQuickyNamesNewlineSeparated();
	}

	@Path("quick/{name}")
	@GET
	public void quick(@PathParam("name") String name) {
		Log.info("Domotic API: got quickie '" + name + "'");
		Quickie q = qSvc.find(name);
		if (q != null) {
			for (Quickie.KeyVal kv : q.actions) {
				updateActuator(kv.key, kv.val);
			}
		} else
			Log.warn("Domotic API: quickie '" + name + "' not found.");

	}

	@Path("screenRobot")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ScreenRobotInfo screenRobotInfo() {
		Random r = new Random();
		ScreenRobotInfo info = new ScreenRobotInfo(r.nextBoolean(), r.nextInt(4000), r.nextFloat() * 15);
		return info;
	}

}
