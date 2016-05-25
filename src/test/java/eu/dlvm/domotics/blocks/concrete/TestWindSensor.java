package eu.dlvm.domotics.blocks.concrete;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.FrequencyGauge;
import eu.dlvm.domotics.sensors.IThresholdListener;
import eu.dlvm.domotics.sensors.WindSensor;
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

	private static final Logger log = Logger.getLogger(TestWindSensor.class);
	private HardwareWindSensor hw;
	private IDomoticContext dom;
	private WindSensor ws;
	private long seq, cur;

	public static final LogCh WINDSENSOR_CH = new LogCh(10);
	public static int HIGH_FREQ_THRESHOLD = 5;
	public static int LOW_FREQ_THRESHOLD = 1;
	public static int HIGH_TIME_BEFORE_ALERT = 1 * 1000; // was 30e3
	public static int LOW_TIME_TO_RESET_ALERT = 2 * 1000; // was 180e3

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

	// private void loop(long time) {
	// cur += time;
	// seq++;
	// ws.loop(cur, seq);
	// }

	private void loop(long time) {
		seq++;
		ws.loop(time, seq);
	}

	int samplePeriodMs = 20;

	private long simulateWind(double freq, double durationMs, long time) {
		double transitionPeriodMs = 1000 / (2.0 * freq);
		long startTime = time;
		
		int nrTransitions = 1;
		double nextTransitionTime = startTime + nrTransitions * transitionPeriodMs;
		boolean val = false;

		while (time <= startTime + durationMs) {
			if (time >= nextTransitionTime) {
				val = !val;
				nrTransitions++;
				nextTransitionTime = startTime + nrTransitions * transitionPeriodMs;
			}
			loop(time);
			// fg.sample(time, val);
			hw.writeDigitalOutput(WINDSENSOR_CH, val);
			time += samplePeriodMs;
		}
		return time;
	}

	/*
	 * public void simulateFrequency(int freq, double duration) { // freq = 1 /
	 * T, sample tijd is 20 ms; dus max freq. is 50Hz / 2 = 25Hz if (freq > 25)
	 * Assert.fail("frequency must not be more than 25"); boolean input = false;
	 * hw.writeDigitalOutput(WINDSENSOR_CH, input); double delta = 1000 /
	 * (double) freq / 2; double curTijd = 0.0; // TODO input moet aan-uit maar
	 * volgens die frequentie for (int i = 0; i < (duration / 20); i++) {
	 * loop(20); curTijd += 20; if (curTijd > delta) { input = !input;
	 * hw.writeDigitalOutput(WINDSENSOR_CH, input); curTijd -= delta; } } }
	 */

	@Test()
	public final void simpleTest() {
		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, dom, HIGH_FREQ_THRESHOLD, LOW_FREQ_THRESHOLD, HIGH_TIME_BEFORE_ALERT, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		seq = cur = 0L;
		long time = 0L;
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug("\n=============\nSTART LOW FREQ FOR 5 SEC\n=============");
		// frequency gauge op snelheid brengen
		time = simulateWind(LOW_FREQ_THRESHOLD, 5000, time);
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug("\n=============\nSTART HIGH FREQ \n=============");
		time = simulateWind(HIGH_FREQ_THRESHOLD + 2, HIGH_TIME_BEFORE_ALERT - 100, time);
		Assert.assertEquals(WindSensor.States.HIGH, ws.getState());

		log.debug("\n=============\n BACK TO NORMAL FOR 5 SEC\n=============");
		// frequency gauge op snelheid brengen
		time = simulateWind(LOW_FREQ_THRESHOLD, 5000, time);
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug("\n=============\nMUST GO TO ALARM \n=============");
		time = simulateWind(HIGH_FREQ_THRESHOLD + 2, HIGH_TIME_BEFORE_ALERT + 1000, time);
		Assert.assertEquals(WindSensor.States.ALARM, ws.getState());

		// simulateFrequency(HIGH_FREQ_THRESHOLD + 5,
		// HIGH_TIME_BEFORE_ALERT-100);
		// Assert.assertEquals(WindSensor.States.HIGH, ws.getState());
		//
		// simulateFrequency(HIGH_FREQ_THRESHOLD + 5, 200);
		// Assert.assertEquals(WindSensor.States.ALARM, ws.getState());

	}
}
