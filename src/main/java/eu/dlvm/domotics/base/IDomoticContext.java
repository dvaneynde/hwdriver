package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareIO;

/**
 * Actuator, controller, sensor and IUiCapable blocks moeten zich kunnen
 * registreren. En geeft toegang tot hardware.
 * 
 * TODO getHw() zou in aparte interface moeten, enkel voor sensors en actuators
 * @author dirk
 * 
 */
public interface IDomoticContext {

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

	public void addController(Controller a);

	public void addStateChangedListener(IStateChangedListener updator);
	public void removeStateChangedListener(IStateChangedListener updator);
	
	/**
	 * @return Underlying hardware.
	 * TODO waarom?
	 */
	public IHardwareIO getHw();
}
