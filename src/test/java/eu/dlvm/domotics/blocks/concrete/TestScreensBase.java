package eu.dlvm.domotics.blocks.concrete;

import org.junit.Before;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.iohardware.IHardwareIO;

public class TestScreensBase {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
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

	protected static int DN = 0;
	protected static int UP = 1;
	protected Screen sr;
	protected Hardware hw;
	protected IDomoticContext ctx;
	protected long seq;
	protected long cur;

	public TestScreensBase() {
		super();
	}

	protected void loop(long inc) {
		cur += inc;
		sr.loop(cur, seq++);
	}

	protected void loop() {
		loop(10);
	}

	@Before
	public void init() {
		hw = new Hardware();
		ctx = new DomoContextMock(hw);
		sr = new Screen("TestScreens", "TestScreens", null, Integer.toString(DN), Integer.toString(UP), ctx);
		sr.setMotorUpPeriod(30);
		sr.setMotorDnPeriod(30);
		seq = cur = 0L;
	}

}
