package eu.dlvm.domotica.blocks;

@SuppressWarnings("serial")
public class PreconditionViolated extends RuntimeException {

	public PreconditionViolated(String msg) {
		super(msg);
	}
	@Override
	public String toString() {
		return "Precondition violated. " + getMessage();
	}
}
