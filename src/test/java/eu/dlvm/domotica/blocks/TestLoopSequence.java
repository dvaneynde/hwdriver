package eu.dlvm.domotica.blocks;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.dlvm.domotica.service.BlockInfo;

public class TestLoopSequence {

	@Test
	public void testCheckLoopSequence() {
		Actuator a = new Actuator(null, null, null, new DomoContextMock(null)) {

			@Override
			public void loop(long currentTime, long sequence) {
			}

			@Override
			public void initializeOutput() {
			}

			@Override
			public void execute(String op) {
			}

			@Override
			public BlockInfo getActuatorInfo() {
				return null;
			}
		};
		a.checkLoopSequence(0);
		a.checkLoopSequence(1);
		try {
			a.checkLoopSequence(1);
			fail("Should have had exception !");
		} catch (RuntimeException e) {
			;
		}
	}

}
