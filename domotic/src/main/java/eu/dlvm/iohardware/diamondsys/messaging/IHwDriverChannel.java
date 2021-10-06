package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.List;

import eu.dlvm.iohardware.ChannelFault;

/**
 * Channel to communicate with Hardware Driver.
 * @author dirk
 */
public interface IHwDriverChannel {

	/**
	 * Reason for {@link IHwDriverChannel#sendAndRecv(String, Reason)}. Was introduced to aid in testing, to give more info to mocks.
	 * @author dirk
	 */
	public enum Reason {
		INIT, INPUT, OUTPUT, STOP
	}

	/**
	 * Connect to Hardware Driver.
	 * @throws ChannelFault
	 */
	void connect() throws ChannelFault;

	/**
	 * Sends a string and then receives one.
	 * 
	 * @param stringToSend
	 *            String first sent to Hardware Driver. Must be line terminated,
	 *            this function adds a new line which must be an empty line.
	 * @param reason
	 * 			  See {@link Reason}
	 * @return List of received lines. Can be empty list, meaning driver had
	 *         nothing to tell and is waiting for more food.
	 */
	List<String> sendAndRecv(String stringToSend, Reason reason) throws ChannelFault;

	/**
	 * Disconnect and closes channel.
	 */
	void disconnect();

}
