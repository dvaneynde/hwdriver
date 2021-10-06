package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.service.uidata.UiInfo;

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

	@Path("shutdown")
	@GET
	public void shutdown() {
		Domotic.singleton().requestStop();
		Log.info("Shutdown of domotic requested.");
	}

	@Path("statuses_txt")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listActuatorsTxt() {
		StringBuffer sb = new StringBuffer();
		for (IUiCapableBlock a : Domotic.singleton().getUiCapableBlocks())
			sb.append(a.getUiInfo().getName()).append(" - ").append(a.getUiInfo().getDescription()).append('\n');
		return sb.toString();
	}

	@Path("statuses")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<UiInfo> listActuators() {
		List<UiInfo> list = new ArrayList<>();
		try {
			for (IUiCapableBlock a : Domotic.singleton().getUiCapableBlocks()) {
				UiInfo aj = a.getUiInfo();
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

	/**
	 * @param name
	 * @param action
	 *            on, off or integer which is level
	 */
	@Path("act/{name}/{action}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<UiInfo> updateActuator(@PathParam("name") String name, @PathParam("action") String action) {
		// TODO debug
		Log.info("Domotic API: got update actuator '" + name + "' action='" + action + "'");
		IUiCapableBlock act = Domotic.singleton().findUiCapable(name);
		if (act == null) {
			// TODO iets terugsturen?
			Log.warn("Could not find actuator " + name);
		} else {
			act.update(action);
		}
		return listActuators();
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

}
