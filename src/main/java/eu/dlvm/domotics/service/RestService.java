package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service_impl.Quickie;
import eu.dlvm.domotics.service_impl.QuickieService;


//@Singleton
public class RestService implements IDomoticSvc {

	private static Logger Log = Logger.getLogger(RestService.class);
	private static int countInstances = 0;

	private QuickieService qSvc;

	public RestService() {
		countInstances++;
		Log.info("Count instances: " + countInstances);
		qSvc = new QuickieService();
	}

	@Override
	public String getIt() {
		return "Got it!";
	}

	
	@Override
	public String updateScreenRobot(ScreenRobotUpdateInfo info) {
		Log.info("Got screenRobot update, info='"+info+"'");
		return Boolean.toString(info.isRobotOn());
	}

	@Override
	public String updateScreenRobot(String info) {
		Log.info("Got screenRobot update, string='"+info+"'");
		//return "OK - robot is "+(info.isRobotOn()?"ON":"OFF")+".\n";
		//return "\"OK, got '"+info+"'\"";
		return "\"OK\"";
	}


	// TODO rename
	@Override
	public String listActuatorsTxt() {
		StringBuffer sb = new StringBuffer();
		for (IUserInterfaceAPI a : Domotic.singleton().getUiCapableBlocks())
			sb.append(a.getName()).append('\n');
		return sb.toString();
	}

	// TODO rename
	// TODO zou het kunnen dat getBlockInfo 2 keer per browser refresh wordt opgeroepen? 2 verschillende threads...
	@Override
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
		Log.debug("listActuators() returns: "+list);
		return list;
	}

	@Override
	public Map<String, Boolean> getGroups2Status() {
		return HtmlService.getGroup2Status();
	}

	// TODO rename
	@Override
	public void updateActuator(String name, String action) {
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

	@Override
	public void shutdown() {
		Domotic.singleton().requestStop();
		Log.info("Shutdown of domotic requested.");
	}

	@Override
	public String ping(String token) {
		return token;
	}

	@Override
	public ScreenRobotInfo screenRobotInfo() {
		Random r = new Random();
		ScreenRobotInfo info = new ScreenRobotInfo(r.nextBoolean(), r.nextInt(4000), r.nextFloat()*15);
		return info;
	}

}
