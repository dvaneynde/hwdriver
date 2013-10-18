package eu.dlvm.domotica.blocks.concrete;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.Sensor;
import eu.dlvm.domotica.blocks.SensorEvent;

/**
 * TODO uitleg te complex...
 * Switch board, connecting pushbutton switches to
 * lamps.
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
 * @deprecated
 */
public class SwitchBoard extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoard.class);

	public static class Config {
		public Lamp lamp;
		public boolean allOffSwitch;
		public boolean allOnSwitch;
		public boolean isTimer;

		public Config(Lamp lamp, boolean allOffSwitch, boolean allOnSwitch) {
			this.lamp = lamp;
			this.allOffSwitch = allOffSwitch;
			this.allOnSwitch = allOnSwitch;
		}

		public Config(Lamp lamp, Timer timer) {
			this.lamp = lamp;
			isTimer = true;
		}

		@Override
		public String toString() {
			return "Config [lamp=" + lamp + ", allOffSwitch=" + allOffSwitch
					+ ", allOnSwitch=" + allOnSwitch + ", isTimer=" + isTimer
					+ "]";
		}
	}

	private Map<Sensor, Config> sensors = new HashMap<Sensor, Config>();

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

	public void add(Timer timer, Lamp lamp) {
		if (sensors.containsKey(timer))
			log.warn("Configuring, already contains given timer, will be overwritten. New timer="
					+ timer.getName() + ", switchboard=" + getName());
		Config c = new Config(lamp, timer);
		sensors.put(timer, c);
		timer.registerListener(this);
		if (log.isDebugEnabled())
			log.debug("addConfig(), timer=" + timer.getName() + ", sb="
					+ getName() + ", lamp=" + lamp);
	}

	private void addConfig(Switch s, Lamp o, boolean isAllOffSwitch,
			boolean isAllOnSwitch) {
		if (sensors.containsKey(s))
			log.warn("Configuring, already contains given switch, will be overwritten. New switch="
					+ s.getName() + ", switchboard=" + getName());
		Config i = new Config(o, isAllOffSwitch, isAllOnSwitch);
		sensors.put(s, i);
		s.registerListener(this);
		if (log.isDebugEnabled())
			log.debug("addConfig(), s=" + s.getName() + ", sb=" + getName()
					+ ", lamp=" + o + ", allOff=" + isAllOffSwitch + ", allOn="
					+ isAllOnSwitch);
	}

	@Override
	public void notify(SensorEvent e) {
		Config cfg = sensors.get(e.getSource());
		if (cfg.isTimer) {
			boolean status = (Boolean) e.getEvent();
			cfg.lamp.setOn(status);
		} else {
			switch ((Switch.ClickType) (e.getEvent())) {
			case SINGLE:
				if (cfg.lamp != null)
					cfg.lamp.toggle();
				break;
			case LONG:
				if (cfg.allOffSwitch)
					for (Config i2 : sensors.values())
						if (i2.lamp != null)
							i2.lamp.setOn(false);
				break;
			case DOUBLE:
				if (cfg.allOnSwitch)
					for (Config i2 : sensors.values())
						if (cfg.lamp != null)
							i2.lamp.setOn(true);
				break;
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoard (" + super.toString()
				+ ")");
		for (Sensor sensor : sensors.keySet()) {
			Config config = sensors.get(sensor);
			sb.append("\n\tsensor=").append(sensor.getName()).append(" --> ")
					.append(config).append(']');
		}
		return sb.toString();
	}
}
