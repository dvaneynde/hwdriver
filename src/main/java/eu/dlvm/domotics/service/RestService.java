package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotica.service.IDomoticSvc;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

@Singleton
public class RestService implements IDomoticSvc {

	private static Logger Log = Logger.getLogger(RestService.class);
	private static int countInstances = 0;

	private QuickieService qSvc;

	public RestService() {
		countInstances++;
		Log.info("Count instances: "+countInstances);
		qSvc = new QuickieService();
	}

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
			BlockInfo aj = a.getBlockInfo();
			if (aj != null)
				list.add(aj);
		}
		return list;
	}

	@Override
	public void updateActuator(String name, String action) {
		// TODO debug
		Log.info("Domotic API: got update actuator '" + name + "' action='" + action+"'");
		Actuator act = Domotic.singleton().findActuator(name);
		if (act == null) {
			// TODO iets terugsturen?
			Log.warn("Could not find actuator " + name);
			return;
		}
		act.update(action);
	}

	@Override
	public String quickies() {
		return qSvc.listQuickyNamesNewlineSeparated();
	}

	@Override
	public void quick(String name) {
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
