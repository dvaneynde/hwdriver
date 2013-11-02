package eu.dlvm.domotics.mappers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.ISensorListener;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.domotics.sensors.DimmerSwitches;

/**
 * Switch board for connecting {@link DimmerSwitches} to {@link DimmedLamp}'s.
 * <p>
 * A {@link DimmerSwitches} is connected to one {@link DimmedLamp} only, and
 * inversely. <br/>
 * DimmerSwitches [1] --- [1] DimmedLamp.
 * <p>
 * For the dimmers, depending on the input (see {@link DimmerSwitches.ClickType}
 * ), the following happens, only describing the LEFT part:<ul>
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

	private Map<DimmerSwitches, DimmedLamp> switchesToDimmers = new HashMap<DimmerSwitches, DimmedLamp>();

	public SwitchBoardDimmers(String name, String description) {
		super(name, description);
	}

	public void add(DimmerSwitches ds, DimmedLamp dl) {
		switchesToDimmers.put(ds, dl);
		ds.registerListenerDeprecated(this);
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
			it.next().registerListenerDeprecated(this);
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

	@Override
	public void notify(SensorEvent e) {
		if (e.getSource() instanceof DimmerSwitches) {
			notify((DimmerSwitches) e.getSource(), e);
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
		return sb.toString();
	}

}
