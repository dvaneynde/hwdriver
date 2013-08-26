package eu.dlvm.domotica.blocks.concrete;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.SensorEvent;

/**
 * Connects a couple of {@link Switch} to one {@link Screen} to make it go up
 * and down. If enabled this couple can also make all screens go up and down, by
 * clicking long enough.
 * <p>
 * In addition, one such couple can be set as all-up and all-down, and not be connected to one particular Screen.
 * 
 * @author dirk vaneynde
 * 
 */
public class SwitchBoardScreens extends Block implements ISensorListener {
    static Logger log = Logger.getLogger(SwitchBoardScreens.class);

    private class Info {
        Screen screen;
        boolean allEnabled;
        boolean isUp;

        public Info(Screen sr, boolean isUp, boolean allEnabled) {
            this.screen = sr;
            this.isUp = isUp;
            this.allEnabled = allEnabled;
        }

        private String stringdump() {
            StringBuffer sb = new StringBuffer();
            sb.append("screen=").append(screen == null ? "n/a" : screen.getName()).append(" isUp=").append(isUp).append(" allEnabled=").append(allEnabled);
            return sb.toString();
        }
    }

    private Map<Switch, Info> switch2ScreenInfo = new HashMap<Switch, Info>(16);

    public SwitchBoardScreens(String name, String description) {
        super(name, description);
    }

    public void addScreen(Switch downSwitch, Switch upSwitch, Screen screen, boolean allEnabled) {
        switch2ScreenInfo.put(downSwitch, new Info(screen, false, allEnabled));
        downSwitch.registerListener(this);
        switch2ScreenInfo.put(upSwitch, new Info(screen, true, allEnabled));
        upSwitch.registerListener(this);
    }

    public void setAllUpDownWithSeparateSwitch(Switch downSwitch, Switch upSwitch) {
        switch2ScreenInfo.put(downSwitch, new Info(null, false, true));
        downSwitch.registerListener(this);
        switch2ScreenInfo.put(upSwitch, new Info(null, true, true));
        upSwitch.registerListener(this);
    }

    @Override
    public void notify(SensorEvent e) {
        if (!(e.getSource() instanceof Switch)) {
            log.warn("Received event from something unexpected: " + e.toString());
            return;
        }
        Info i = switch2ScreenInfo.get(e.getSource());
        if (i == null) {
            log.warn("No screen found for given switch: " + e.getSource().toString());
            return;
        }

        switch ((Switch.ClickType) (e.getEvent())) {
        case SINGLE:
            if ((i.screen == null) && (i.allEnabled)) {
                doAll(i.isUp);
            } else {
                if (i.isUp)
                    i.screen.up();
                else
                    i.screen.down();
            }
            break;
        case LONG:
            if (i.allEnabled) {
                doAll(i.isUp);
            }
            break;
        default:
            if (log.isDebugEnabled())
                log.debug("Ignored sensor event " + e);
        }
    }

    private void doAll(boolean isUp) {
        Collection<Info> is = switch2ScreenInfo.values();
        for (Info info : is) {
            if (isUp && info.isUp && info.screen != null) {
                info.screen.up();
            } else if (!isUp && !info.isUp && info.screen != null) {
                info.screen.down();
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("SwitchBoardFans (" + super.toString() + ")");
        for (Iterator<Switch> iterator = switch2ScreenInfo.keySet().iterator(); iterator.hasNext();) {
            Switch sw = iterator.next();
            Info info = switch2ScreenInfo.get(sw);
            sb.append(" [switch=").append(sw.getName()).append("-->").append(info.stringdump()).append(']');
        }
        return sb.toString();
    }

}
