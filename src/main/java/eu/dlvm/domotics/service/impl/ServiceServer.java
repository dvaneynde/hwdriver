package eu.dlvm.domotics.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class ServiceServer {

	private static Logger log = Logger.getLogger(ServiceServer.class);
	private HttpServer server;

	public void start() {
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
		Set<Class<?>> services = new HashSet<>();
		services.add(RestService.class);
		services.add(HtmlService.class);
		ResourceConfig config = new ResourceConfig(services);
		config.register(org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature.class);
		config.register(JacksonFeature.class);
		server = JdkHttpServerFactory.createHttpServer(baseUri, config);
		log.info("HTTP Server started.");
	}

	public void stop() {
		// http://stackoverflow.com/questions/928211/how-to-shutdown-com-sun-net-httpserver-httpserver
		server.stop(1);
		ExecutorService threadpool = (ExecutorService)server.getExecutor();
		threadpool.shutdownNow();
		log.info("HTTP Server stopped.");
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ServiceServer ss = new ServiceServer();
		ss.start();
		System.out.println(String.format("Jersey app started.\nHit enter to stop it..."));
		System.in.read();
		ss.stop();
	}
}
