package eu.dlvm.domotica.blocks.concrete;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.SensorEvent;

/**
 * Switch board, connecting pushbutton switches to lamps.
 * <p>
 * A {@link Switch} can toggle one lamp only, a lamp can be toggled by multiple
 * switches <br/>
 * Per SwitchBoard, each Switch instance should be registered only once. A 2nd
 * registration of the same will overwrite the 1st.
 * <p>
 * There are 2 general cases: a switch with a lamp, and a switch without. The
 * latter is intended for all-on and/or all-off only
 * <p>
 * <b><u>Switch with a lamp</u></b>
 * <p>
 * A {@link Switch} can toggle one lamp, do all lamps off, or do all lamps on,
 * or any of these three combinations. <br/>
 * All-on or all-off is for all lamps registered in this Switchboard. If you
 * would like to exclude some lamps you must use another switchboard.
 * <p>
 * Switch types ({@link Switch.ClickType}) are used as follows:
 * <ul>
 * <li>SINGLE - toggle on/off one lamp</li>
 * <li>LONG - switch all lamps off, if enabled on this switch</li>
 * <li>DOUBLE - switch all lamps on, if enabled on this switch</li>
 * </ul>
 * <p>
 * <b><u>Switch without a lamp.</u></b>
 * <p>
 * Used for all on or off. It can be a dedicated switch, or one that is
 * connected to a lamp in another SwitchBoard. It must be able to send LONG
 * events though.
 * <p>
 * Same events as above are used; of course SINGLE is not used here. Multiple
 * switches are possible, but they must not overlap with a normal one on this
 * switchboard.
 * 
 * @author dirk vaneynde
 */
public class SwitchBoard extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoard.class);

	public static class Config {
		public Lamp lamp;
		public boolean allOffSwitch;
		public boolean allOnSwitch;

		public Config(Lamp lamp, boolean allOffSwitch, boolean allOnSwitch) {
			this.lamp = lamp;
			this.allOffSwitch = allOffSwitch;
			this.allOnSwitch = allOnSwitch;
		}
	}

	private Map<Switch, Config> switches = new HashMap<Switch, Config>();

	public SwitchBoard(String name, String description) {
		super(name, description);
	}

	/**
	 * Add switch connected to a lamp.
	 */
	public void add(Switch s, Lamp lamp) {
		add(s, lamp, false, false);
	}

	/**
	 * Add switch connected to a lamp. Can double as all-off and/or all-on
	 * switch.
	 */
	public void add(Switch s, Lamp o, boolean isAllOffSwitch,
			boolean isAllOnSwitch) {
		addConfig(s, o, isAllOffSwitch, isAllOnSwitch);
	}

	/**
	 * Add an all on or off switch, without a dedicated lamp (at least not in
	 * this SwitchBoard).
	 */
	public void add(Switch s, boolean isAllOffSwitch, boolean isAllOnSwitch) {
		if (!(isAllOffSwitch || isAllOnSwitch))
			log.warn("Switch '"
					+ s.getName()
					+ "' as all-on or -off added, but neither is specified. Won't work.");
		addConfig(s, null, isAllOffSwitch, isAllOnSwitch);
	}

	private void addConfig(Switch s, Lamp o, boolean isAllOffSwitch,
			boolean isAllOnSwitch) {
		if (switches.containsKey(s))
			log.warn("Configuring, already contains given switch, will be overwritten. New switch="
					+ s.getName() + ", switchboard=" + getName());
		Config i = new Config(o, isAllOffSwitch, isAllOnSwitch);
		switches.put(s, i);
		s.registerListener(this);
		if (log.isDebugEnabled())
			log.debug("addConfig(), s=" + s.getName() + ", sb=" + getName()
					+ ", lamp=" + o + ", allOff=" + isAllOffSwitch + ", allOn="
					+ isAllOnSwitch);
	}

	@Override
	public void notify(SensorEvent e) {
		if (!(e.getSource() instanceof Switch)) {
			log.warn("Received event from something unexpected: "
					+ e.toString());
			return;
		}
		Config cfg = switches.get(e.getSource());
		switch ((Switch.ClickType) (e.getEvent())) {
		case SINGLE:
			if (cfg.lamp != null)
				cfg.lamp.toggle();
			break;
		case LONG:
			if (cfg.allOffSwitch)
				for (Config i2 : switches.values())
					if (i2.lamp != null)
						i2.lamp.setOn(false);
			break;
		case DOUBLE:
			if (cfg.allOnSwitch)
				for (Config i2 : switches.values())
					if (cfg.lamp != null)
						i2.lamp.setOn(true);
			break;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoard (" + super.toString()
				+ ")");
		for (Iterator<Switch> iterator = switches.keySet().iterator(); iterator
				.hasNext();) {
			Switch sw = (Switch) iterator.next();
			Config config = switches.get(sw);
			sb.append(" [switch=")
					.append(sw.getName())
					.append("-->lamp=")
					.append(config.lamp == null ? "null" : config.lamp
							.getName()).append(" allOn/Off=")
					.append(config.allOnSwitch).append('/')
					.append(config.allOffSwitch).append(']');
		}
		return sb.toString();
	}
}
