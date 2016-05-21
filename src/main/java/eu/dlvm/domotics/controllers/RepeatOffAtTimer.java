package eu.dlvm.domotics.controllers;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable.ActionType;

public class RepeatOffAtTimer extends Timer {
	private static Logger log = Logger.getLogger(RepeatOffAtTimer.class);
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
				notifyListeners(ActionType.OFF);
				timeLastOffSent = currentTime;
			}
		}
	}
}
