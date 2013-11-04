package eu.dlvm.domotics.mappers;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.sensors.DimmerSwitch;
import eu.dlvm.domotics.sensors.IDimmerSwitchListener;

/**
 * Connects exactly one {@link DimmerSwitch}  to one {@link DimmedLamp}. <br/>
 * DimmerSwitches [1] --- [1] DimmedLamp.
 * <p>
 * For the dimmers, depending on the input (see {@link IDimmerSwitchListener.ClickType}
 * ), the following happens (only describing the LEFT part):<ul>
 * <li>LEFT_CLICK - switch on/off</li>
 * <li>LEFT_HOLD_DOWN - start dimming; if light was off first turn it on to
 * previous on-level</li>
 * <li>LEFT_RELEASED - stop dimming.</li>
 * <li>LEFT_WITH_RIGHTCLICK - set to full strength</li> </ul>
 * <p>Note: all-off or all-on can be realised by {@link Switch2OnOffToggle}.
 * @author dirk vaneynde
 */
public class DimmerSwitch2Dimmer extends Block implements IDimmerSwitchListener {
	static Logger log = Logger.getLogger(DimmerSwitch2Dimmer.class);

	private DimmedLamp dimmedLamp;

	public DimmerSwitch2Dimmer(String name, String description) {
		super(name, description);
	}

	public DimmedLamp getLamp() {
		return dimmedLamp;
	}

	public void setLamp(DimmedLamp lamp) {
		this.dimmedLamp = lamp;
	}


	@Override
	public void onEvent(DimmerSwitch source, ClickType click) {
		switch (click) {
		case LEFT_CLICK:
		case RIGHT_CLICK:
			dimmedLamp.toggle();
			break;
		case LEFT_HOLD_DOWN:
			dimmedLamp.down(true);
			break;
		case LEFT_RELEASED:
			dimmedLamp.down(false);
			break;
		case RIGHT_HOLD_DOWN:
			dimmedLamp.up(true);
			break;
		case RIGHT_RELEASED:
			dimmedLamp.up(false);
			break;
		case LEFT_WITH_RIGHTCLICK:
		case RIGHT_WITH_LEFTCLICK:
			dimmedLamp.on(100);
			break;
		}
	}

	@Override
	public String toString() {
		return "SwitchBoardDimmers [dimmedLamp=" + dimmedLamp + "]";
	}

}
