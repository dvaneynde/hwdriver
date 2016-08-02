package eu.dlvm.domotics.base;

import eu.dlvm.domotics.service_impl.IUIUpdator;
import eu.dlvm.iohardware.IHardwareIO;

/**
 * Actuator, controller, sensor en IUiCapable blocks moeten zich kunnen
 * registreren. En geeft toegang tot hardware.
 * 
 * TODO getHw() zou in aparte interface moeten, enkel voor sensors en actuators
 * TODO kan dit niet dynamisch, b.v. addBlock en dan kijkt code zelf of het sensor is, of ui capable is? en hw al dan niet zetten? Factory maken?
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
	// TODO addBlock
	public void addSensor(Sensor s);

	/**
	 * Add Actuator to loop set (see {@link #loopOnce()}. Not essential, just to
	 * make code more robust by registering itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	// TODO addBlock
	public void addActuator(Actuator a);

	// TODO addBlock
	public void addController(Controller a);

	// TODO onderstaande wordt rechtstreeks aangeroepen vanuit Handler, naast intern in Domotic...
	//public void addUiCapableBlock(IUiCapableBlock uiblock0);

	public void addUiUpdator(IUIUpdator updator);
	public void removeUiUpdator(IUIUpdator updator);
	
	/**
	 * @return Underlying hardware.
	 * TODO waarom?
	 */
	public IHardwareIO getHw();
}
