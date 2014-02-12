package eu.dlvm.domotics.controllers;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable;
import eu.dlvm.domotics.mappers.IOnOffToggleCapable.ActionType;
import eu.dlvm.domotics.service.BlockInfo;
import eu.dlvm.iohardware.LogCh;

/**
 * TODO moet Controller worden
 * @author dirkv
 *
 */
public class Timer extends Controller {

	static Logger log = Logger.getLogger(Timer.class);

	private int onTime, offTime;
	private boolean state;

	private Set<IOnOffToggleCapable> listeners = new HashSet<>();

	public Timer(String name, String description, LogCh channel, IDomoticContext ctx) {
		super(name, description, null, channel, ctx);
		state = false;
		onTime = offTime = 0;
	}

	public void register(IOnOffToggleCapable listener) {
		listeners.add(listener);
	}

	public void notifyListeners(IOnOffToggleCapable.ActionType action) {
		for (IOnOffToggleCapable l : listeners)
			l.onEvent(action);
	}

	public static int timeInDay(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		int timeInDay = timeInDayMillis(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		return timeInDay;
	}

	public static int timeInDayMillis(int hour, int minute) {
		return ((hour * 60) + minute) * 60000;
	}

	public static int[] hourMinute(long time) {
		time /= 60000;
		int minute = (int) time % 60;
		int hour = (int) time / 60;
		return new int[] { hour, minute };
	}

	public void setOnTime(int hour, int minute) {
		onTime = timeInDayMillis(hour, minute);
	}

	public void setOffTime(int hour, int minute) {
		offTime = timeInDayMillis(hour, minute);
	}

	public String getOnTimeAsString() {
		int[] times = hourMinute(onTime);
		return String.format("%02d:%02d", times[0], times[1]);
	}

	public String getOffTimeAsString() {
		int[] times = hourMinute(offTime);
		return String.format("%02d:%02d", times[0], times[1]);
	}

	public boolean isOn() {
		return state;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		long currentTimeInDay = timeInDay(currentTime);
		boolean state2 = state;
		if (onTime <= offTime) {
			state2 = (currentTimeInDay > onTime && currentTimeInDay < offTime);
		} else {
			state2 = !(currentTimeInDay > offTime && currentTimeInDay < onTime);
		}
		if (state2 != state) {
			state = state2;
			log.info("Timer '" + getName() + "' sends event '" + (state ? "ON" : "OFF") + "'");
			notifyListeners(state ? ActionType.ON : ActionType.OFF);
		}
	}

	@Override
	public BlockInfo getBlockInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(String action) {
		// TODO Auto-generated method stub
		
	}
}
