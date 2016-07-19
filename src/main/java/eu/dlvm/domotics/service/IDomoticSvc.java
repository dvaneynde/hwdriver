package eu.dlvm.domotics.service;

import java.util.List;
import java.util.Map;

//@Path("domo")
public interface IDomoticSvc {
	//@GET
	//@Produces(MediaType.TEXT_PLAIN)
	public String getIt();

//	@Path("screenRobot")
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public ScreenRobotInfo screenRobotInfo();
	
//	@Path("screenRobotUpdateText")
//	@POST
//	@Consumes(MediaType.TEXT_PLAIN)
//	@Produces(MediaType.TEXT_PLAIN)
	public String updateScreenRobot(String info);
	
//	@Path("screenRobotUpdate")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.TEXT_PLAIN)
	public String updateScreenRobot(ScreenRobotUpdateInfo info);
	
//	@Path("actuators_txt")
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public String listActuatorsTxt();

//	@Path("actuators")
//	@Produces(MediaType.APPLICATION_JSON)
//	@GET
	public List<BlockInfo> listActuators();

//	@Path("groups")
//	@Produces(MediaType.APPLICATION_JSON)
//	@GET
	public Map<String, Boolean> getGroups2Status();

	/**
	 * TODO return result must be new state of Block
	 * @param name
	 * @param action on, off or integer which is level
	 */
//	@Path("act/{name}/{action}")
//	@GET
	public void updateActuator(/*@PathParam("name")*/ String name, /*@PathParam("action")*/ String action);

//	@Path("quickies")
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public String quickies();

//	@Path("quick/{name}")
//	@GET
	public void quick(/*@PathParam("name")*/ String name);
	
//	@Path("shutdown")
//	@GET
	public void shutdown();

//	@Path("ping/{token}")
//	@GET
	public String ping(/*@PathParam("token")*/ String token);

	//	@Path("home")
//	@Produces({MediaType.TEXT_HTML})
//	@GET
//	public InputStream viewHome();
//	
}
;