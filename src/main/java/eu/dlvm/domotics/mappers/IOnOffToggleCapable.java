package eu.dlvm.domotics.mappers;


public interface IOnOffToggleCapable {

	public enum ActionType {
		ON, OFF, TOGGLE;
	}

	public void onEvent(ActionType action);
	
	public void on();
	public void off();
	public boolean toggle();
}
