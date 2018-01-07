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
import eu.dlvm.domotics.controllers.gadgets.GadgetState;
import eu.dlvm.domotics.controllers.gadgets.IGadget;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Starts and stops a list of gadgets at given time - or via on-off event.
 * <p>
 * Gadget Sets their active period may overlap, so be careful not to have the
 * same Actuator in multiple sets.
 * 
 * @author dirk
 *
 */
public class GadgetController extends Controller implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(GadgetController.class);
	private long startTimeMs;
	private long endTimeMs;
	private boolean running;
	private long actualStartMs = -1;
	private List<GadgetSet> gadgetSets;

	public GadgetController(String name, long startTimeMs, long endTimeMs, IDomoticContext ctx) {
		super(name, name, null, ctx);
		this.startTimeMs = startTimeMs;
		this.endTimeMs = endTimeMs;
		gadgetSets = new ArrayList<>();
	}

	public void addGadgetSet(GadgetSet e) {
		gadgetSets.add(e);
	}

	public synchronized void start() {
		logger.info(getName() + " started.");
		actualStartMs = -1;
		running = true;
	}

	public synchronized void stop() {
		logger.info(getName() + " stopped.");
		running = false;
	}

	private synchronized boolean needToRun(long currentTime) {
		running = (currentTime >= startTimeMs && currentTime <= endTimeMs);
		return running;
	}

	public void on() {
		onEvent(this, EventType.ON);
	}

	public void off() {
		onEvent(this, EventType.OFF);
	}

	public boolean toggle() {
		onEvent(this, EventType.TOGGLE);
		return running;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			start();
			break;
		case OFF:
			stop();
			break;
		case TOGGLE:
			if (running)
				stop();
			else
				start();
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	public long getStartTimeMs() {
		return startTimeMs;
	}

	public long getEndTimeMs() {
		return endTimeMs;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		if (needToRun(currentTime)) {
			if (actualStartMs == -1)
				actualStartMs = currentTime;
			long relativeTimeController = currentTime - actualStartMs;
			for (GadgetSet gadgetSet : gadgetSets) {
				long relativeTimeGadgetSet = relativeTimeController - gadgetSet.startMs;
				if (!gadgetSet.isActive(gadgetSet, relativeTimeController)) {
					if (gadgetSet.state == GadgetState.BUSY) {
						gadgetSet.state = GadgetState.DONE;
						for (IGadget g : gadgetSet.gadgets)
							g.onDone(relativeTimeGadgetSet);
					}
				} else {
					if (gadgetSet.state == GadgetState.BEFORE) {
						for (IGadget g : gadgetSet.gadgets)
							g.onBefore(relativeTimeGadgetSet);
						gadgetSet.state = GadgetState.BUSY;
					}
					for (IGadget g : gadgetSet.gadgets)
						g.loop2(relativeTimeGadgetSet, gadgetSet.state);
				}
			}
		}
		// TODO if from running to not running anymore need to call onDone on those that are done

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
