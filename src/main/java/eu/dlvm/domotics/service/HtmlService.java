package eu.dlvm.domotics.service;

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

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

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
		if (data != null)
			return;
		data = new Data();
		data.setTitle(new Date().toLocaleString());

		List<Actuator> actuators = Domotic.singleton().getActuators();
		try {
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
				for (BlockInfo oldBlockInfo: data.getGroupname2infos().get(groupName)) {
					if (oldBlockInfo!= null)
						newBlockinfos.add(oldBlockInfo);
				}
				data.getGroupname2infos().put(groupName, newBlockinfos);
			}
		} catch (Exception e) {
			Log.error("Unexpected exception.", e);
			throw new RuntimeException("Server Error - check log.");
		}
		Log.info("Web model data ready. Data=" + data);
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
}
