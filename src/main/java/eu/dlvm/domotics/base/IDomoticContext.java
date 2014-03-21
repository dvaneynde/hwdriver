package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareIO;

// TODO weggooien, en enkel IHardwareIO gebruiken, en na creatie expliciet toevoegen aan domotic ?
// TODO alhoewel, actuator, controller, sensor en zelfs mappers die IUiCapable zijn moeten zich kunnen registreren; werkt een polymorfe methode, b.v. addBlock(Block) die dan overridden is? denk het niet... 
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

	public void addUiCapableBlock(IUserInterfaceAPI uiblock0);

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw();
}
