package eu.dlvm.domotica.blocks.concrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.Block;
import eu.dlvm.domotica.blocks.ISensorListener;
import eu.dlvm.domotica.blocks.SensorEvent;
import eu.dlvm.domotica.blocks.concrete.SwitchBoard.Config;

/**
 * Switch board for connecting {@link DimmerSwitches} to {@link DimmedLamp}'s,
 * and optional all-on or all-off {@link Switch}es. *
 * <p>
 * A {@link DimmerSwitches} is connected to one {@link DimmedLamp} only, and
 * inversely. <br/>
 * DimmerSwitches [1] --- [1] DimmedLamp.
 * <p>
 * A {@link Switch} can do all lamps off, or do all lamps on, or both. Multiple
 * such Switches can be connected.
 * <p>
 * For all-on and all-off functionality see {@link SwitchBoard}; this board
 * behaves identically.
 * <p>
 * For the dimmers, depending on the input (see {@link DimmerSwitches.ClickType}
 * ), the following happens, only describing the LEFT part:
 * <li>LEFT_CLICK - switch on/off</li>
 * <li>LEFT_HOLD_DOWN - start dimming; if light was off first turn it on to
 * previous on-level</li>
 * <li>LEFT_RELEASED - stop dimming.</li>
 * <li>LEFT_WITH_RIGHTCLICK - set to full strength</li> </ul>
 * 
 * @author dirk vaneynde
 */
public class SwitchBoardDimmers extends Block implements ISensorListener {
	static Logger log = Logger.getLogger(SwitchBoardDimmers.class);

	public static class AllOnOffInfo {
		Switch s;
		boolean allOff;
		boolean allOn;

		public AllOnOffInfo(Switch s, boolean allOff, boolean allOn) {
			this.s = s;
			this.allOff = allOff;
			this.allOn = allOn;
		}
	}

	private List<AllOnOffInfo> allonoffSwitches = new ArrayList<AllOnOffInfo>();

	private AllOnOffInfo find(Switch s) {
		for (AllOnOffInfo i : allonoffSwitches) {
			if (i.s == s)
				return i;
		}
		return null;
	}

	private Map<DimmerSwitches, DimmedLamp> switchesToDimmers = new HashMap<DimmerSwitches, DimmedLamp>();

	public SwitchBoardDimmers(String name, String description) {
		super(name, description);
	}

	public void add(DimmerSwitches ds, DimmedLamp dl) {
		switchesToDimmers.put(ds, dl);
		ds.registerListener(this);
	}

	/**
	 * @return Map of all switches connected to one dimmer, and vice versa.
	 */
	public Map<DimmerSwitches, DimmedLamp> getMappings() {
		return switchesToDimmers;
	}

	/**
	 * Property that constitutes the SwitchBoard configuration.
	 * 
	 * @param configs
	 *            map of {@link SwitchBoard} to {@link Config}
	 */
	public void setMappings(Map<DimmerSwitches, DimmedLamp> mappings) {
		this.switchesToDimmers = mappings;
		for (Iterator<DimmerSwitches> it = switchesToDimmers.keySet()
				.iterator(); it.hasNext();) {
			it.next().registerListener(this);
		}
	}

	public void add(Switch allOnOffSwitch, boolean isAllOffSwitch,
			boolean isAllOnSwitch) {
		AllOnOffInfo i = new AllOnOffInfo(allOnOffSwitch,isAllOffSwitch,isAllOnSwitch);
		allonoffSwitches.add(i);
		allOnOffSwitch.registerListener(this);
	}

	public List<AllOnOffInfo> getAllOnOffs() {
		return allonoffSwitches;
	}

	public void setAllOnOffs(List<AllOnOffInfo> list) {
		for (AllOnOffInfo i : list) {
			add(i.s, i.allOff, i.allOn);
		}
	}

	private void notify(DimmerSwitches ds, SensorEvent e) {
		DimmedLamp dl = switchesToDimmers.get(ds);
		if (dl == null) {
			log.warn("Received notification from DimmerSwitch, which is not registered here. Ignored. Please fix.");
			return;
		}
		switch ((DimmerSwitches.ClickType) (e.getEvent())) {
		case LEFT_CLICK:
		case RIGHT_CLICK:
			dl.toggle();
			break;
		case LEFT_HOLD_DOWN:
			dl.down(true);
			break;
		case LEFT_RELEASED:
			dl.down(false);
			break;
		case RIGHT_HOLD_DOWN:
			dl.up(true);
			break;
		case RIGHT_RELEASED:
			dl.up(false);
			break;
		case LEFT_WITH_RIGHTCLICK:
		case RIGHT_WITH_LEFTCLICK:
			dl.on(100);
			break;
		default:
			log.warn("Unknown event from DimmerSwitch. Please fix.");
		}
	}

	private void notify(Switch s, SensorEvent e) {
		AllOnOffInfo i = find(s);
		if (i == null) {
			log.warn("Notified by Switch event, but that Switch is not registered here. Detail source: "
					+ e.getSource().toString());
			return;
		}
		switch ((Switch.ClickType) (e.getEvent())) {
		case SINGLE:
			break; // ignore
		case LONG:
			if (i.allOff) {
				for (DimmedLamp dl : switchesToDimmers.values()) {
					dl.off();
				}
			}
			break;
		case DOUBLE:
			if (i.allOn) {
				for (DimmedLamp dl : switchesToDimmers.values()) {
					dl.on(100);
				}
			}
			break;
		}
	}

	@Override
	public void notify(SensorEvent e) {
		if (e.getSource() instanceof DimmerSwitches) {
			notify((DimmerSwitches) e.getSource(), e);
		} else if (e.getSource() instanceof Switch) {
			notify((Switch) e.getSource(), e);
		} else {
			log.warn("Received event from something unexpected: "
					+ e.toString());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("SwitchBoardDimmers ("
				+ super.toString() + ")");
		for (Map.Entry<DimmerSwitches, DimmedLamp> e : switchesToDimmers
				.entrySet()) {
			sb.append(" [switches=").append(e.getKey().getName())
					.append("-->lamp=").append(e.getValue().getName())
					.append(']');
		}
		sb.append(" all on/off switches= [");
		for (Iterator<AllOnOffInfo> iter = allonoffSwitches.iterator(); iter
				.hasNext();) {
			AllOnOffInfo info = iter.next();
			sb.append(" [switch=").append(info.s.getName())
					.append(" allOn/Off=").append(info.allOn).append('/')
					.append(info.allOff).append(']');
		}
		sb.append(']');
		return sb.toString();
	}

}
