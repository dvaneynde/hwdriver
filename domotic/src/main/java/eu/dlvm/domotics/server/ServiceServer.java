package eu.dlvm.domotics.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;

import eu.dlvm.domotics.base.IStateChangeRegistrar;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
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

import eu.dlvm.domotics.service.RestService;
import eu.dlvm.domotics.service.UiStateUpdatorSocket;

/**
 * Serves whatever stuff via http (rest) or websocket.
 * 
 * @author dirk
 *
 */
public class ServiceServer {

	private static final Logger log = LoggerFactory.getLogger(ServiceServer.class);
	private Server server;
	private File rootHtmlFile;

	public ServiceServer(File rootHtmlFile) {
		this.rootHtmlFile = rootHtmlFile;
	}

	public static class UiSocketCreator implements WebSocketCreator {
		private IStateChangeRegistrar registrar;
		
		public UiSocketCreator(IStateChangeRegistrar registrar) {
			this.registrar = registrar;
		}

		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			InetSocketAddress remoteAddress = req.getRemoteSocketAddress();
			log.debug("Creating websocket connection, remote address="+remoteAddress.toString());
			UiStateUpdatorSocket uiStateUpdatorSocket = new UiStateUpdatorSocket(registrar);
			return uiStateUpdatorSocket;
		}
	}

	public void start(IStateChangeRegistrar stateChangeRegistrar) {
		server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8080);
		server.addConnector(connector);

		try {
			// Setup the basic application "context" for this application at "/"
			// This is also known as the handler tree (in jetty speak)
			ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
			contextHandler.setContextPath("/");
			//Resource staticRoot = Resource.newClassPathResource("static-root");
			Resource staticRoot = Resource.newResource(rootHtmlFile);
			contextHandler.setBaseResource(staticRoot);
			contextHandler.setWelcomeFiles(new String[] { "index.html" });
			server.setHandler(contextHandler);

			// Add the filter, and then use the provided FilterHolder to configure it
			FilterHolder cors = contextHandler.addFilter(CrossOriginFilter.class,"/*",EnumSet.of(DispatcherType.REQUEST));
			cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
			cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
			cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
			cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");

			// Add the websocket filter
			WebSocketUpgradeFilter wsfilter = WebSocketUpgradeFilter.configureContext(contextHandler);
			// Configure websocket behavior
			wsfilter.getFactory().getPolicy().setIdleTimeout(5000);
			// Add websocket mapping
			wsfilter.addMapping(new ServletPathSpec("/status/"), new UiSocketCreator(stateChangeRegistrar));

			// Add REST interface handler
			// https://www.acando.no/thedailypassion/200555/a-rest-service-with-jetty-and-jersey
			Set<Class<?>> services = new HashSet<>();
			services.add(RestService.class);
			ResourceConfig config = new ResourceConfig(services);
			config.register(JacksonFeature.class);
			ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer(config));
			contextHandler.addServlet(jerseyServletHolder, "/rest/*");

			// Add default servlet
			ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
			holderDefault.setInitParameter("dirAllowed", "true");
			contextHandler.addServlet(holderDefault, "/*");

			// TODO
			// https://stackoverflow.com/questions/28190198/cross-origin-filter-with-embedded-jetty
			// Lijkt erop dat ik 9.4 nodig heb ipv 9.3?
				
			server.start();
			//server.join();
		} catch (Throwable t) {
			log.error("Error starting server.", t);
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
		ServiceServer ss = new ServiceServer(new File("/Users/dirk/dev/ws-domotica/domotic"));
		ss.start(null);
		System.out.println(String.format("Server app started.\nHit enter to stop it..."));
		System.in.read();
		ss.stop();
	}
}
