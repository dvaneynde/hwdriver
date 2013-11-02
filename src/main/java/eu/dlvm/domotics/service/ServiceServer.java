package eu.dlvm.domotics.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class ServiceServer {

	private HttpServer server;

	public void start() {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(getBaseURI()
					.getPort()), 0);
			// create a handler wrapping the JAX-RS application
			HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(
					new JaxRsApplication(), HttpHandler.class);

			// map JAX-RS handler to the server root
			server.createContext(getBaseURI().getPath(), handler);

			// start the server
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		server.stop(0);
	}

	public static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(8085).build();
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
		System.out.println(String.format(
				"Jersey app started.\nHit enter to stop it..."));
		System.in.read();
		ss.stop();
	}
}
