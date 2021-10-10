package eu.dlvm.domotics.sensors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.sensors.WindSensor.States;
import eu.dlvm.iohardware.IHardwareIO;

public class TestWindSensor implements IEventListener {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;

		@Override
		public void writeDigitalOutput(String channel, boolean value) throws IllegalArgumentException {
			inval = value;
		}

		@Override
		public boolean readDigitalInput(String channel) {
			return inval;
		}
	};

	static final long SAMPLE_TIME = 20;
	static final String WINDSENSOR_CH = Integer.toString(10);

	private static final Logger log = LoggerFactory.getLogger(TestWindSensor.class);
	private Hardware hw;
	private IDomoticBuilder dom;
	private WindSensor ws;
	private long time;
	private EventType lastEvent;
	private int nrEvents;


	@Override
	public void onEvent(Block source, EventType event) {
		lastEvent = event;
		nrEvents++;
	}

	/**
	 * @param freq
	 *            frequency to simulate
	 * @param durationMs
	 *            duration of this frequency
	 */
	void simulateWind(double freq, double durationMs) {
		double transitionPeriodMs = 1000 / (2.0 * freq);
		int nrTransitions = 1;
		long beginTime = time;
		double nextTransitionTime = beginTime + nrTransitions * transitionPeriodMs;

		boolean val = false;
		for (time = beginTime; time <= beginTime + durationMs; time += SAMPLE_TIME) {
			if (time >= nextTransitionTime) {
				val = !val;
				nrTransitions++;
				nextTransitionTime = beginTime + nrTransitions * transitionPeriodMs;
				hw.writeDigitalOutput(WINDSENSOR_CH, val);
			}
			ws.loop(time);
		}
	}

	@Before
	public void init() {
        hw = new Hardware();
        dom = new DomoticMock();
	}

	// ===============
	// TESTS

	@Test
	public final void simpleTest() {
		int HIGH_FREQ_THRESHOLD = 5;
		int LOW_FREQ_THRESHOLD = 2;
		int HIGH_TIME_BEFORE_ALERT = 1;
		int LOW_TIME_TO_RESET_ALERT = 2;

		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, hw, dom, HIGH_FREQ_THRESHOLD,
				LOW_FREQ_THRESHOLD, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		Assert.assertEquals(WindSensor.States.SAFE, ws.getState());

		log.debug("\n=============\nSTART LOW FREQ " + LOW_FREQ_THRESHOLD + " FOR 5 SEC\n=============");
		// frequency gauge op snelheid brengen
		simulateWind(LOW_FREQ_THRESHOLD, 5000);
		Assert.assertEquals(WindSensor.States.SAFE, ws.getState());

		log.debug(
				"\n=============\nMUST GO TO ALARM HIGH FREQ " + HIGH_FREQ_THRESHOLD + "+2 long enough\n=============");
		simulateWind(HIGH_FREQ_THRESHOLD + 2, HIGH_TIME_BEFORE_ALERT * 1000 + 1000);
		Assert.assertEquals(WindSensor.States.ALARM, ws.getState());

		log.debug("\n=============\n LOW FREQ " + LOW_FREQ_THRESHOLD + " FOR 5 SEC\n=============");
		simulateWind(LOW_FREQ_THRESHOLD, LOW_TIME_TO_RESET_ALERT * 1000 - 100);
		Assert.assertEquals(WindSensor.States.ALARM_BUT_LOW, ws.getState());
		simulateWind(LOW_FREQ_THRESHOLD, 1000);
		Assert.assertEquals(WindSensor.States.SAFE, ws.getState());
	}

	@Test()
	public final void testSafeAlarmSafe() {
		int HIGH_FREQ_THRESHOLD = 5;
		int LOW_FREQ_THRESHOLD = 1;
		int LOW_TIME_TO_RESET_ALERT = 30;

		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, hw, dom, HIGH_FREQ_THRESHOLD,
				LOW_FREQ_THRESHOLD, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		check(WindSensor.States.SAFE, null, 0);

		simulateWind(HIGH_FREQ_THRESHOLD + 1, 1000);
		log.info("600? " + ws.getFreqTimesHundred());
		check(States.ALARM, EventType.ALARM, 1);

		simulateWind(HIGH_FREQ_THRESHOLD - 1, 1000);
		log.info("600? " + ws.getFreqTimesHundred());
		check(States.ALARM, EventType.ALARM, 2);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 2000);
		log.info("600? " + ws.getFreqTimesHundred());
		check(States.ALARM_BUT_LOW, EventType.ALARM, 4);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 27000);
		check(States.ALARM_BUT_LOW, EventType.ALARM, 31);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 2000);
		check(States.SAFE, EventType.SAFE, 34);
	}

	private void check(WindSensor.States stateExpected, EventType eventExpected, int nrEventsExpected) {
		Assert.assertEquals(stateExpected, ws.getState());
		Assert.assertEquals(eventExpected, lastEvent);
		Assert.assertEquals(nrEventsExpected, nrEvents);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
