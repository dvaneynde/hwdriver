package eu.dlvm.iohardware.diamondsys.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.ChannelFault;

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
	private int readTimeout;

	/**
	 * 
	 * @param serverHostname
	 * @param serverPort
	 * @param readTimeout
	 */
	public HwDriverTcpChannel(String serverHostname, int serverPort, int readTimeout) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.readTimeout = readTimeout;
	}

	/* (non-Javadoc)
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#connect()
	 */
	@Override
	public void connect() throws ChannelFault {
		try {
			socket = new Socket(serverHostname, serverPort);
			socket.setSoTimeout(readTimeout);
			log.debug("HwDriver socket to communicate with is: "
					+ socket.toString());
		} catch (IOException e) {
			log.fatal("Connecting to " + serverHostname + ':' + serverPort
					+ " failed.", e);
			throw new ChannelFault("Cannot connect to "+serverHostname+':'+serverPort+", probably not recoverable.", false);
		}
	}

	/* (non-Javadoc)
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#sendAndRecv(java.lang.String)
	 */
	@Override
	public List<String> sendAndRecv(String stringToSend) throws ChannelFault {
		try {
			List<String> recvd = new ArrayList<String>();
			// Send stringToSend to Hardware Driver
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			if (log.isDebugEnabled())
				log.debug("  Write to HW Driver:\n[" + stringToSend + "]");
			out.print(stringToSend);
			out.flush(); // TODO nodig?
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
		} catch (SocketTimeoutException e) {
			log.warn("Read timeout, probably driver gone? Timeout value is:"+readTimeout, e);
			throw new ChannelFault("Read timeout, probably driver gone? Timeout value is:"+readTimeout, true);
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
			log.warn("TCP socket close to driver throws IO exception.", e);
		}
	}

}