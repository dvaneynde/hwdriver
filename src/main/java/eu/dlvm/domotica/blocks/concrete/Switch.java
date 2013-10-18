package eu.dlvm.domotica.blocks.concrete;

import org.apache.log4j.Logger;

import eu.dlvm.domotica.blocks.IDomoContext;
import eu.dlvm.domotica.blocks.PreconditionViolated;
import eu.dlvm.domotica.blocks.Sensor;
import eu.dlvm.domotica.blocks.SensorEvent;
import eu.dlvm.iohardware.LogCh;

/**
 * A pushdown Switch.
 * <ol>
 * <li>
 * single-click: on > off</li>
 * <li>double-click: 2x (on > off) within n seconds, if enabled.</li>
 * <li>long: on-for-m-secs > off, if enabled</li>
 * </ol>
 * <p>
 * Note that you may need multiple {@link #loop(long, long)} executions before
 * the proper event is sent.
 * 
 * @author dirk vaneynde
 * 
 */
public class Switch extends Sensor {

	static Logger log = Logger.getLogger(Switch.class);

	public static long DEFAULT_LONG_TIMEOUT = 2000L;
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

	public static enum ClickType {
		SINGLE, DOUBLE, LONG;
	};

	public Switch(String name, String description, LogCh channel,
			IDomoContext ctx) {
		super(name, description, channel, ctx);
	}

	public Switch(String name, String description, LogCh channel,
			boolean singleClickEnabled, boolean longClickEnabled,
			boolean doubleClickEnabled, IDomoContext ctx) {
		super(name, description, channel, ctx);
		this.singleClickEnabled = singleClickEnabled;
		this.longClickEnabled = longClickEnabled;
		this.doubleClickEnabled = doubleClickEnabled;
		// TODO aparte validatie functie, voor zowel alles-disabled, en timings
		// double-long ?
	}

	public String toString() {
		return "Switch (" + super.toString() + ") (SINGLE,DOUBLE,LONG)=("
				+ isSingleClickEnabled() + ',' + isDoubleClickEnabled() + ','
				+ isLongClickEnabled() + ", (dblT,lngT)=("
				+ getDoubleClickTimeout() + ',' + getLongClickTimeout()
				+ "), state=" + getState();
	}

	/**
	 * Signal from hardware received on given channel.
	 * <p>
	 * Houdt voorlopig geen rekening met dender !
	 */
	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInputState = hw().readDigitalInput(getChannel());

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
					log.info("Switch '" + getName()
							+ "' notifies LONG click event (seq=" + sequence
							+ ").");
					notifyListeners(new SensorEvent(this, ClickType.LONG));
					state = States.WAIT_RELEASE;
					break;
				}
			}
			if (!newInputState) {
				if (isDoubleClickEnabled()) {
					state = States.WAIT_2ND_PRESS;
				} else if (isSingleClickEnabled()) {
					log.info("Switch '" + getName()
							+ "' notifies SINGLE click event (seq=" + sequence
							+ ").");
					notifyListeners(new SensorEvent(this, ClickType.SINGLE));
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
					log.info("Switch '" + getName()
							+ "' notifies SINGLE click event (seq=" + sequence
							+ ").");
					notifyListeners(new SensorEvent(this, ClickType.SINGLE));
				}
				state = States.REST;
			} else if (newInputState) {
				log.info("Switch '" + getName()
						+ "' notifies DOUBLE click event (seq=" + sequence
						+ ").");
				notifyListeners(new SensorEvent(this, ClickType.DOUBLE));
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
		if (!longClickLongerThanDoubleClick(getLongClickTimeout(),
				doubleClickTimeout)) {
			throw new PreconditionViolated(
					"Long click timeout must be longer than double click timeout.");
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
		if (!longClickLongerThanDoubleClick(longClickTimeout,
				getDoubleClickTimeout())) {
			throw new PreconditionViolated(
					"Long click timeout must be longer than double click timeout.");
		}
		this.longClickTimeout = longClickTimeout;
	}

	public boolean isSingleClickEnabled() {
		return singleClickEnabled;
	}

	public void setSingleClickEnabled(boolean singleClickEnabled) {
		this.singleClickEnabled = singleClickEnabled;
	}

	private boolean longClickLongerThanDoubleClick(long longClickTimeout,
			long doubleClickTimeout) {
		if (isLongClickEnabled() && isDoubleClickEnabled()) {
			return (longClickTimeout > doubleClickTimeout);
		} else {
			return true; // in fact, not applicable, but precondition is ok
		}
	}
}
