package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * Data to update UI.
 * 
 * @author dirk
 *
 */
public class UiInfoOnOff extends UiInfo {
	private boolean on;

	public UiInfoOnOff() {
	}

	public UiInfoOnOff(Block block, String status, boolean on) {
		super(block, status);
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

}
