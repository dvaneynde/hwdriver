package eu.dlvm.iohardware.diamondsys.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.ChannelFault;

/**
 * Communicates via TCP with a Hardware Driver.
 * 
 * @author dirk vaneynde
 * 
 */
public class HwDriverTcpChannel implements IHwDriverChannel {

	static Logger log = LoggerFactory.getLogger(HwDriverTcpChannel.class);

	public static final int DEFAULT_DRIVER_PORT=4444;
    
    private String serverHostname;
	private int serverPort;
	private Socket socket;
	private int readTimeout;

	/**
	 * 
	 * @param serverHostname Host of hardware driver, typically localhost
	 * @param serverPort Port on which hardware driver communicates, default is {@link #DEFAULT_DRIVER_PORT}
	 * @param readTimeout Should be less than looptime
	 */
	public HwDriverTcpChannel(String serverHostname, int serverPort, int readTimeout) {
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.readTimeout = readTimeout;
	}

	/**
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
			log.error("Connecting to " + serverHostname + ':' + serverPort
					+ " failed.", e);
			throw new ChannelFault("Cannot connect to "+serverHostname+':'+serverPort+", probably not recoverable.");
		}
	}

	/**
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#sendAndRecv(java.lang.String)
	 */
	@Override
	public List<String> sendAndRecv(String stringToSend, Reason reason) throws ChannelFault {
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
			throw new ChannelFault("Read timeout, probably driver gone? Timeout value is:"+readTimeout);
		} catch (IOException e) {
			log.error("Error communicating with Hardware Driver.", e);
			throw new ChannelFault("Messages lost in driver interaction. Error message: "+e.getMessage());
		}
	}

	/**
	 * @see eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel#disconnect()
	 */
	@Override
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			log.warn("TCP socket close to driver throws IO exception.", e);
		}
		log.info("Connection to driver closed. socket="+socket);
	}

}
