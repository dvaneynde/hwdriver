package eu.dlvm.domotics.sensors;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.iohardware.LogCh;

/**
 * Events:
 * <ul>
 * <li>a) single click, links of rechts, op neergaande flank</li>
 * <li>b) ingedrukt houden, links of rechts</li>
 * <li>c) ingedrukt houden links, klik rechts opgaande flank - of andersom</li>
 * <li>d) beide gelijktijdig ingedrukt</li>
 * </ol>
 * <p>
 * Dimmer: a (aan/uit), b (dimmen), c(halve kracht), d(volle kracht)
 * 
 * @author dirk
 */
public class DimmerSwitch extends Sensor {
	static Logger log = LoggerFactory.getLogger(DimmerSwitch.class);

	private LogCh channelR;
	private long leftRESTtime = 0L;
	private boolean firstPressedLeft;
	private long clickTimeout = 400L;
	private Set<IDimmerSwitchListener> listeners = new HashSet<>();

	public enum States {
		REST, DOWN_SHORT, DOWN_LONG, BOTH_DOWN
	};

	private States state = States.REST;

	public States getState() {
		return state;
	}

	/**  */
	public DimmerSwitch(String name, String description, LogCh channelLeft, LogCh channelRight, IDomoticContext ctx) {
		super(name, description, channelLeft, ctx);
		this.channelR = channelRight;
	}

	/**
	 * Creates a DimmerSwitches.
	 * 
	 * @param name
	 * @param description
	 * @param channelLeft
	 * @param channelRight
	 * @param hw
	 */
	public DimmerSwitch(String name, String description, int channelLeft, int channelRight, IDomoticContext ctx) {
		this(name, description, new LogCh(channelLeft), new LogCh(channelRight), ctx);
	}

	@Override
	public String toString() {
		return "DimmerSwitches '" + name + "', chLeft=" + getChannel() + ", chRight=" + getChannelRight() + ", state="
				+ getState();
	}

	public void registerListener(IDimmerSwitchListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners(IDimmerSwitchListener.ClickType click) {
		for (IDimmerSwitchListener sl : listeners)
			sl.onEvent(this, click);
	}

	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInputLeft = getHw().readDigitalInput(getChannel());
		boolean newInputRight = getHw().readDigitalInput(getChannelRight());

		switch (state) {
		case REST:
			if (newInputLeft) {
				state = States.DOWN_SHORT;
				firstPressedLeft = true;
				leftRESTtime = currentTime;
			} else if (newInputRight) {
				state = States.DOWN_SHORT;
				firstPressedLeft = false;
				leftRESTtime = currentTime;
			} // otherwise ignore
			break;
		case DOWN_SHORT:
			if ((currentTime - leftRESTtime) >= getClickedTimeoutMS()) {
				IDimmerSwitchListener.ClickType ct = (firstPressedLeft ? IDimmerSwitchListener.ClickType.LEFT_HOLD_DOWN
						: IDimmerSwitchListener.ClickType.RIGHT_HOLD_DOWN);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.DOWN_LONG;
				break;
			} else if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				IDimmerSwitchListener.ClickType ct = (firstPressedLeft ? IDimmerSwitchListener.ClickType.LEFT_CLICK
						: IDimmerSwitchListener.ClickType.RIGHT_CLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				IDimmerSwitchListener.ClickType ct = (firstPressedLeft
						? IDimmerSwitchListener.ClickType.LEFT_WITH_RIGHTCLICK
						: IDimmerSwitchListener.ClickType.RIGHT_WITH_LEFTCLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.BOTH_DOWN;
			}
			break;
		case DOWN_LONG:
			if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				// Released the key being hold
				IDimmerSwitchListener.ClickType ct = (firstPressedLeft ? IDimmerSwitchListener.ClickType.LEFT_RELEASED
						: IDimmerSwitchListener.ClickType.RIGHT_RELEASED);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				IDimmerSwitchListener.ClickType ct = (firstPressedLeft
						? IDimmerSwitchListener.ClickType.LEFT_WITH_RIGHTCLICK
						: IDimmerSwitchListener.ClickType.RIGHT_WITH_LEFTCLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.BOTH_DOWN;
			}
			break;
		case BOTH_DOWN:
			if (!newInputLeft && !newInputRight) {
				state = States.REST;
			}
			break;
		default:
			throw new RuntimeException("Programming Error. Unhandled state.");
		}
	}

	public LogCh getChannelRight() {
		return channelR;
	}

	public long getClickedTimeoutMS() {
		return clickTimeout;
	}

	public void setClickedTimeoutMS(long clickedTimeoutMS) {
		this.clickTimeout = clickedTimeoutMS;
	}

}
