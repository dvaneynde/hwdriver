package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

public class TestOpenWeatherMap {

	@Ignore
	@Test
	public void testOpenWeatherMap() {
		OpenWeatherMap owm = new OpenWeatherMap();
		OpenWeatherMap.Info info = owm.getWeatherReport();
		long day = 24*60*60;
		long notBefore = System.currentTimeMillis()/1000L-day;
		long notAfter = notBefore+2*day;
		assertNotNull(info);
		assertNotNull(info.sunrise_sec);
		assertNotNull(info.sunset_sec);
		assertTrue(info.sunrise_sec<info.sunset_sec);
		assertTrue(info.sunrise_sec>notBefore && info.sunrise_sec<notAfter);
		assertTrue(info.sunset_sec>notBefore && info.sunset_sec<notAfter);
	}
}
