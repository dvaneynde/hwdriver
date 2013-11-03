package eu.dlvm.domotics.sensors;


public interface IDimmerSwitchListener {
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
	}

	public void onEvent(DimmerSwitch source, ClickType click);

}
