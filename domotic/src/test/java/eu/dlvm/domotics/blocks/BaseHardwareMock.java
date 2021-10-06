package eu.dlvm.domotics.blocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.IHardwareIO;
import junit.framework.Assert;

public abstract class BaseHardwareMock implements IHardwareIO {

	static Logger log = LoggerFactory.getLogger(BaseHardwareMock.class);

	@Override
	public void writeDigitalOutput(String channel, boolean value) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean readDigitalInput(String channel) {
		Assert.fail("Must not come here for this test.");
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public int readAnalogInput(String channel) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void writeAnalogOutput(String channel, int value) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void initialize() {
		log.trace("initialize() called, ignored because mock.");
	}

	@Override
	public void refreshInputs() {
		log.trace("refreshInputs() called, ignored because mock.");
	}

	@Override
	public void refreshOutputs() {
		log.trace("refreshOutputs() called, ignored because mock.");
	}

	@Override
	public void stop() {
		log.trace("stop() called, ignored because mock.");
	}

}
