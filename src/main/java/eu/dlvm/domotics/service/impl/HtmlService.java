package eu.dlvm.domotics.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service.BlockInfo;

@Singleton
@Path("domo")
public class HtmlService {

	private static Logger Log = Logger.getLogger(RestService.class);
	private static int countInstances = 0;

	private static Data data = null;

	public HtmlService() {
		countInstances++;
		Log.info("Count instances: " + countInstances);
	}

	private synchronized void ensureModelDataFilledIn() {
		try {
			if (data == null) {
				data = new Data();
				List<Actuator> actuators = Domotic.singleton().getActuators();
				List<String> groupnames = new ArrayList<>();
				Map<String, List<BlockInfo>> groupname2blockinfos = new HashMap<>();
				data.setGroupNames(groupnames);
				data.setGroupname2infos(groupname2blockinfos);
				// Opbouw structuren
				for (Actuator a : actuators) {
					if (a.getUi() == null)
						continue;
					StringTokenizer st = new StringTokenizer(a.getUi(), ":");
					String groupName = st.nextToken();
					int idx = Integer.parseInt(st.nextToken());
					if (!groupnames.contains(groupName)) {
						groupnames.add(groupName);
						groupname2blockinfos.put(groupName, new ArrayList<BlockInfo>());
					}
					List<BlockInfo> blockinfos = groupname2blockinfos.get(groupName);
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
					List<BlockInfo> newBlockinfos = new ArrayList<>();
					for (BlockInfo oldBlockInfo : data.getGroupname2infos().get(groupName)) {
						if (oldBlockInfo != null)
							newBlockinfos.add(oldBlockInfo);
					}
					data.getGroupname2infos().put(groupName, newBlockinfos);
				}
				Log.info("Web model data first-time initialized. Data=" + data);
			} else {
				for (List<BlockInfo> list : data.groupname2infos.values()) {
					for (int i = 0; i < list.size(); i++) {
						IUserInterfaceAPI act = Domotic.singleton().findActuator(list.get(i).getName());
						list.set(i, act.getBlockInfo());
					}
				}
			}
			Map<String, Boolean> onMap = new HashMap<>(data.getGroupNames().size());
			for (String group : data.getGroupname2infos().keySet()) {
				boolean groupOn = false;
				for (BlockInfo info : data.getGroupname2infos().get(group)) {
					if (info.getParms().containsKey("on") && info.getParms().get("on").equals("1")) {
						groupOn = true;
						break;
					}
				}
				onMap.put(group, groupOn);
			}
			data.setGroupOn(onMap);
			data.setTitle(new Date().toLocaleString());
		} catch (Exception e) {
			Log.error("Unexpected exception.", e);
			throw new RuntimeException("Server Error - check log.");
		}
	}

	@Path("home")
	@Produces({ javax.ws.rs.core.MediaType.TEXT_HTML })
	@GET
	public Viewable viewThuis() {
		Log.info("viewThuis() opgeroepen");
		ensureModelDataFilledIn();
		Viewable v = new Viewable("/index", data);
		Log.info("viewable=" + v);

		return v;
	}

	@Path("js/{name}")
	@Produces("application/javascript")
	@GET
	public InputStream serveJs(@PathParam("name") String name) {
		Log.info("Domotic API: serveJs() called, name=" + name);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("js/" + name);
		if (is == null) {
			try {
				is = new FileInputStream("src/main/resources/js/" + name);
				Log.warn("serveJs(" + name + "), niet in jar gevonden, maar in src/main/resources/js.");
			} catch (FileNotFoundException e) {
				Log.error("Could not find " + name + ", neither in jar or development location.");
				return null;
			}
		}
		return is;
	}

	@Path("css/{name}")
	@Produces("text/css")
	@GET
	public InputStream serveCss(@PathParam("name") String name) {
		Log.info("Domotic API: serveCss() called, name=" + name);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("css/" + name);
		if (is == null) {
			try {
				is = new FileInputStream("src/main/resources/css/" + name);
				Log.warn("serveCss(" + name + "), niet in jar gevonden, maar in src/main/resources/css.");
			} catch (FileNotFoundException e) {
				Log.error("Could not find " + name + ", neither in jar or development location.");
				return null;
			}
		}
		return is;
	}

	@Path("test")
	@Produces({ javax.ws.rs.core.MediaType.TEXT_HTML })
	@GET
	public InputStream serveHtml() {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("home2.html");
		if (is == null) {
			try {
				is = new FileInputStream("src/main/resources/home2.html");
				Log.warn("html niet in jar gevonden, maar in src/main/resources/js.");
			} catch (FileNotFoundException e) {
				Log.error("Could not find html file, neither in jar or development location.");
				return null;
			}
		}
		return is;
	}

}
