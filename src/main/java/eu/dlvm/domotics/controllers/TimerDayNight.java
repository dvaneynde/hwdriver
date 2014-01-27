package eu.dlvm.domotics.controllers;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.utils.OpenWeatherMap;
import eu.dlvm.domotics.utils.OpenWeatherMap.Info;
import eu.dlvm.iohardware.LogCh;

/**
 * Checks via {@link OpenWeatherMap} the sunrise and sunset times, and uses
 * these for setting lamps off and on respectively. <br/>
 * 30 minutes is subtracted or added to account for shimmer.
 * 
 * @author dirkv
 */
public class TimerDayNight extends Timer {

	static Logger LOG = Logger.getLogger(TimerDayNight.class);

	public static long TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS = 5 * 60 * 1000;

	private boolean timesUpdatedForToday;
	private Calendar today;

	private long lastContactTimeProviderMs;
	private OpenWeatherMap openWeatherMap;
	private Future<Info> asyncCheckWeather;

	public TimerDayNight(String name, String description, LogCh channel, IDomoticContext ctx) {
		super(name, description, channel, ctx);
		setOpenWeatherMap(new OpenWeatherMap());
	}

	void checktTimesUpdatedForToday(long currentTime) {
		if (timesUpdatedForToday || (today == null)) {
			// check if still today: if not, false; above test on today is to force to initialize today
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(currentTime);
			if (today == null || (now.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH))) {
				today = now;
				timesUpdatedForToday = false;
			}
		}
	}

	/** Testing only */
	boolean isTimesUpdatedForToday() {
		return timesUpdatedForToday;
	}
	/** Testing only */
	void setTimesUpdatedForToday(boolean value) {
		timesUpdatedForToday = value;
	}

	@Override
	public void loop(long currentTime, long sequence) {
		checktTimesUpdatedForToday(currentTime);
		if (!timesUpdatedForToday) {
			if (asyncCheckWeather != null) {
				// Already checking, check if there is a result
				if (asyncCheckWeather.isDone()) {
					try {
						Info info = asyncCheckWeather.get();
						asyncCheckWeather = null;
						if (info != null) {
							setOnOffTimes(info);
							log.info("Checked todays' sunrise (" + getOffTimeAsString() + ") and sunset (" + getOnTimeAsString()
									+ ") times. Note: these include 30 minutes shimmer time. I'll check again tomorrow.");
							timesUpdatedForToday = true;
						} else {
							log.warn("Did not get times from internet provider. Will try again in " + TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS / 1000 / 60 + " minutes.");
						}
					} catch (InterruptedException | ExecutionException e) {
						log.warn("Getting weather report failed.", e);
					}
				} else {
					log.debug("loop() asyncCheckWeather task not finished yet...");
				}
			} else if (lastContactTimeProviderMs + TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS <= currentTime) {
				// Not checking, start one if grace period expired
				lastContactTimeProviderMs = currentTime;
				Callable<Info> worker = new TimerDayNight.WheatherInfoCallable();
				asyncCheckWeather = Executors.newSingleThreadExecutor().submit(worker);
			}
		}
		super.loop(currentTime, sequence);
	}

	private void setOnOffTimes(OpenWeatherMap.Info info) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis((info.sunset_sec + 30 * 60) * 1000L);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		setOnTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		c.setTimeInMillis((info.sunrise_sec - 30 * 60) * 1000L);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		setOffTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

	public OpenWeatherMap getOpenWeatherMap() {
		return openWeatherMap;
	}

	public void setOpenWeatherMap(OpenWeatherMap openWeatherMap) {
		this.openWeatherMap = openWeatherMap;
	}

	public class WheatherInfoCallable implements Callable<Info> {
		@Override
		public Info call() throws Exception {
			Info info = openWeatherMap.getWeatherReport();
			log.debug("WheatherInfoCallable.call() info="+info);
			return info;
		}
	}

	public static long getTimeMsSameDayAtHourMinute(long basetime, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(basetime);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

}
