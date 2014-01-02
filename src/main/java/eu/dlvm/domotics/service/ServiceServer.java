package eu.dlvm.domotics.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class ServiceServer {

	private HttpServer server;

	public void start() {
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
		Set<Class<?>> services = new HashSet<>();
		services.add(RestService.class);
		services.add(HtmlService.class);
		ResourceConfig config = new ResourceConfig(services);
		config.register(org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature.class);
		server = JdkHttpServerFactory.createHttpServer(baseUri, config);

	}

	public void stop() {
		server.stop(0);
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
