package eu.dlvm.domotics.controllers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.iohardware.IHardwareIO;

public class TestSunWindControllerWithRealScreen {

	public class HardwareMock extends BaseHardwareMock implements IHardwareIO {
		public boolean dnRelais;
		public boolean upRelais;

		@Override
		public void writeDigitalOutput(String ch, boolean value) throws IllegalArgumentException {
			if (ch.equals("0"))
				dnRelais = value;
			else
				upRelais = value;
		}
	};

	public long middayLong = 1562500800000L;
	protected static int DN = 0;
	protected static int UP = 1;
	protected Screen sr;
	SunWindController swc;
	protected HardwareMock hw;
	protected DomoticMock dom;

	@Before
	public void init() {
		hw = new HardwareMock();
		dom = new DomoticMock();

		sr = new Screen("TestScreens", "TestScreens", null, Integer.toString(DN), Integer.toString(UP), hw, dom);
		sr.setMotorUpPeriod(30);
		sr.setMotorDnPeriod(30);

		swc = new SunWindController("Test", "Test SunWindCtonroller", "Dummy UI", dom);
		swc.registerListener(sr);
	}

	public void loop(long currentTime) {
		swc.loop(currentTime);
		sr.loop(currentTime);
	}

	// TODO all tests from TestSunWindController !
	
	@Test
	public void testScreensDownAfterWindSafeAndSunStillHigh() {

		long time = middayLong - 10L;

		loop(time += 10L);
		Assert.assertFalse(swc.isEnabled());
		Assert.assertEquals(Screen.States.REST, sr.getState());

		swc.on();
		loop(middayLong += 10L);
		Assert.assertTrue(swc.isEnabled());
		Assert.assertEquals(Screen.States.UP, sr.getState());
		loop(middayLong += (sr.getMotorUpPeriodMs() + 10L));
		Assert.assertEquals(Screen.States.REST, sr.getState());

		swc.onEvent(null, EventType.LIGHT_HIGH);
		loop(middayLong += 10L);
		Assert.assertEquals(Screen.States.DOWN, sr.getState());
		loop(middayLong += (sr.getMotorDnPeriodMs() + 10L));
		Assert.assertEquals(Screen.States.REST, sr.getState());

		swc.onEvent(null, EventType.ALARM);
		loop(middayLong += 10L);
		Assert.assertEquals(Screen.States.UP, sr.getState());
		loop(middayLong += (sr.getMotorUpPeriodMs() + 10L));
		Assert.assertEquals(Screen.States.REST_PROTECT, sr.getState());

		swc.onEvent(null, EventType.SAFE);
		loop(middayLong += 10L);
		Assert.assertEquals(Screen.States.REST, sr.getState());
		loop(middayLong += 10L);
		Assert.assertEquals(Screen.States.DOWN, sr.getState());
		}
}
