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
 * Switch [1..n] --- [1] Lamp.
 * <p>
 * A {@link Switch} can toggle one lamp, do all lamps off, or do all lamps on,
 * or any of these three combinations.
 * <p>
 * All-on or all-off is for all lamps registered in this Switchboard. If you
 * would like to exclude some lamps you must use another switchboard.
 * <p>
 * Switch types ({@link Switch.ClickType}) are used as follows:
 * <ul>
 * <li>SINGLE - toggle on/off one lamp</li>
 * <li>LONG - switch all lamps off, if enabled on this switch</li>
 * <li>DOUBLE - switch all lamps on, if enabled on this switch</li>
 * </ul>
 * 
 * @author dirk vaneynde
 */
public class SwitchBoard extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoard.class);

	public static class Config {
		public Lamp onoff;
		public boolean allOffSwitch;
		public boolean allOnSwitch;

		public Config(Lamp o, boolean allOffSwitch, boolean allOnSwitch) {
			onoff = o;
			this.allOffSwitch = allOffSwitch;
			this.allOnSwitch = allOnSwitch;
		}
	}

	private Map<Switch, Config> configs = new HashMap<Switch, Config>();

	public SwitchBoard(String name, String description) {
		super(name, description);
	}

	public void add(Switch s, Lamp o) {
		add(s, o, false, false);
	}

	/**
	 * Add an all on or off switch, without a dedicated lamp.
	 * 
	 * @param s
	 * @param isAllOffSwitch
	 * @param isAllOnSwitch
	 */
	public void add(Switch s, boolean isAllOffSwitch, boolean isAllOnSwitch) {
		if (!(isAllOffSwitch || isAllOnSwitch))
			log.warn("Switch '" + s.getName() + "' as all-on or -off added, but neither is specified. Won't work.");
		add(s, null, isAllOffSwitch, isAllOnSwitch);
	}

	public void add(Switch s, Lamp o, boolean isAllOffSwitch, boolean isAllOnSwitch) {
		Config i = new Config(o, isAllOffSwitch, isAllOnSwitch);
		configs.put(s, i);
		s.registerListener(this);
	}

	/**
	 * Property that constitutes the SwitchBoard configuration.
	 * 
	 * @return map of {@link SwitchBoard} to {@link Config}
	 */
	public Map<Switch, Config> getSwitch2ConfigMap() {
		return configs;
	}

	/**
	 * Property that constitutes the SwitchBoard configuration.
	 * 
	 * @param configs
	 *            map of {@link SwitchBoard} to {@link Config}
	 */
	public void setSwitch2ConfigMap(Map<Switch, Config> configs) {
		this.configs = configs;
		for (Iterator<Switch> it = configs.keySet().iterator(); it.hasNext();) {
			it.next().registerListener(this);
		}
	}

	@Override
	public void notify(SensorEvent e) {
		if (!(e.getSource() instanceof Switch)) {
			log.warn("Received event from something unexpected: " + e.toString());
			return;
		}
		Config i = configs.get(e.getSource());
		switch ((Switch.ClickType) (e.getEvent())) {
		case SINGLE:
			if (i.onoff == null) {
				log.warn("Single click and no output lamp found for given source: " + e.getSource().toString()
						+ ". Configuration error?");
				return;
			}
			i.onoff.toggle();
			break;
		case LONG:
			if (i.allOffSwitch) {
				for (Config i2 : configs.values()) {
					if (i2.onoff != null)
						i2.onoff.setOn(false);
				}
			}
			break;
		case DOUBLE:
			if (i.allOnSwitch) {
				for (Config i2 : configs.values()) {
					if (i2.onoff != null)
						i2.onoff.setOn(true);
				}
			}
			break;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoard (" + super.toString() + ")");
		for (Iterator<Switch> iterator = configs.keySet().iterator(); iterator.hasNext();) {
			Switch sw = (Switch) iterator.next();
			Config config = configs.get(sw);
			sb.append(" [switch=").append(sw.getName()).append("-->lamp=").append(
					config.onoff == null ? "null" : config.onoff.getName()).append(" allOn/Off=").append(config.allOnSwitch)
					.append('/').append(config.allOffSwitch).append(']');
		}
		return sb.toString();
	}
}
