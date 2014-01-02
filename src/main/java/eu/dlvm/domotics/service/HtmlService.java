package eu.dlvm.domotics.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.mvc.Viewable;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Domotic;

@Singleton
@Path("domo2")
public class HtmlService {

	private static Logger Log = Logger.getLogger(RestService.class);
	private static int countInstances = 0;
	
	public HtmlService() {
		countInstances++;
		Log.info("Count instances: "+countInstances);
	}
	
	@Path("home")
	@Produces({ javax.ws.rs.core.MediaType.TEXT_HTML })
	@GET
	public InputStream viewHome() {
		Log.info("Domotic API: home() called.");
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("home.html");
		if (is == null) {
			try {
				Log.warn("home(), niet in jar gevonden, maar in src/main/resources.");
				is = new FileInputStream("src/main/resources/home.html");
			} catch (FileNotFoundException e) {
				Log.error("Could not find home.html, neither in jar or development location.");
				return null;
			}
		}
		return is;
	}

	@Path("thuis")
	@Produces({ javax.ws.rs.core.MediaType.TEXT_HTML })
	@GET
	public Viewable viewThuis() {
		Log.info("thuis opgeroepen");
		Data data = new Data();
		data.setTitle("Alleballe");
		List<BlockInfo> list = new ArrayList<>();
		for (Actuator a : Domotic.singleton().getActuators()) {
			BlockInfo aj = a.getBlockInfo();
			if (aj != null && aj.getParms().containsKey("on"))
				list.add(aj);
		}
		data.setActuators(list);
		Viewable v = new Viewable("/thuis", data);
		Log.info("viewable=" + v);

		return v;
	}

}
