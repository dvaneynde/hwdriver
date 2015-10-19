package eu.dlvm.domotics.blocks.concrete;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.FrequencyGauge;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.sensors.WindSensor;
import eu.dlvm.domotics.sensors.IThresholdListener.EventType;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestWindSensor implements IThresholdListener {

	public class HardwareWindSensor extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;

		@Override 
		public void writeDigitalOutput(LogCh channel, boolean value) throws IllegalArgumentException {
			inval = value;
		}
		
		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inval;
		}
	};

	private HardwareWindSensor hw;
	private IDomoticContext dom;
	private WindSensor ws;
	private long seq, cur;

	public static final LogCh WINDSENSOR_CH = new LogCh(10);
	public static int HIGH_SPEED_THRESHOLD = 20;
	public static int LOW_SPEED_THRESHOLD = 5;
	public static int HIGH_TIME_BEFORE_ALERT = 30 * 1000;
	public static int LOW_TIME_TO_RESET_ALERT = 180 * 1000;

	@Override
	public void onEvent(Sensor source, EventType event) {
		lastEvent = event;
	}

	private IThresholdListener.EventType lastEvent;

	@BeforeClass
	public static void initLog() {
		BasicConfigurator.configure();
	}

	@Before
	public void init() {
		hw = new HardwareWindSensor();
		dom = new DomoContextMock(hw);
	}

	private void loop(long time) {
		cur += time;
		seq++;
		ws.loop(cur, seq);
	}

	public void simulateFrequency(int freq, double duration) {
		/*
		 * freq = 1 / T sample tijd is 20 ms; dus max freq. is 50Hz / 2 = 25Hz
		 */
		boolean input = false;
		hw.writeDigitalOutput(WINDSENSOR_CH, input);
		double delta = 1 / (double) freq * 1000;
		double curTijd = 0.0;
		// TODO input moet aan-uit maar volgens die frequentie
		for (int i = 0; i < (duration / 20); i++) {
			loop(20);
			curTijd += 20;
			if (curTijd > delta) {
				input = !input;
				hw.writeDigitalOutput(WINDSENSOR_CH, input);
				curTijd -= delta;
			}
		}
	}

	@Test
	public final void simpleTest() {
		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, dom, HIGH_SPEED_THRESHOLD, LOW_SPEED_THRESHOLD, HIGH_TIME_BEFORE_ALERT, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		seq = cur = 0L;
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		simulateFrequency(HIGH_SPEED_THRESHOLD + 5, HIGH_TIME_BEFORE_ALERT + 100);
		Assert.assertEquals(WindSensor.States.TOO_HIGH, ws.getState());

		Assert.fail("En de rest moet ik nog doen...");
	}
}
