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
 * In addition, one such couple can be set as all-up and all-down, and not be
 * connected to one particular Screen.
 * 
 * @author dirk vaneynde
 * 
 */
public class SwitchBoardScreens extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoardScreens.class);

	private class Info {
		Screen relays;
		boolean allEnabled;
		boolean isUp;

		public Info(Screen sr, boolean isUp, boolean allEnabled) {
			this.relays = sr;
			this.isUp = isUp;
			this.allEnabled = allEnabled;
		}

		private String stringdump() {
			StringBuffer sb = new StringBuffer();
			sb.append("screen=").append(relays.getName()).append(" isUp=").append(isUp).append(" allEnabled=").append(
					allEnabled);
			return sb.toString();
		}
	}

	private Map<Switch, Info> map = new HashMap<Switch, Info>(16);

	public SwitchBoardScreens(String name, String description) {
		super(name, description);
	}

	public void addScreen(Switch dnSwitch, Switch upSwitch, Screen relays, boolean allEnabled) {
		map.put(dnSwitch, new Info(relays, false, allEnabled));
		dnSwitch.registerListener(this);
		map.put(upSwitch, new Info(relays, true, allEnabled));
		upSwitch.registerListener(this);
	}

	public void setAllUpDownWithSeparateSwitch(Switch downSwitch, Switch upSwitch) {
		map.put(downSwitch, new Info(null, false, true));
		map.put(upSwitch, new Info(null, true, true));
	}

	@Override
	public void notify(SensorEvent e) {
		if (!(e.getSource() instanceof Switch)) {
			log.warn("Received event from something unexpected: " + e.toString());
			return;
		}
		Info i = map.get(e.getSource());
		if (i == null) {
			log.warn("No relays found for given source: " + e.getSource().toString());
			return;
		}

		switch ((Switch.ClickType) (e.getEvent())) {
		case SINGLE:
			if ((i.relays == null) && (i.allEnabled)) {
				doAll(i.isUp);
			} else {
				if (i.isUp)
					i.relays.up();
				else
					i.relays.down();
			}
			break;
		case LONG:
			if (i.allEnabled) {
				doAll(i.isUp);
			}
			break;
		}
	}

	private void doAll(boolean isUp) {
		Collection<Info> is = map.values();
		for (Info info : is) {
			if (isUp && info.isUp) {
				info.relays.up();
			} else if (!isUp && !info.isUp) {
				info.relays.down();
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoardFans (" + super.toString() + ")");
		for (Iterator<Switch> iterator = map.keySet().iterator(); iterator.hasNext();) {
			Switch sw = iterator.next();
			Info info = map.get(sw);
			sb.append(" [switch=").append(sw.getName()).append("-->").append(info.stringdump()).append(']');
		}
		return sb.toString();
	}

}
