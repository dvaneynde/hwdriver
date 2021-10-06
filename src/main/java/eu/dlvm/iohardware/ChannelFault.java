package eu.dlvm.iohardware;

/**
 * Something went wrong in the channel, currently tcp.
 * @author dirkv
 */
public class ChannelFault extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ChannelFault(String message) {
		super(message);
	}
}
