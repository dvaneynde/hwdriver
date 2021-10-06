package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareIO;

/**
 * General context, to pass around stuff easily.
 * TODO use Java injection instead of these?
 *  
 * @author dirk
 * 
 */
public interface IDomoticContext {

	/**
	 * Add Sensor. Not essential, just to make code more robust by registering
	 * itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Sensor can be present no
	 *            more than once.
	 */
	public void addSensor(Sensor s);

	/**
	 * Add Actuator. Not essential, just to make code more robust by registering
	 * itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	public void addActuator(Actuator a);

	/**
	 * Add Controller. Not essential, just to make code more robust by
	 * registering itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	public void addController(Controller a);

	// TODO observable
	public void addStateChangedListener(IStateChangedListener updator);
	// TODO observable
	public void removeStateChangedListener(IStateChangedListener updator);

	/**
	 * TODO should be separate interfaces, read and write, and sensors only have read and actuators only have write
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw();
}
