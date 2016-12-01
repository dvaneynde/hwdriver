package eu.dlvm.domotics.controllers;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.events.EventType;

/**
 * TODO must be incorporated in lamps instead. With blinking 2 times, and if toggled remains on, otherwises goes out.
 * 
 * @author dirk
 *
 */
public class RepeatOffAtTimer extends Timer {
	private static Logger log = LoggerFactory.getLogger(RepeatOffAtTimer.class);
	private int intervalSec;
	private long timeLastOffSent;

	// public user api
	public RepeatOffAtTimer(String name, String description, IDomoticContext ctx, int intervalSec) {
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
	public void loop(long currentTime, long sequence) {
		long currentTimeInDay = timeInDay(currentTime);
		// boolean state2 = state;
		if (onTime <= offTime) {
			state = (currentTimeInDay > onTime && currentTimeInDay < offTime);
		} else {
			state = !(currentTimeInDay > offTime && currentTimeInDay < onTime);
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
