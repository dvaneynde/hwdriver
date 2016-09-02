package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.List;

import eu.dlvm.iohardware.ChannelFault;

public interface IHwDriverChannel {

	public enum Reason {
		INIT, INPUT, OUTPUT, STOP
	}

	void connect() throws ChannelFault;

	/**
	 * Sends a string and then receives one.
	 * 
	 * @param stringToSend
	 *            String first sent to Hardware Driver. Must be line terminated,
	 *            this function adds a new line which must be an empty line.
	 * @return List of received lines. Can be empty list, meaning driver had
	 *         nothing to tell and is waiting for more food.
	 */
	List<String> sendAndRecv(String stringToSend, Reason reason) throws ChannelFault;

	void disconnect();

}
