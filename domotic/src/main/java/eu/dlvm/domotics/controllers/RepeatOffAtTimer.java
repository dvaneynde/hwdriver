package eu.dlvm.domotics.controllers;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.events.EventType;

/**
 * Sends {@link EventType#OFF} every {@link #intervalSec} seconds.
 * 
 * @author dirk
 *
 */
public class RepeatOffAtTimer extends Timer {
	private static Logger log = LoggerFactory.getLogger(RepeatOffAtTimer.class);
	private int intervalSec;
	private long timeLastOffSent;

	// public user api
	public RepeatOffAtTimer(String name, String description, IDomoticBuilder ctx, int intervalSec) {
		super(name, description, ctx);
		timeLastOffSent = 0;
		setIntervalSec(intervalSec);
	}

	public int getIntervalSec() {
		return intervalSec;
	}

	public void setIntervalSec(int intervalSec) {
		this.intervalSec = intervalSec;
	}

	// internal
	@Override
	public void loop(long currentTime) {
		long currentTimeInDay = timeInDayMillis(currentTime);
		// boolean state2 = state;
		if (onTimeMs <= offTimeMs) {
			state = (currentTimeInDay > onTimeMs && currentTimeInDay < offTimeMs);
		} else {
			state = !(currentTimeInDay > offTimeMs && currentTimeInDay < onTimeMs);
		}
		if (state) {
			if (currentTime - timeLastOffSent >= intervalSec * 1000) {
				log.info("RepeatOffAtTimer '" + getName() + "' sends OFF to listeners.");
				notifyListeners(EventType.OFF);
				timeLastOffSent = currentTime;
			}
		}
	}
}
