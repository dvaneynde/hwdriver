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
 * Starts and stops a list of gadgets when any of the following holds:
 * <ol>
 * <ul>
 * via ON or TOGGLE event, manual on/off
 * </ul>
 * <ul>
 * via TRIGGER event, when in certain time period [start..end]
 * </ul>
 * <ul>
 * when start time has arrived (and triggier is like automatic)
 * </ul>
 * </ol>
 * <p>
 * Whenever end time has arrived (startTimeMs + durationMS) the gadgets stop -
 * even with manual ON.
 * <p>
 * When within [start..end] period and an OFF event is received the TRIGGER will
 * not work unless the END period has passed.
 * <p>
 * <strong>Important</strong>{@link #isRunning()} == true and state ACTIF do not
 * mean that he gadgets (still) run, the gadgets may have run out. Except if
 * repeat of course.
 * 
 * @author dirk
 *
 */
public class GadgetController extends Controller implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(GadgetController.class);
	private long startTimeMs;
	private long durationMs;
	private boolean activateOnStartTime;
	private boolean repeat;
	private boolean manualStartRequested, manualStopRequested, triggerRecorded = false;
	private long startOfSequenceMs = -1;
	private int idxInSequence = 0;
	private long runtimePastGadgets = 0L;
	private List<GadgetSet> gadgetSets = new ArrayList<>();
	private States state = States.INACTIF;

	/**
	 * INACTIF: outside actif period and not manually started ACTIF: within
	 * actif period or manually started, and not manually stoped WAITING_END:
	 * stopped functioning due to manual stop, but still in actif period
	 * (awaiting its end, only manual start possible)
	 * 
	 * @author dirk
	 * TODO alternatief, extra MANUAL_ACTIF, die los staat van tijden e.d.
	 */
	public enum States {
		INACTIF, WAITING_TRIGGER, ACTIF, WAITING_END
	}

	public GadgetController(String name, long startTimeMs, long durationMs, boolean activateOnStartTme, boolean repeat, IDomoticContext ctx) {
		super(name, name, null, ctx);
		this.startTimeMs = startTimeMs;
		this.durationMs = durationMs;
		this.activateOnStartTime = activateOnStartTme;
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
		case TRIGGERED:
			triggerRecorded = true;
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	@Override
	public void loop(long currentTime, long sequence) {
		switch (state) {
		case INACTIF:
			// goes to ACTIF or WAITING_TRIGGER
			if (manualStartRequested || (activateOnStartTime && withinTimePeriod(currentTime))) {
				state = States.ACTIF;
				startOfSequenceMs = -1;
				manualStartRequested = false;
				logger.info(getName() + " - start running for max. " + durationMs / 1000 + " sec.");
			} else if (!activateOnStartTime && withinTimePeriod(currentTime)) {
				state = States.WAITING_TRIGGER;
				logger.info(getName() + " - from INACTIVE to " + state);
			}
			break;
		case WAITING_TRIGGER:
			// goes to INACTIVE, ACTIVE or WAITING_END
			if (!withinTimePeriod(currentTime)) {
				state = States.INACTIF;
				logger.info(getName() + " - go from ACTIF to " + state + " because of end time reached.");
			} else if (manualStopRequested) {
				state = States.WAITING_END;
				logger.info(getName() + " - go from ACTIF to " + state + " because of manual stop request.");
				manualStopRequested = false;
			} else if (triggerRecorded || manualStartRequested) {
				state = States.ACTIF;
				startOfSequenceMs = -1;
				triggerRecorded = false;
				manualStartRequested = false;
				logger.info(getName() + " - from WAITING_TRIGGER to " + state + " because of " + (triggerRecorded ? "trigger" : "manual start request")
						+ ", start running for max. " + durationMs / 1000 + " sec.");
			}
			break;
		case ACTIF:
			// goes to INACTIVE or WAITING_END
			if (!withinTimePeriod(currentTime)) {
				gadgetSets.get(idxInSequence).onDone();
				state = States.INACTIF;
				logger.info(getName() + " - go from ACTIF to " + state + " because of end time reached.");
			} else if (manualStopRequested) {
				gadgetSets.get(idxInSequence).onDone();
				state = States.WAITING_END;
				logger.info(getName() + " - go from ACTIF to " + state + " because of manual stop request.");
				manualStopRequested = false;
			}
			break;
		case WAITING_END:
			if (!withinTimePeriod(currentTime)) {
				state = States.INACTIF;
				logger.info(getName() + " - go from WAITING_END to " + state + " because of end time reached.");
			} else if (manualStartRequested) {
				state = States.ACTIF;
				startOfSequenceMs = -1;
				manualStartRequested = false;
				logger.info(getName() + " - go from WAITING_END to" + state + ", due to manual start request, for max. " + durationMs / 1000 + " sec.");
			}
			break;
		default:
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
					state = States.WAITING_END;
					logger.info(getName() + " all gadget sets done, go to " + state + " at time " + relativeTimeWithinSequence + "ms.");
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
