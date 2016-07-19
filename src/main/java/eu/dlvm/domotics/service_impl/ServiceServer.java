package eu.dlvm.domotics.service_impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.eclipse.jetty.websocket.server.pathmap.ServletPathSpec;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

//@SuppressWarnings("restriction")
public class ServiceServer {

	private static Logger log = Logger.getLogger(ServiceServer.class);

	private Server server;

	public static class TimeSocketCreator implements WebSocketCreator {
		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			return new JettyTimeSocket();
		}
	}

	public void start() {
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8080);
		server.addConnector(connector);

		try {

			// The location of the webapp base resource (for resources and
			// static file serving)
			Path webRootPath = new File("webapps/static-root/").toPath().toRealPath();

			// Setup the basic application "context" for this application at "/"
			// This is also known as the handler tree (in jetty speak)
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			context.setBaseResource(new PathResource(webRootPath));
			context.setWelcomeFiles(new String[] { "index.html" });
			server.setHandler(context);

			// Add the websocket filter
			WebSocketUpgradeFilter wsfilter = WebSocketUpgradeFilter.configureContext(context);
			// Configure websocket behavior
			wsfilter.getFactory().getPolicy().setIdleTimeout(5000);
			// Add websocket mapping
			wsfilter.addMapping(new ServletPathSpec("/time/"), new TimeSocketCreator());

			// Add time servlet
			context.addServlet(TimeServlet.class, "/time/");

			// Toevoegen Jersey... spannend !
			// https://www.acando.no/thedailypassion/200555/a-rest-service-with-jetty-and-jersey
			ResourceConfig config = new ResourceConfig();
			config.packages("eu.dlvm.domotics.service"); 
			ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer(config));
			context.addServlet(jerseyServletHolder, "/rest/*");

			// Add default servlet
			ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
			holderDefault.setInitParameter("dirAllowed", "true");
			context.addServlet(holderDefault, "/*");

			server.start();
			server.join();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	public void stop() {
		// http://stackoverflow.com/questions/928211/how-to-shutdown-com-sun-net-httpserver-httpserver
		try {
			server.stop();
		} catch (Exception e) {
			log.error(e);
		}
		log.info("HTTP Server stopped.");
	}

	public static void main(String[] args) throws IOException, Exception {
		ServiceServer ss = new ServiceServer();
		ss.start();
		System.out.println(String.format("Server app started.\nHit enter to stop it..."));
		System.in.read();
		// ss.stop();
	}

}
