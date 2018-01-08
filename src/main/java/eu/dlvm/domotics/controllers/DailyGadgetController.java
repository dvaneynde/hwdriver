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
import eu.dlvm.domotics.controllers.gadgets.IGadget;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Starts and stops a list of gadgets every day, based on time and/or light
 * input. Specifically, gadgets run if and only if:
 * <ol>
 * <li>time of day is between [earliestStartTimeMs, latestEndTimdMs</li>
 * <li>a LIGHT_LOW event was received and no LIGHT_HIGH event was received</li>
 * </ol>
 * <p>
 * Gadget Sets their active period may overlap, so be careful not to have the
 * same Actuator in multiple sets.
 * 
 * @author dirk
 *
 */
public class DailyGadgetController /*extends Controller implements IEventListener, IUiCapableBlock*/ {

	/*
	private static Logger logger = LoggerFactory.getLogger(DailyGadgetController.class);
	
	private int earliestStartTime;
	private int latestEndTime;
	private States state = States.DISABLED;
	private States lastState = state;
	private long actualStartMs = -1;
	private List<GadgetSet> gadgetSets = new ArrayList<>();
	*/
	/**
	 * DISABLED -> IN_PERIOD_BUT_NO_LOW -> RUNNING -> DISABLED DISABLED ->
	 * IN_PERIOD_BUT_NO_LOW -> RUNNING -> IN_PERIOD_BUT_HIGH_AFTER_LOW ->
	 * DISABLED manual only: DISABLED <-> RUNNING
	 */
	public static enum States {
		DISABLED, IN_PERIOD_BUT_NO_LOW, RUNNING, IN_PERIOD_BUT_HIGH_AFTER_LOW
	};
	/*
		// TODO both earliest and latest 0 must equal 'always'
		public DailyGadgetController(String name, int earliestStartTime, int latestEndTime, IDomoticContext ctx) {
			super(name, name, null, ctx);
			this.earliestStartTime = earliestStartTime;
			this.latestEndTime = latestEndTime;
		}
	
		public void addGadgetSet(GadgetSet e) {
			gadgetSets.add(e);
		}
	
		public synchronized void start() {
			state = States.RUNNING;
		}
	
		public synchronized void stop() {
			state = States.DISABLED;
		}
	
		private boolean isEnabledPeriod(long currentTime) {
			int currentTimeInDay = Timer.timeInDay(currentTime);
			boolean enabled;
			if (earliestStartTime <= latestEndTime) {
				enabled = (currentTimeInDay > earliestStartTime && currentTimeInDay < latestEndTime);
			} else {
				enabled = !(currentTimeInDay > latestEndTime && currentTimeInDay < earliestStartTime);
			}
			return enabled;
		}
	
		private boolean isRunning() {
			return (state == States.RUNNING);
		}
	
		public void on() {
			onEvent(this, EventType.ON);
		}
	
		public void off() {
			onEvent(this, EventType.OFF);
		}
	
		public boolean toggle() {
			onEvent(this, EventType.TOGGLE);
			return isRunning();
		}
	
		@Override
		public synchronized void onEvent(Block source, EventType event) {
			switch (event) {
			case ON:
				start();
				break;
			case OFF:
				stop();
				break;
			case TOGGLE:
				if (isRunning())
					stop();
				else
					start();
				break;
			case LIGHT_HIGH:
				if (state == States.RUNNING) {
					state = States.IN_PERIOD_BUT_HIGH_AFTER_LOW;
				}
			case LIGHT_LOW:
				if (state == States.IN_PERIOD_BUT_NO_LOW) {
					state = States.RUNNING;
				}
			default:
				logger.warn("Ignored event " + event + " from " + source.getName());
			}
		}
	
		public long getEarliestStartTime() {
			return earliestStartTime;
		}
	
		public long getLatestEndTime() {
			return latestEndTime;
		}
	
		private void finalizeAnyBysyGadgetSets(long currentTime) {
			long relativeTimeController = currentTime - actualStartMs;
			for (GadgetSet gadgetSet : gadgetSets) {
				long relativeTimeGadgetSet = relativeTimeController - gadgetSet.startMs;
				if (gadgetSet.state == GadgetState.BUSY) {
					gadgetSet.state = GadgetState.DONE;
					for (IGadget g : gadgetSet.gadgets)
						g.onDone(relativeTimeGadgetSet);
				}
			}
		}
	
		@Override
		public synchronized void loop(long currentTime, long sequence) {
			boolean periodEnabled = isEnabledPeriod(currentTime);
			switch (state) {
			case DISABLED:
				if (lastState == States.RUNNING) {
					finalizeAnyBysyGadgetSets(currentTime);
					actualStartMs = -1;
				}
				if (periodEnabled) {
					state = States.IN_PERIOD_BUT_NO_LOW;
					logger.info(getName() + " - within enabled time period now, awaiting LIGHT_LOW event.");
				}
				break;
			case IN_PERIOD_BUT_HIGH_AFTER_LOW:
				if (lastState == States.RUNNING) {
					finalizeAnyBysyGadgetSets(currentTime);
					actualStartMs = -1;
				}
				break;
			case IN_PERIOD_BUT_NO_LOW:
				break;
			case RUNNING:
				if (actualStartMs == -1)
					actualStartMs = currentTime;
				for (GadgetSet gadgetSet : gadgetSets) {
					long relativeTimeController = currentTime - actualStartMs;
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
							g.onBusy(relativeTimeGadgetSet, gadgetSet.state);
					}
				}
				break;
			default:
				break;
			}
			lastState = state;
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
	*/
}
