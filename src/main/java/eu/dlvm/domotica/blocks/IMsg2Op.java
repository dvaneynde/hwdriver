package eu.dlvm.domotica.blocks;

public interface IMsg2Op {

	public void execute(String op);
	
	// TODO uncomment, and OperationExecutor must use this to check target events
	// public String[] allowedOps();
}
