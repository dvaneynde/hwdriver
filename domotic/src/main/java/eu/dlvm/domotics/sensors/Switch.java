package eu.dlvm.domotics.sensors;

import eu.dlvm.iohardware.IHardwareReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;

/**
 * A pushdown Switch.
 * <ol>
 * <li>single-click: on > off</li>
 * <li>double-click: 2x (on > off) within n seconds, if enabled.</li>
 * <li>long: on-for-m-secs > off, if enabled</li>
 * </ol>
 * <p>
 * Note that you may need multiple {@link #loop(long)} executions before
 * the proper event is sent.
 * 
 * @author dirk vaneynde
 * 
 */
public class Switch extends Sensor {

	static Logger logger = LoggerFactory.getLogger(Switch.class);

	public static long DEFAULT_LONG_TIMEOUT = 1200L;
	public static long DEFAULT_DOUBLE_TIMEOUT = 200L;

	private long leftRESTtime = 0L;
	private boolean singleClickEnabled = true;
	private boolean doubleClickEnabled = false;
	private long doubleClickTimeout = DEFAULT_DOUBLE_TIMEOUT;
	private boolean longClickEnabled = false;
	private long longClickTimeout = DEFAULT_LONG_TIMEOUT;

	public enum States {
		REST, FIRST_PRESS, WAIT_2ND_PRESS, WAIT_RELEASE
	};

	private States state = States.REST;

	public States getState() {
		return state;
	}

	public Switch(String name, String description, String channel, IHardwareReader reader, IDomoticBuilder builder) {
		super(name, description, channel, reader, builder);
	}

	public Switch(String name, String description, String channel, boolean singleClickEnabled, boolean longClickEnabled,
			boolean doubleClickEnabled, IHardwareReader reader, IDomoticBuilder builder) {
		super(name, description, channel, reader, builder);
		this.singleClickEnabled = singleClickEnabled;
		this.longClickEnabled = longClickEnabled;
		this.doubleClickEnabled = doubleClickEnabled;
	}

	/**
	 * Signal from hardware received on given channel.
	 * <p>
	 * Houdt voorlopig geen rekening met dender !
	 */
	@Override
	public void loop(long currentTime) {
		boolean newInputState = getHwReader().readDigitalInput(getChannel());

		switch (state) {
		case REST:
			if (newInputState) {
				state = States.FIRST_PRESS;
				leftRESTtime = currentTime;
			} // otherwise ignore
			break;
		case FIRST_PRESS:
			if (isLongClickEnabled()) {
				if ((currentTime - leftRESTtime) > getLongClickTimeout()) {
					logger.info("Switch '" + getName() + "' notifies LONG click event.");
					notifyListeners(EventType.LONG_CLICK);
					state = States.WAIT_RELEASE;
					break;
				}
			}
			if (!newInputState) {
				if (isDoubleClickEnabled()) {
					state = States.WAIT_2ND_PRESS;
				} else if (isSingleClickEnabled()) {
					logger.info("Switch '" + getName() + "' notifies SINGLE click event.");
					notifyListeners(EventType.SINGLE_CLICK);
					state = States.REST;
				} else if (isLongClickEnabled()) {
					state = States.REST; // Did not press long enough
				}
			}
			break;
		case WAIT_RELEASE:
			if (!newInputState) {
				state = States.REST;
			}
			break;
		case WAIT_2ND_PRESS:
			if ((currentTime - leftRESTtime) > getDoubleClickTimeout()) {
				if (isSingleClickEnabled()) {
					logger.info("Switch '" + getName() + "' notifies SINGLE click event.");
					notifyListeners(EventType.SINGLE_CLICK);
				}
				state = States.REST;
			} else if (newInputState) {
				logger.info("Switch '" + getName() + "' notifies DOUBLE click event.");
				notifyListeners(EventType.DOUBLE_CLICK);
				state = States.REST;
			}
			break;
		}
	}

	public boolean isDoubleClickEnabled() {
		return doubleClickEnabled;
	}

	public void setDoubleClickEnabled(boolean doubleClickEnabled) {
		this.doubleClickEnabled = doubleClickEnabled;
	}

	public long getDoubleClickTimeout() {
		return doubleClickTimeout;
	}

	public void setDoubleClickTimeout(long doubleClickTimeout) {
		if (!longClickLongerThanDoubleClick(getLongClickTimeout(), doubleClickTimeout)) {
			throw new ConfigurationException("Long click timeout must be longer than double click timeout.");
		}
		this.doubleClickTimeout = doubleClickTimeout;
	}

	public boolean isLongClickEnabled() {
		return longClickEnabled;
	}

	public void setLongClickEnabled(boolean longClickEnabled) {
		this.longClickEnabled = longClickEnabled;
	}

	public long getLongClickTimeout() {
		return longClickTimeout;
	}

	public void setLongClickTimeout(long longClickTimeout) {
		if (!longClickLongerThanDoubleClick(longClickTimeout, getDoubleClickTimeout())) {
			throw new ConfigurationException("Long click timeout must be longer than double click timeout.");
		}
		this.longClickTimeout = longClickTimeout;
	}

	public boolean isSingleClickEnabled() {
		return singleClickEnabled;
	}

	public void setSingleClickEnabled(boolean singleClickEnabled) {
		this.singleClickEnabled = singleClickEnabled;
	}

	private boolean longClickLongerThanDoubleClick(long longClickTimeout, long doubleClickTimeout) {
		if (isLongClickEnabled() && isDoubleClickEnabled()) {
			return (longClickTimeout > doubleClickTimeout);
		} else {
			return true; // in fact, not applicable, but precondition is ok
		}
	}

	public String toString() {
		return "Switch (" + super.toString() + ") (SINGLE,DOUBLE,LONG)=(" + isSingleClickEnabled() + ','
				+ isDoubleClickEnabled() + ',' + isLongClickEnabled() + ", (dblT,lngT)=(" + getDoubleClickTimeout()
				+ ',' + getLongClickTimeout() + "), state=" + getState();
	}
}
