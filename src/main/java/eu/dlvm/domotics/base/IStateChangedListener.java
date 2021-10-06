package eu.dlvm.domotics.base;

/**
 * Listens for state-has-changed.
 * @author dirk
 */
public interface IStateChangedListener {

	/** Just to keep track in easy way of listeners. */
	public int getId();
	/** State has changed. */
	public void updateUi();
}
