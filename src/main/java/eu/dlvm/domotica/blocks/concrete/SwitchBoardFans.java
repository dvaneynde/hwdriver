package eu.dlvm.domotica.blocks.concrete;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.SensorEvent;

/**
 * Switchboard connecting one {@link Switch} to one {@link Fan}, 1 or more.
 * <p>
 * The Switch toggles the lamp on or off, see {@link Fan#toggle()}.
 * <p>
 * Note that Fans may also be triggered by a lamp, see
 * {@link Fan#Fan(String, String, eu.dlvm.domotica.hw.LogCh, eu.dlvm.domotica.hw.IHardwareIO)}
 * for more information. In that case the {@link Fan#turnOffUntilLampOff()} is
 * interesting, which is always connected to a long-click of the same Switch.
 * 
 * @author dirk vaneynde
 */
public class SwitchBoardFans extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoardFans.class);

	private Map<Switch, Fan> switch2Fan = new HashMap<Switch, Fan>();

	/**
	 * Constructs a SwitchBoardFan.
	 * 
	 * @param name
	 *            Identifying name.
	 * @param description
	 */
	public SwitchBoardFans(String name, String description) {
		super(name, description);
	}

	/**
	 * Connects a controlling Switch to a Fan.
	 * @param s
	 * @param f
	 */
	public void add(Switch s, Fan f) {
		switch2Fan.put(s, f);
		s.registerListener(this);
	}

	/**
	 * Property used for Spring configuration.
	 * @return the switch2Fan
	 */
	public Map<Switch, Fan> getSwitch2FanMap() {
		return switch2Fan;
	}

	/**
	 * Property used for Spring configuration.
	 * @param switch2Fan
	 *            the switch2Fan to set
	 */
	public void setSwitch2FanMap(Map<Switch, Fan> fans) {
		this.switch2Fan = fans;
		for (Iterator<Switch> it = fans.keySet().iterator(); it.hasNext();) {
			it.next().registerListener(this);
		}
	}

	@Override
	public void notify(SensorEvent e) {
		if (!(e.getSource() instanceof Switch)) {
			log.warn("Received event from something unexpected: "
					+ e.toString());
			return;
		}
		Fan f = switch2Fan.get(e.getSource());
		if (f == null) {
			log.warn("No Fan found for given Switch: "
					+ e.getSource().toString());
			return;
		}
		switch ((Switch.ClickType) (e.getEvent())) {
		case SINGLE:
			f.toggle();
			break;
		case LONG:
			f.turnOffUntilLampOff();
			break;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoardFans ("
				+ super.toString() + ")");
		for (Iterator<Switch> iterator = switch2Fan.keySet().iterator(); iterator
				.hasNext();) {
			Switch sw = (Switch) iterator.next();
			Fan fan = switch2Fan.get(sw);
			sb.append(" [switch=").append(sw.getName()).append("-->fan=")
					.append(fan.getName()).append(']');
		}
		return sb.toString();
	}

}
