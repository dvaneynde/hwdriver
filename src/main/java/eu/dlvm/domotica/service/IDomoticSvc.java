package eu.dlvm.domotica.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("domo")
public interface IDomoticSvc {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt();

	@Path("actuators_txt")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String listActuatorsTxt();

	@Path("actuators")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<BlockInfo> listActuators();

	/**
	 * TODO return result must be new state of Block
	 * @param name
	 * @param action on, off or integer which is level
	 */
	@Path("act/{name}/{action}")
	@GET
	public void updateActuator(@PathParam("name") String name, @PathParam("action") String action);

	@Path("quickies")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String quickies();

	@Path("quick/{name}")
	@GET
	public void quick(@PathParam("name") String name);
	
	@Path("shutdown")
	@GET
	public void shutdown();

	@Path("ping/{token}")
	@GET
	public String ping(@PathParam("token") String token);

	//	@Path("home")
//	@Produces({MediaType.TEXT_HTML})
//	@GET
//	public InputStream viewHome();
//	
}
