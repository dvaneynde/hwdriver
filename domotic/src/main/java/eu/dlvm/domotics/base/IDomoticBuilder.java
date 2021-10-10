package eu.dlvm.domotics.base;

/**
 * Methods to build a domotic configuration.
 *
 * TODO use Java injection instead of these?
 *  
 * @author dirk
 * 
 */
public interface IDomoticBuilder {

	/**
	 * Add Sensor. Not essential, just to make code more robust by registering
	 * itself in constructor.
	 * 
	 * @param s
	 *            Added, if not already present. Each Sensor can be present no
	 *            more than once.
	 */
	void addSensor(Sensor s);

	/**
	 * Add Actuator. Not essential, just to make code more robust by registering
	 * itself in constructor.
	 * 
	 * @param a
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	void addActuator(Actuator a);

	/**
	 * Add Controller. Not essential, just to make code more robust by
	 * registering itself in constructor.
	 * 
	 * @param a
	 *            Added, if not already present. Each Actuator can be present no
	 *            more than once.
	 */
	void addController(Controller a);
}
