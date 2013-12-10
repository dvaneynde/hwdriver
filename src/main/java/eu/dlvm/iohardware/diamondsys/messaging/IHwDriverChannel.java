package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.List;

import eu.dlvm.iohardware.ChannelFault;

public interface IHwDriverChannel {

	public abstract void connect() throws ChannelFault;

	/**
	 * Sends a string and then receives one.
	 * 
	 * @param stringToSend
	 *            String first sent to Hardware Driver. Must be line terminated,
	 *            this function adds a new line which must be an empty line.
	 * @return List of received lines. Can be empty list, meaning driver had
	 *         nothing to tell and is waiting for more food.
	 */
	public abstract List<String> sendAndRecv(String stringToSend) throws ChannelFault;

	public abstract void disconnect();

}