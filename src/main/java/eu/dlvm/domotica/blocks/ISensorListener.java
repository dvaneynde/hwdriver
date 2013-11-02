package eu.dlvm.domotica.blocks;

/**
 * Realizations are interested in Sensor events.
 * See {@link Sensor#registerListener(ISensorListener).}
 * @author dirk
 * @deprecated
 */
public interface ISensorListener {
	/**
	 * To be notified of a sensor change (or event).
	 * @param e
	 */
	public void notify(SensorEvent e);
}
