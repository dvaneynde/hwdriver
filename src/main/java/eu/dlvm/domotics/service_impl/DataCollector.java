package eu.dlvm.domotics.service_impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service.UiInfo;
import eu.dlvm.domotics.service.RestService;

public class DataCollector {

	private static final Logger log = LoggerFactory.getLogger(RestService.class);
	private static int countInstances = 0;

	private static Data data = null;

	public DataCollector() {
		countInstances++;
		log.info("Count instances: " + countInstances);
	}

	public static Map<String, Boolean> getGroup2Status() {
		ensureModelDataFilledIn();
		return data.groupOn;
	}
	
	private static synchronized void ensureModelDataFilledIn() {
		try {
			if (data == null) {
				data = new Data();
				List<IUserInterfaceAPI> actuators = Domotic.singleton().getUiCapableBlocks();
				List<String> groupnames = new ArrayList<>();
				Map<String, List<UiInfo>> groupname2blockinfos = new HashMap<>();
				data.setGroupNames(groupnames);
				data.setGroupname2infos(groupname2blockinfos);
				// Opbouw structuren
				for (IUserInterfaceAPI a : actuators) {
					if (a.getUi() == null)
						continue;
					StringTokenizer st = new StringTokenizer(a.getUi(), ":");
					log.info("TEMP ui="+a.getUi());
					String groupName = st.nextToken();
					int idx = Integer.parseInt(st.nextToken());
					if (!groupnames.contains(groupName)) {
						groupnames.add(groupName);
						groupname2blockinfos.put(groupName, new ArrayList<UiInfo>());
					}
					List<UiInfo> blockinfos = groupname2blockinfos.get(groupName);
					if (idx >= blockinfos.size()) {
						for (int i = blockinfos.size(); i < idx; i++)
							blockinfos.add(null);
						blockinfos.add(a.getBlockInfo());
					} else {
						blockinfos.add(idx, a.getBlockInfo());
					}
				}
				// Opkuis structuren, d.i. null eruithalen
				for (String groupName : data.getGroupNames()) {
					List<UiInfo> newBlockinfos = new ArrayList<>();
					for (UiInfo oldBlockInfo : data.getGroupname2infos().get(groupName)) {
						if (oldBlockInfo != null)
							newBlockinfos.add(oldBlockInfo);
					}
					data.getGroupname2infos().put(groupName, newBlockinfos);
				}
				log.info("Web model data first-time initialized. Data=" + data);
			} else {
				for (List<UiInfo> list : data.groupname2infos.values()) {
					for (int i = 0; i < list.size(); i++) {
						IUserInterfaceAPI act = Domotic.singleton().findUiCapable(list.get(i).getName());
						list.set(i, act.getBlockInfo());
					}
				}
			}
			Map<String, Boolean> onMap = new HashMap<>(data.getGroupNames().size());
			for (String group : data.getGroupname2infos().keySet()) {
				boolean groupOn = false;
				for (UiInfo info : data.getGroupname2infos().get(group)) {
					// if (info.getParms().containsKey("on") &&
					// info.getParms().get("on").equals("1")) {
					if (info.isOn()) {
						groupOn = true;
						break;
					}
				}
				onMap.put(group, groupOn);
			}
			data.setGroupOn(onMap);
			data.setTitle("Domotica");
		} catch (Exception e) {
			log.error("Unexpected exception.", e);
			throw new RuntimeException("Server Error - check log.");
		}
	}
}
