package eu.dlvm.domotics.sensors;

import eu.dlvm.iohardware.IHardwareReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;

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

	private String channelR;
	private long leftRESTtime = 0L;
	private boolean firstPressedLeft;
	private long clickTimeout = 400L;

	public enum States {
		REST, DOWN_SHORT, DOWN_LONG, BOTH_DOWN
	};

	private States state = States.REST;

	public States getState() {
		return state;
	}

	/**  */
	public DimmerSwitch(String name, String description, String channelLeft, String channelRight, IHardwareReader reader, IDomoticBuilder builder) {
		super(name, description, channelLeft, reader, builder);
		this.channelR = channelRight;
	}

	/**
	 * Creates a DimmerSwitches.
	 * 
	 * @param name
	 * @param description
	 * @param channelLeft
	 * @param channelRight
	 * @deprecated
	 */
	public DimmerSwitch(String name, String description, int channelLeft, int channelRight, IHardwareReader reader, IDomoticBuilder builder) {
		this(name, description, Integer.toString(channelLeft), Integer.toString(channelRight), reader, builder);
	}

	@Override
	public String toString() {
		return "DimmerSwitches '" + name + "', chLeft=" + getChannel() + ", chRight=" + getChannelRight() + ", state="
				+ getState();
	}

	@Override
	public void loop(long currentTime) {
		boolean newInputLeft = getHwReader().readDigitalInput(getChannel());
		boolean newInputRight = getHwReader().readDigitalInput(getChannelRight());

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
				EventType ct = (firstPressedLeft ? EventType.LEFT_HOLD_DOWN : EventType.RIGHT_HOLD_DOWN);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.DOWN_LONG;
				break;
			} else if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				EventType ct = (firstPressedLeft ? EventType.LEFT_CLICK : EventType.RIGHT_CLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				EventType ct = (firstPressedLeft ? EventType.LEFT_WITH_RIGHTCLICK : EventType.RIGHT_WITH_LEFTCLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.BOTH_DOWN;
			}
			break;
		case DOWN_LONG:
			if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				// Released the key being hold
				EventType ct = (firstPressedLeft ? EventType.LEFT_RELEASED : EventType.RIGHT_RELEASED);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListeners(ct);
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				EventType ct = (firstPressedLeft ? EventType.LEFT_WITH_RIGHTCLICK : EventType.RIGHT_WITH_LEFTCLICK);
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

	public String getChannelRight() {
		return channelR;
	}

	public long getClickedTimeoutMS() {
		return clickTimeout;
	}

	public void setClickedTimeoutMS(long clickedTimeoutMS) {
		this.clickTimeout = clickedTimeoutMS;
	}

}
