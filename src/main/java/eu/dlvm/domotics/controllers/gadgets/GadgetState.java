package eu.dlvm.domotics.controllers.gadgets;

/**
 * Gadget Sets are executed in sequence, and each Gadget Set goes through these
 * states.
 * 
 * @author dirk
 */
public enum GadgetState {
	BEFORE, BUSY, DONE;
}
