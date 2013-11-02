package eu.dlvm.domotics.sensors;


public interface ISwitchListener {

	public static enum ClickType {
		SINGLE, DOUBLE, LONG;
	}

	public void onEvent(Switch source, ClickType click);
	
}
