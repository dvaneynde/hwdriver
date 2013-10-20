package eu.dlvm.domotica.blocks;

import eu.dlvm.iohardware.IHardwareIO;

/**
 * @author dirk vaneynde
 *
 */
public interface IDomoContext {

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw();
	
	/**
	 * Add Sensor to domotic system.
	 * 
	 * @param s
	 *            Added, if not already present. Each Sensor can be present no
	 *            more than once.
	 */
	public void addSensor(Sensor s);

	/**
	 * Add Actuator to domotic system.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	public void addActuator(Actuator a);

	/**
	 * @return Number incremented with each  {@link Block#loopOnce()}.
	 */
	public long getLoopSequence();

}
