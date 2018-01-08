package eu.dlvm.domotics.controllers;

import org.junit.Assert;
import org.junit.Test;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.controllers.gadgets.IGadget;

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

	@Test
	public void happyPath() {
		IDomoticContext domoticContext = new DomoContextMock(null);
		GadgetController gc = new GadgetController("TestGadgetController", 0, 20 * 1000, false, domoticContext);

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
		
		// gs0 active
		long time = 0L;
		gc.loop(time, time);
		check(g0, true, true, false);
		Assert.assertEquals(time, g0.busyTime);
		Assert.assertTrue(gc.isRunning());
		Assert.assertEquals(GadgetController.States.ACTIF, gc.getState());
		
		time += 20;
		gc.loop(time, time);
		check(g0, false, true, false);
		Assert.assertEquals(time, g0.busyTime);

		time = 100;
		gc.loop(time, time);
		check(g0, false, true, false);
		Assert.assertEquals(time, g0.busyTime);
		check(g1, false, false, false);
		check(g2, false, false, false);

		// gs1 active, time > 100
		time = 120;
		gc.loop(time, time);
		check(g0, false, false, true);
		check(g1, true, true, false);
		Assert.assertEquals(time-100, g1.busyTime);
		check(g2, false, false, false);

		time += 20;
		while (time <= (1000+100)) {
			gc.loop(time, time);
			check(g0, false, false, false);
			check(g1, false, true, false);
			Assert.assertEquals(time-100, g1.busyTime);
			check(g2, false, false, false);
			time += 20;
		}
		// gs2 active, time > 1100
		gc.loop(time, time);
		check(g0, false, false, false);
		check(g1, false, false, true);
		check(g2, true, true, false);
		Assert.assertEquals(time-1100, g2.busyTime);
		
		time += 20;
		while (time <= (10000+1000+100)) {
			gc.loop(time, time);
			check(g0, false, false, false);
			check(g1, false, false, false);
			check(g2, false, true, false);
			Assert.assertEquals(time-1100, g2.busyTime);
			Assert.assertTrue(gc.isRunning());
			time += 20;
		}
		gc.loop(time, time);
		check(g0, false, false, false);
		check(g1, false, false, false);
		check(g2, false, false, true);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());

		gc.loop(20020, 20020);
		check(g0, false, false, false);
		check(g1, false, false, false);
		check(g2, false, false, false);
		Assert.assertFalse(gc.isRunning());
		Assert.assertEquals(GadgetController.States.INACTIF, gc.getState());
	}

	private void check(TestGadget g, boolean before, boolean busy, boolean after) {
		Assert.assertEquals("testing before", before, g.getAndResetBefore());
		Assert.assertEquals("testing busy", busy, g.getAndResetBusy());
		Assert.assertEquals("testing after", after, g.getAndResetDone());

	}

}
