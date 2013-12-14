package eu.dlvm.domotics.blocks;

import static org.junit.Assert.fail;

import org.junit.Test;

import eu.dlvm.domotica.service.BlockInfo;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.iohardware.LogCh;

public class TestLoopSequence {

	class MyActuator extends Actuator {

		public MyActuator(String name, String description, LogCh channel, IHardwareAccess ctx) {
			super(name, description, channel, ctx);
		}

		@Override
		public void loop(long currentTime, long sequence) {
		}

		@Override
		public void initializeOutput(RememberedOutput ro) {
		}

		@Override
		public BlockInfo getActuatorInfo() {
			return null;
		}

		public void check(long i) {
			checkLoopSequence(i);
		}
	}

	@Test
	public void testCheckLoopSequence() {
		MyActuator a = new MyActuator(null, null, null, new DomoContextMock(null));
		a.check(0);
		a.check(1);
		try {
			a.check(1);
			fail("Should have had exception !");
		} catch (RuntimeException e) {
			;
		}
	}

}
