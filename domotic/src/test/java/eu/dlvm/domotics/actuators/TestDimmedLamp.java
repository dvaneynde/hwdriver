package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

// TODO werkt met 50ms sample, maar niet meer met 20ms sample; daarom loop() zoals bij WindSensor test.
public class TestDimmedLamp {

	public static int MAX_OUT = 32;

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public int level;

		@Override
		public void writeAnalogOutput(String channel, int value) throws IllegalArgumentException {
			Assert.assertTrue(channel.equals(LAMP_CH));
			level = value;
		}
	};

	private static final String LAMP_CH = Integer.toString(10);
	private DimmedLamp lamp;
	private Hardware hw = new Hardware();
	private IDomoticBuilder builder = new DomoContextMock(hw);
	private long seq;

	@Before
	public void init() {
		lamp = new DimmedLamp("TestDimmedLamp", "TestDimmedLamp", MAX_OUT, LAMP_CH, hw, builder);
		lamp.setMsTimeFullDim(3000);
	}

	@Test
	public void testToggleAndOnAndOff() {
		int cur = 0;
		lamp.loop(cur, seq++);
		assertOff(100);

		lamp.on(50);
		lamp.loop(cur += 100, seq++);
		assertOn(50);

		lamp.toggle();
		lamp.loop(cur += 100, seq++);
		assertOff(50);

		lamp.toggle();
		lamp.loop(cur += 100, seq++);
		assertOn(50);

		lamp.off();
		lamp.loop(cur += 100, seq++);
		assertOff(50);

		lamp.on();
		lamp.loop(cur += 100, seq++);
		assertOn(50);
	}

	@Test
	public void dimUpAndDown() {
		int cur = 0;
		lamp.loop(cur += 1, seq++);
		assertOff(100);

		lamp.up(true);
		lamp.loop(cur += 1000, seq++);
		assertUp(0);
		lamp.loop(cur += 1000, seq++);
		assertUp(100 / 3);
		lamp.loop(cur += 1000, seq++);
		assertUp(200 / 3);
		// Next one is 1023 instead of 1000, because each time 33, 3 times is only 99...
		lamp.loop(cur += 1023, seq++);
		assertUp(100);
		lamp.loop(cur += 1000, seq++);
		assertUp(100);
		lamp.up(false);
		lamp.loop(cur += 1, seq++);
		assertOn(100);

		lamp.down(true);
		lamp.loop(cur += 1, seq++);
		assertDown();
		for (int i = 0; i < 3; i++) {
			lamp.loop(cur += 1000, seq++);
			assertDown();
		}
		lamp.down(false);
		lamp.loop(cur += 1, seq++);
		assertOn(0);
	}

	private void assertOff(int lvl) {
		Assert.assertEquals(DimmedLamp.States.OFF, lamp.getState());
		Assert.assertEquals(lvl, lamp.getLevel());
		Assert.assertEquals(0, hw.level);
	}

	private void assertUp(int lvl) {
		Assert.assertEquals(DimmedLamp.States.UP, lamp.getState());
		Assert.assertEquals(lvl, lamp.getLevel());
		Assert.assertEquals(MAX_OUT * lvl / 100, hw.level);
	}

	private void assertDown() {
		Assert.assertEquals(DimmedLamp.States.DOWN, lamp.getState());
	}

	private void assertOn(int expectedLevelAsPercentage) {
		Assert.assertEquals(DimmedLamp.States.ON, lamp.getState());
		Assert.assertEquals(expectedLevelAsPercentage, lamp.getLevel());
		Assert.assertEquals(expectedLevelAsPercentage * MAX_OUT / 100, hw.level);
	}
}
