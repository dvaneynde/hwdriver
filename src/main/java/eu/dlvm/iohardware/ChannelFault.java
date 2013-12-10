package eu.dlvm.iohardware;

/**
 * Something went wrong in the channel, currently tcp.
 * @author dirkv
 */
public class ChannelFault extends Exception {

	private static final long serialVersionUID = 1L;
	private boolean recoverable = false;
	
	public ChannelFault(String message, boolean recoverable) {
		super(message);
	}
	public boolean isRecoverable() {
		return recoverable;
	}
	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

}
