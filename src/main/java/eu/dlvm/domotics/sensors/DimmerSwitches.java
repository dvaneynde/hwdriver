package eu.dlvm.domotics.sensors;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.base.IHardwareAccess;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.base.SensorEvent;
import eu.dlvm.iohardware.LogCh;

/**
 * Events: a) single click, links of rechts, op neergaande flank b) ingedrukt
 * houden, links of rechts c) ingedrukt houden links, klik rechts opgaande flank
 * - of andersom d) beide gelijktijdig ingedrukt
 * 
 * Dimmer: a (aan/uit), b (dimmen), c(halve kracht), d(volle kracht)
 * 
 * @author dirk
 */
public class DimmerSwitches extends Sensor {
	static Logger log = Logger.getLogger(DimmerSwitches.class);

	private LogCh channelR;
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

	/**
	 * <dl>
	 * <dd>LEFT_CLICK</dd>
	 * <dd>RIGHT_CLICK</dd>
	 * <dd>LEFT_HOLD_DOWN</dd>
	 * <dd>LEFT_RELEASED</dd>
	 * <dd>RIGHT_HOLD_DOWN</dd>
	 * <dd>RIGHT_RELEASED</dd>
	 * <dd>LEFT_WITH_RIGHTCLICK</dd>
	 * <dd>RIGHT_WITH_LEFTCLICK</dd>
	 * </dl>
	 * 
	 * @author dirk
	 * 
	 */
	public static enum ClickType {
		LEFT_CLICK, RIGHT_CLICK, LEFT_HOLD_DOWN, LEFT_RELEASED, RIGHT_HOLD_DOWN, RIGHT_RELEASED, LEFT_WITH_RIGHTCLICK, RIGHT_WITH_LEFTCLICK;
	};

	/**  */
	public DimmerSwitches(String name, String description, LogCh channelLeft, LogCh channelRight, IHardwareAccess ctx) {
		super(name, description, channelLeft, ctx);
		this.channelR = channelRight;
	}

	/**
	 * Creates a DimmerSwitches.
	 * @param name
	 * @param description
	 * @param channelLeft
	 * @param channelRight
	 * @param hw
	 */
	public DimmerSwitches(String name, String description, int channelLeft, int channelRight, IHardwareAccess ctx) {
		this(name, description, new LogCh(channelLeft), new LogCh(channelRight), ctx);
	}

	@Override
	public String toString() {
		return "DimmerSwitches '" + name + "', chLeft=" + getChannel() + ", chRight=" + getChannelRight() + ", state="
				+ getState();
	}

	/**
	 * Signal from hardware received on given channel.
	 * <p>
	 * Houdt voorlopig geen rekening met dender !
	 * <p>
	 * <strong>Currently ClickType.Simultaneous is not supported!</strong>
	 */
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
			}// otherwise ignore
			break;
		case DOWN_SHORT:
			if ((currentTime - leftRESTtime) >= getClickedTimeoutMS()) {
				ClickType ct = (firstPressedLeft ? ClickType.LEFT_HOLD_DOWN : ClickType.RIGHT_HOLD_DOWN);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListenersDeprecated(new SensorEvent(this, ct));
				state = States.DOWN_LONG;
				break;
			} else if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				ClickType ct = (firstPressedLeft ? ClickType.LEFT_CLICK : ClickType.RIGHT_CLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListenersDeprecated(new SensorEvent(this, ct));
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				ClickType ct = (firstPressedLeft ? ClickType.LEFT_WITH_RIGHTCLICK : ClickType.RIGHT_WITH_LEFTCLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListenersDeprecated(new SensorEvent(this, ct));
				state = States.BOTH_DOWN;
			}
			break;
		case DOWN_LONG:
			if ((firstPressedLeft && !newInputLeft) || (!firstPressedLeft && !newInputRight)) {
				// Released the key being hold
				ClickType ct = (firstPressedLeft ? ClickType.LEFT_RELEASED : ClickType.RIGHT_RELEASED);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListenersDeprecated(new SensorEvent(this, ct));
				state = States.REST;
			} else if ((firstPressedLeft && newInputRight) || (!firstPressedLeft && newInputLeft)) {
				// Pressed a key down while already holding the other one
				ClickType ct = (firstPressedLeft ? ClickType.LEFT_WITH_RIGHTCLICK : ClickType.RIGHT_WITH_LEFTCLICK);
				log.info("DimmerSwitches '" + getName() + "' notifies " + ct + " event.");
				notifyListenersDeprecated(new SensorEvent(this, ct));
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
