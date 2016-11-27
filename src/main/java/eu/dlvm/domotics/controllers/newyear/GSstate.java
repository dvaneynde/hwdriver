package eu.dlvm.domotics.controllers.newyear;

/**
 * Gadget Sets are executed in sequence, and each Gadget Set goes through these
 * states.
 * <p>
 * Note: Currently not really used.
 * 
 * @author dirk
 */
public enum GSstate {
	BEFORE, FIRST, BUSY, LAST, DONE;
}
