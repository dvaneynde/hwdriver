package eu.dlvm.domotics.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotica.service.IDomoticSvc;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

public class Service implements IDomoticSvc {

	static Logger LOG = Logger.getLogger(Service.class);

	private QuickieService qSvc;

	public Service() {
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
		LOG.info("Domotic API: got update actuator '" + name + "' action=" + action);
		Actuator act = Domotic.singleton().findActuator(name);
		if (act == null) {
			// TODO iets terugsturen?
			LOG.warn("Could not find actuator " + name);
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
		LOG.info("Domotic API: got quickie '" + name + "'");
		Quickie q = qSvc.find(name);
		if (q != null) {
			for (Quickie.KeyVal kv : q.actions) {
				updateActuator(kv.key, kv.val);
			}
		} else
			LOG.warn("Domotic API: quickie '" + name + "' not found.");

	}

	@Override
	public InputStream viewHome() {
		LOG.info("Domotic API: home() called.");
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("home.html");
		if (is == null) {
			try {
				LOG.warn("home(), niet in jar gevonden, maar in src/main/resources.");
				is = new FileInputStream("src/main/resources/home.html");
			} catch (FileNotFoundException e) {
				LOG.error("Could not find home.html, neither in jar or development location.");
				return null;
			}
		}
		return is;
	}

}
