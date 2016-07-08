package eu.dlvm.domotics.service.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.servlet.ServletContainer;

//@SuppressWarnings("restriction")
public class ServiceServer {

	private static Logger log = Logger.getLogger(ServiceServer.class);

	private Server jettyServer;

	// private HttpServer server;
	// public void start() {
	// URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
	// Set<Class<?>> services = new HashSet<>();
	// services.add(RestService.class);
	// services.add(HtmlService.class);
	// ResourceConfig config = new ResourceConfig(services);
	// config.register(org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature.class);
	// config.register(JacksonFeature.class);
	// server = JdkHttpServerFactory.createHttpServer(baseUri, config);
	// log.info("HTTP Server started.");
	// }

	public void start() {
		Set<Class<?>> services = new HashSet<>();
		services.add(RestService.class);
		services.add(HtmlService.class);
		ResourceConfig config = new ResourceConfig(services);
		ServletHolder servlet = new ServletHolder(new ServletContainer(config));
		config.register(FreemarkerMvcFeature.class);
		config.register(JacksonFeature.class);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(servlet, "/*");
		jettyServer = new Server(8080);
		jettyServer.setHandler(context);

		// ServletHolder jerseyServlet =
		// context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class,
		// "/*");
		// jerseyServlet.setInitOrder(0);
		// jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
		// RestService.class.getCanonicalName());

		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			log.error(e);
		} finally {
			jettyServer.destroy();
		}
	}

	public void stop() {
		// http://stackoverflow.com/questions/928211/how-to-shutdown-com-sun-net-httpserver-httpserver
		try {
			jettyServer.stop();
		} catch (Exception e) {
			log.error(e);
		}
		log.info("HTTP Server stopped.");
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, Exception {
		ServiceServer ss = new ServiceServer();
		ss.start();
		System.out.println(String.format("Jersey app started.\nHit enter to stop it..."));
		System.in.read();
		// ss.stop();
	}
}
