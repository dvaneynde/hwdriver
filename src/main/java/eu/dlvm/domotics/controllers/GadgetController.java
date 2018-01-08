package eu.dlvm.domotics.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Starts and stops a list of gadgets at given time - or via on-off event.
 * 
 * @author dirk
 *
 */
public class GadgetController extends Controller implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(GadgetController.class);
	private long startTimeMs;
	private long durationMs;
	private boolean repeat;
	private boolean manualStartRequested, manualStopRequested = false;
	private long startOfSequenceMs = -1;
	private int idxInSequence = 0;
	private long runtimePastGadgets = 0L;
	private List<GadgetSet> gadgetSets = new ArrayList<>();
	private States state = States.INACTIF;

	/**
	 * INACTIF: outside actif period and not manually started ACTIF: within
	 * actif period or manually started, and not manually stoped MANUALSTOP:
	 * stopped functioning due to manual stop, but still in actif period
	 * (awaiting its end, only manual start possible)
	 * 
	 * @author dirk
	 *
	 */
	public enum States {
		INACTIF, ACTIF, MANUALSTOP
	}

	public GadgetController(String name, long startTimeMs, long durationMs, boolean repeat, IDomoticContext ctx) {
		super(name, name, null, ctx);
		this.startTimeMs = startTimeMs;
		this.durationMs = durationMs;
		this.repeat = repeat;
	}

	public void addGadgetSet(GadgetSet e) {
		gadgetSets.add(e);
	}

	public synchronized void requestManualStart() {
		logger.info(getName() + " manual start requested.");
		manualStartRequested = true;
	}

	public synchronized void requestManualStop() {
		logger.info(getName() + " manual stop requested.");
		manualStopRequested = true;
	}

	private boolean withinTimePeriod(long currentTime) {
		return (currentTime >= startTimeMs && currentTime <= (startTimeMs + durationMs));
	}

	public void on() {
		onEvent(this, EventType.ON);
	}

	public void off() {
		onEvent(this, EventType.OFF);
	}

	public boolean toggle() {
		onEvent(this, EventType.TOGGLE);
		return (manualStartRequested == true); // isRunning() cannot work, is async
	}

	public States getState() {
		return state;
	}
	
	public boolean isRunning() {
		return (state == States.ACTIF);
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			requestManualStart();
			break;
		case OFF:
			requestManualStop();
			break;
		case TOGGLE:
			if (isRunning())
				requestManualStop();
			else
				requestManualStart();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	@Override
	public void loop(long currentTime, long sequence) {
		switch (state) {
		case ACTIF:
			if (manualStopRequested || !withinTimePeriod(currentTime)) {
				gadgetSets.get(idxInSequence).onDone();
				state = (manualStopRequested ? States.MANUALSTOP : States.INACTIF);
				logger.info(getName() + " - go from ACTIF to " + state + ".");
				manualStopRequested = false;
			}
			break;
		case INACTIF:
			if (manualStartRequested || withinTimePeriod(currentTime)) {
				state = States.ACTIF;
				startOfSequenceMs = -1;
				manualStartRequested = false;
				logger.info(getName() + " - start running for max. " + durationMs / 1000 + " sec.");
			}
			break;
		case MANUALSTOP:
			if (!withinTimePeriod(currentTime)) {
				gadgetSets.get(idxInSequence).onDone();
				state = States.INACTIF;
				logger.info(getName() + " - active period passed, go to INACTIF.");
			}
			break;

		}

		if (state == States.ACTIF) {
			if (startOfSequenceMs == -1) {
				startOfSequenceMs = currentTime;
				idxInSequence = 0;
				runtimePastGadgets = 0;
				logger.info(getName() + " starting set " + idxInSequence + " / " + (gadgetSets.size() - 1) + " at time 0ms.");
				gadgetSets.get(0).onBefore();
			}
			long relativeTimeWithinSequence = currentTime - startOfSequenceMs;
			GadgetSet gadgetSet = gadgetSets.get(idxInSequence);

			if (relativeTimeWithinSequence > runtimePastGadgets + gadgetSet.durationMs) {
				gadgetSet.onDone(); // Could be that this one executes without gadget ever have run onBusy().
				idxInSequence++;
				if (idxInSequence >= gadgetSets.size()) {
					gadgetSets.get(idxInSequence - 1).onDone();
					state = States.INACTIF;
					logger.info(getName() + " all gadget sets done, go INACTIF at time " + relativeTimeWithinSequence + "ms.");
					// TODO looping case
				} else {
					runtimePastGadgets += gadgetSet.durationMs;
					gadgetSet = gadgetSets.get(idxInSequence);
					gadgetSet.onBefore();
					logger.info(getName() + " previous gadget set done, now starting set " + idxInSequence + " / " + (gadgetSets.size() - 1) + " at time "
							+ relativeTimeWithinSequence + "ms.");
				}
			}

			if (state == States.ACTIF) {
				gadgetSet.onBusy(relativeTimeWithinSequence - runtimePastGadgets);
			}
		}
	}

	@Override
	public UiInfo getUiInfo() {
		return null;
	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on GadgetController '" + getName() + "' got unsupported action '" + action + ".");
	}

}
