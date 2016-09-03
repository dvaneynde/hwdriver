package eu.dlvm.domotics.service_impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.service.Resource;
import eu.dlvm.domotics.service.RestService;

//@SuppressWarnings("restriction")
public class ServiceServer {

	private static final Logger log = LoggerFactory.getLogger(ServiceServer.class);
	private Server server;

	public static class TimeSocketCreator implements WebSocketCreator {
		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			return new TimeSocket();
		}
	}

	public static class UiSocketCreator implements WebSocketCreator {
		private IDomoticContext domoContext;

		public UiSocketCreator(IDomoticContext domoContext) {
			this.domoContext = domoContext;
		}

		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			UiUpdatorSocket uiUpdatorSocket = new UiUpdatorSocket(domoContext);
			return uiUpdatorSocket;
		}
	}

	public void start(IDomoticContext domoContext) {
		server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8080);
		server.addConnector(connector);

		try {

			// The location of the webapp base resource (for resources and
			// static file serving)
			Path webRootPath = new File("src/main/resources/static-root/").toPath().toRealPath();
			//Path webRootPath = new File("src/main/resources/static-root/").toPath().toRealPath();
			//Path webRootPath = new File("webapps/static-root/").toPath().toRealPath();

			// TODO html en js embedden?
			// http://download.eclipse.org/jetty/9.3.9.v20160517/apidocs/org/eclipse/jetty/util/resource/URLResource.html
			// FileResource
			// InputStream input = getClass().getResourceAsStream("/classpath/to/my/file");

			// Setup the basic application "context" for this application at "/"
			// This is also known as the handler tree (in jetty speak)
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			context.setBaseResource(new PathResource(webRootPath));
			context.setWelcomeFiles(new String[] { "index.html" });
			server.setHandler(context);

			/*
			 * // Add the websocket filter WebSocketUpgradeFilter wsfilter =
			 * WebSocketUpgradeFilter.configureContext(context); // Configure
			 * websocket behavior
			 * wsfilter.getFactory().getPolicy().setIdleTimeout(5000); // Add
			 * websocket mapping wsfilter.addMapping(new
			 * ServletPathSpec("/time/"), new TimeSocketCreator());
			 */

			// Add the websocket filter
			WebSocketUpgradeFilter wsfilter = WebSocketUpgradeFilter.configureContext(context);
			// Configure websocket behavior
			wsfilter.getFactory().getPolicy().setIdleTimeout(50000); // TODO default seems to be 5000
			// Add websocket mapping
			wsfilter.addMapping(new ServletPathSpec("/time/"), new UiSocketCreator(domoContext));

			// Add time servlet
			context.addServlet(TimeServlet.class, "/time/");

			// Toevoegen Jersey... spannend !
			// https://www.acando.no/thedailypassion/200555/a-rest-service-with-jetty-and-jersey
			Set<Class<?>> services = new HashSet<>();
			services.add(RestService.class);
			services.add(Resource.class);
			ResourceConfig config = new ResourceConfig(services);
			config.register(JacksonFeature.class);
			ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer(config));
			context.addServlet(jerseyServletHolder, "/rest/*");

			// Add default servlet
			ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
			holderDefault.setInitParameter("dirAllowed", "true");
			context.addServlet(holderDefault, "/*");

			server.start();
			//server.join();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	public void stop() {
		// http://stackoverflow.com/questions/928211/how-to-shutdown-com-sun-net-httpserver-httpserver
		try {
			server.stop();
		} catch (Exception e) {
			log.error("Unexpected exception, ignored.", e);
		}
		log.info("HTTP Server stopped.");
	}

	public static void main(String[] args) throws IOException, Exception {
		ServiceServer ss = new ServiceServer();
		ss.start(null);
		System.out.println(String.format("Server app started.\nHit enter to stop it..."));
		System.in.read();
		ss.stop();
	}

}
