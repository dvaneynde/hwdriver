package eu.dlvm.domotica.blocks.concrete;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotica.blocks.Domotic;
import eu.dlvm.domotica.blocks.concrete.Lamp;
import eu.dlvm.domotica.blocks.concrete.Switch;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard;
import eu.dlvm.iohardware.LogCh;

public class TestSwitchBoardDedicatedAllOff {
    static Logger log = Logger.getLogger(TestSwitchBoardDedicatedAllOff.class);
    private TestSwitchBoard.Hardware hw;
    private Domotic dom;
    private Switch swLamp, swAllOff;
    private Lamp lamp;
    private SwitchBoard ssb;
    private long cur;

    @Before
    public void init() {
        cur = 0L;

        hw = new TestSwitchBoard.Hardware();
        hw.in(0, false);
        hw.in(1, false);
        hw.out(10, false);

        dom = new Domotic(hw);
        swLamp = new Switch("SwitchLamp", "Switch Lamp", new LogCh(0), dom);
        swAllOff = new Switch("SwitchAllOff", "Switch All Off", new LogCh(1), dom);
        lamp = new Lamp("Lamp1", "Lamp1", new LogCh(10), dom);
        ssb = new SwitchBoard("ssb", "ssb");
    }

    @Test
    public void testAllOffOk() throws InterruptedException {
        swAllOff.setLongClickEnabled(true);
        swAllOff.setLongClickTimeout(100);
        swAllOff.setDoubleClickEnabled(false);
        swAllOff.setSingleClickEnabled(false);
        ssb.add(swLamp, lamp);
        ssb.add(swAllOff, true, false);
        dom.initialize();

        Assert.assertEquals(false, hw.out(10));
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 60);
        Assert.assertEquals(true, hw.out(10));
        hw.in(1, true);
        dom.loopOnce(cur += 1);
        hw.in(1, false);
        dom.loopOnce(cur += 120);
        Assert.assertEquals(false, hw.out(10));
    }

    @Test
    public void testAllOffTooShort() throws InterruptedException {
        swAllOff.setLongClickEnabled(true);
        swAllOff.setLongClickTimeout(100);
        swAllOff.setDoubleClickEnabled(false);
        swAllOff.setSingleClickEnabled(false);
        ssb.add(swLamp, lamp);
        ssb.add(swAllOff, true, false);
        dom.initialize();

        Assert.assertEquals(false, hw.out(10));
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 60);
        Assert.assertEquals(true, hw.out(10));
        hw.in(1, true);
        dom.loopOnce(cur += 1);
        hw.in(1, false);
        dom.loopOnce(cur += 99);
        Assert.assertEquals(true, hw.out(10));
    }

    @Test
    public void testAllOffTogetherWithNormalSwitchAllOff() throws InterruptedException {
        swAllOff.setLongClickEnabled(true);
        swAllOff.setLongClickTimeout(100);
        swAllOff.setDoubleClickEnabled(false);
        swAllOff.setSingleClickEnabled(false);
        ssb.add(swAllOff, true, false);
        ssb.add(swLamp, lamp, true, false);
        dom.initialize();

        // lamp on
        Assert.assertEquals(false, hw.out(10));
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 60);
        Assert.assertEquals(true, hw.out(10));

        // all off via normal switch
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 120);
        Assert.assertEquals(false, hw.out(10));

        // lamp on again
        Assert.assertEquals(false, hw.out(10));
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 60);
        Assert.assertEquals(true, hw.out(10));

        // lamp off via dedicated switch
        hw.in(1, true);
        dom.loopOnce(cur += 1);
        hw.in(1, false);
        dom.loopOnce(cur += 120);
        Assert.assertEquals(false, hw.out(10));
    }

    @Test
    public void testAllOffTheSameAsNormalSwitchAllOff() throws InterruptedException {
        swLamp.setSingleClickEnabled(true);
        swLamp.setLongClickEnabled(true);
        swLamp.setLongClickTimeout(100);
        swLamp.setDoubleClickEnabled(false);
        ssb.add(swLamp, lamp, true, false);
        dom.initialize();

        // lamp on
        Assert.assertEquals(false, hw.out(10));
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 60);
        Assert.assertEquals(true, hw.out(10));

        // all off via normal switch
        hw.in(0, true);
        dom.loopOnce(cur += 1);
        hw.in(0, false);
        dom.loopOnce(cur += 120);
        Assert.assertEquals(false, hw.out(10));
    }

}
