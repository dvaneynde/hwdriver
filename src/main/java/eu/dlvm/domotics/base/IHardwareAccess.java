package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareIO;

/**
 * Groups functions needed by blocks that need hardware access.
 * 
 * @author dirk vaneynde
 * TODO add loop() here
 */
public interface IHardwareAccess {

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw();

	/**
	 * Add Sensor to loop set (see {@link #loopOnce()}. Not essential, just to
	 * make code more robust by registering itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Sensor can be present no
	 *            more than once.
	 */
	public void addSensor(Sensor s);

	/**
	 * Add Actuator to loop set (see {@link #loopOnce()}. Not essential, just to
	 * make code more robust by registering itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	public void addActuator(Actuator a);

}
