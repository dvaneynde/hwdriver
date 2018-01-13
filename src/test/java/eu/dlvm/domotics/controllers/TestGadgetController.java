package eu.dlvm.domotics.controllers;

import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.controllers.gadgets.IGadget;
import eu.dlvm.domotics.events.EventType;

public class TestGadgetController {

	public class TestGadget implements IGadget {

		private boolean busyRan, beforeRan, doneRan;
		public long busyTime = -1L;

		@Override
		public void onBefore() {
			beforeRan = true;
		}

		@Override
		public void onBusy(long time) {
			busyRan = true;
			busyTime = time;
		}

		@Override
		public void onDone() {
			doneRan = true;
		}

		public void reset() {
			beforeRan = false;
			busyRan = false;
			doneRan = false;
		}

		public boolean getAndResetBefore() {
			boolean r = beforeRan;
			beforeRan = false;
			return r;
		}

		public boolean getAndResetBusy() {
			boolean r = busyRan;
			busyRan = false;
			return r;
		}

		public boolean getAndResetDone() {
			boolean r = doneRan;
			doneRan = false;
			return r;
		}
	}

	private void check(TestGadget g, boolean before, boolean busy, boolean after) {
		Assert.assertEquals("testing before", before, g.getAndResetBefore());
		Assert.assertEquals("testing busy", busy, g.getAndResetBusy());
		Assert.assertEquals("testing after", after, g.getAndResetDone());

	}

	@Test
	public void startAndEndWithTiming() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		GadgetController gc = new GadgetController("TestGadgetController", 200, 20 * 1000, true, false, domoticContext);

		TestGadget g0 = new TestGadget();
		TestGadget g1 = new TestGadget();
		TestGadget g2 = new TestGadget();

		GadgetSet gs0 = new GadgetSet();
		gs0.durationMs = 100;
		gs0.gadgets.add(g0);

		GadgetSet gs1 = new GadgetSet();
		gs1.durationMs = 1000;
		gs1.gadgets.add(g1);

		GadgetSet gs2 = new GadgetSet();
		gs2.durationMs = 10000;
		gs2.gadgets.add(g2);

		gc.addGadgetSet(gs0);
		gc.addGadgetSet(gs1);
		gc.addGadgetSet(gs2);

		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		long time = 100L;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		// gs0 active, time >= 200
		time = 200L;
		gc.loop(time, time);
		check(g0, true, true, false);
		Assert.assertEquals(time - 200, g0.busyTime);
		Assert.assertTrue(gc.isRunning());
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());

		time += 20;
		gc.loop(time, time);
		check(g0, false, true, false);
		Assert.assertEquals(time - 200, g0.busyTime);

		time = 280;
		gc.loop(time, time);
		check(g0, false, true, false);
		Assert.assertEquals(time - 200, g0.busyTime);
		check(g1, false, false, false);
		check(g2, false, false, false);

		// gs1 active, time >= 300
		time = 300;
		gc.loop(time, time);
		check(g0, false, false, true);
		check(g1, true, true, false);
		Assert.assertEquals(time - 300, g1.busyTime);
		check(g2, false, false, false);

		time += 20;
		while (time < (1000 + 100 + 200)) {
			gc.loop(time, time);
			check(g0, false, false, false);
			check(g1, false, true, false);
			Assert.assertEquals(time - 300, g1.busyTime);
			check(g2, false, false, false);
			time += 20;
		}
		// gs2 active, time >= 1300
		gc.loop(time, time);
		check(g0, false, false, false);
		check(g1, false, false, true);
		check(g2, true, true, false);
		Assert.assertEquals(time - 1300, g2.busyTime);

		time += 20;
		while (time < (10000 + 1000 + 100 + 200)) {
			gc.loop(time, time);
			check(g0, false, false, false);
			check(g1, false, false, false);
			check(g2, false, true, false);
			Assert.assertEquals(time - 1300, g2.busyTime);
			Assert.assertTrue(gc.isRunning());
			time += 20;
		}
		gc.loop(time, time);
		check(g0, false, false, false);
		check(g1, false, false, false);
		check(g2, false, false, true);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.WAITING_END, gc.getState());

		gc.loop(20200, 20200);
		check(g0, false, false, false);
		check(g1, false, false, false);
		check(g2, false, false, false);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.WAITING_END, gc.getState());

		gc.loop(20220, 20220);
		check(g0, false, false, false);
		check(g1, false, false, false);
		check(g2, false, false, false);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());
	}

	@Test
	public void startWithLowLight() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		GadgetController gc = new GadgetController("TestGadgetController", 200, 20 * 1000, false, false, domoticContext);

		TestGadget g0 = new TestGadget();

		GadgetSet gs0 = new GadgetSet();
		gs0.durationMs = 100;
		gs0.gadgets.add(g0);
		gc.addGadgetSet(gs0);

		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		long time = 100L;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		// gs0 active
		time = 200L;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.WAITING_TRIGGER, gc.getState());
		time += 100L;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.WAITING_TRIGGER, gc.getState());

		gc.onEvent(null, EventType.TRIGGERED);
		time = 400;
		gc.loop(time, time);
		check(g0, true, true, false);
		Assert.assertEquals(time - 400, g0.busyTime);
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());

		time += 50;
		while (time < 400 + 100) {
			gc.loop(time, time);
			check(g0, false, true, false);
			Assert.assertEquals(time - 400, g0.busyTime);
			Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());
			time += 50;
		}
		gc.loop(time, time);
		check(g0, false, false, true);
		Assert.assertEquals(GadgetController.States.WAITING_END, gc.getState());
	}

	@Test
	public void startWithLowLightAndEndRepeat() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		GadgetController gc = new GadgetController("TestGadgetController", 200, 2 * 1000, false, true, domoticContext);

		TestGadget g0 = new TestGadget();

		GadgetSet gs0 = new GadgetSet();
		gs0.durationMs = 300;
		gs0.gadgets.add(g0);
		gc.addGadgetSet(gs0);

		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		long time = 100L;
		gc.loop(time, time);
		time = 200L;
		gc.loop(time, time);
		Assert.assertEquals(GadgetController.States.WAITING_TRIGGER, gc.getState());
		gc.onEvent(null, EventType.TRIGGERED);
		time = 400;
		gc.loop(time, time);
		check(g0, true, true, false);
		Assert.assertEquals(time - 400, g0.busyTime);
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());

		time += 50;
		while (time <= 200 + 2000) {
			System.out.println("time=" + time);
			gc.loop(time, time);
			//long relativeTime = time - 400;
			//			if (relativeTime > 450 && ((relativeTime - 50) % 200 == 0))
			//				check(g0, true, true, true);
			//			else
			//				check(g0, false, true, false);
			Assert.assertEquals(true, g0.getAndResetBusy());
			g0.reset();
			Assert.assertEquals((time - 400) % 300, g0.busyTime);
			Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());
			time += 50;
		}
		gc.loop(time, time);
		check(g0, false, false, true);
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());
	}

	@Test
	public void dailyStartBeforeEndOnSameDay() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		//GadgetController gc = new GadgetController("TestGadgetController", 17 * 3600 * 1000, 5 * 3600 * 1000, true, true, true, domoticContext);
		GadgetController gc = new GadgetController("TestGadgetController", true, false, Timer.timeInDayMillis(17, 0), Timer.timeInDayMillis(22, 0),
				domoticContext);

		TestGadget g0 = new TestGadget();

		GadgetSet gs0 = new GadgetSet();
		gs0.durationMs = 3600 * 1000;
		gs0.gadgets.add(g0);
		gc.addGadgetSet(gs0);

		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		long time = new GregorianCalendar(2018, 0, 1, 6, 0, 0).getTime().getTime();
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		time += 11 * 3600 * 1000;
		long timeEnd = time + 5 * 3600 * 1000;
		gc.loop(time, time);
		check(g0, true, true, false);
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());
		time += 50;
		gc.loop(time, time);
		check(g0, false, true, false);
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());
		time += gs0.durationMs;
		gc.loop(time, time);
		check(g0, false, false, true);
		Assert.assertEquals(GadgetController.States.WAITING_END, gc.getState());

		time = timeEnd;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.WAITING_END, gc.getState());
		time += 50;
		gc.loop(time, time);
		check(g0, false, false, false);
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());
	}

	@Test
	public void dailyStartAfterEndOnSameDay() {
		// TOOD
	}

}
