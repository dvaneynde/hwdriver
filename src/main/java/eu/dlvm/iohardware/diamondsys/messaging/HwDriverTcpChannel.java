package eu.dlvm.iohardware.diamondsys.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Communicates via TCP with a Hardware Driver.
 * 
 * @author dirk vaneynde
 * 
 */
public class HwDriverTcpChannel implements IHwDriverChannel {

	static Logger log = Logger.getLogger(HwDriverTcpChannel.class);

	public static final int DEFAULT_DRIVER_PORT=4444;
    
    private String serverHostname;
	private int serverPort;
	private Socket socket;

	/**
	 * 
	 * @param processor
	 * @param sleepMilliSecsIfNothingReceived
	 */
	public HwDriverTcpChannel(String serverHostname, int serverPort) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
	}

	/* (non-Javadoc)
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#connect()
	 */
	@Override
	public void connect() {
		try {
			socket = new Socket(serverHostname, serverPort);
			log.debug("HwDriver socket to communicate with is: "
					+ socket.toString());
		} catch (IOException e) {
			log.fatal("Connecting to " + serverHostname + ':' + serverPort
					+ " failed.", e);
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#sendAndRecv(java.lang.String)
	 */
	@Override
	public List<String> sendAndRecv(String stringToSend) {
		try {
			List<String> recvd = new ArrayList<String>();
			// Send stringToSend to Hardware Driver
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			if (log.isDebugEnabled())
				log.debug("  Write to HW Driver:\n[" + stringToSend + "]");
			out.print(stringToSend);
			out.flush(); // TEST TODO
			// Now receive any results, skipping empty last line
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (log.isDebugEnabled())
					log.debug("  Read from HW Driver:\n[" + line + "]");
				if (line.length() == 0)
					break;
				recvd.add(line);
			}
			return recvd;
		} catch (IOException e) {
			log.fatal("Error communicating with Hardware Driver.", e);
			throw new RuntimeException(e);

		}
	}

	/* (non-Javadoc)
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#disconnect()
	 */
	@Override
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			log.warn("disconnect() throws IO exception, ignored.", e);
		}
	}

}