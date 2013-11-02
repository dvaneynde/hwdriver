package eu.dlvm.domotics.blocks.concrete;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;

import eu.dlvm.domotics.base.ISensorListener;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.sensors.WindSensor;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestWindSensor {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;
		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inval;
		}
	};
	private Hardware hw = new Hardware();
	private long seq, cur;

	public static final LogCh WINDSENSOR_CH = new LogCh(10);
	public static int HIGH_SPEED_THRESHOLD = 20;
	public static int LOW_SPEED_THRESHOLD = 5;
	public static int HIGH_TIME_BEFORE_ALERT = 30 * 1000;
	public static int LOW_TIME_TO_RESET_ALERT = 180 * 1000;
	

	private ISensorListener sensorListener = new ISensorListener() {
		@Override
		public void notify(SensorEvent e) {
			lastEvent = e;
		}
	};
	private SensorEvent lastEvent;

	public void simulateFrequency(int freq, Hardware hw) {
		// TODO
		// Call loop n times switching hw.inval.
		// Loop period should be 
	}
	
	@BeforeClass
	public static void initLog() {
		BasicConfigurator.configure();
	}

	private void loop(WindSensor ws, long time) {
		cur += time;
		seq++;
		ws.loop(cur, seq);
	}

//	@Test
//	public final void simpleTest() {
//		this.ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, ctx, HIGH_SPEED_THRESHOLD,
//				LOW_SPEED_THRESHOLD, HIGH_TIME_BEFORE_ALERT, LOW_TIME_TO_RESET_ALERT);
//		this.gauge = new MockFrequencyGauge();
//		ws.gauge = gauge;
//		ws.registerListener(sensorListener);
//		
//		seq = cur = 0L;
//		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());
//		
//		loop(ws, 10, HIGH_SPEED_THRESHOLD+1);
//		Assert.assertEquals(WindSensor.States.TOO_HIGH, ws.getState());
//		
//		Assert.fail("En de rest moet ik nog doen...");
//	}
}
