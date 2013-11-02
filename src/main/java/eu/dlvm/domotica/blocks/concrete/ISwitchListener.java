package eu.dlvm.domotica.blocks.concrete;

public interface ISwitchListener {

	public static enum ClickType {
		SINGLE, DOUBLE, LONG;
	}

	public void onEvent(Switch source, ClickType click);
	
}
