package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * Data to update UI.
 * 
 * @author dirk
 *
 */
public class UiInfoOnOffEco extends UiInfoOnOff {
	private boolean eco;

	public UiInfoOnOffEco() {
	}

	public UiInfoOnOffEco(Block block, String status, boolean on, boolean eco) {
		super(block, status, on);
		this.setEco(eco);
	}

	public boolean isEco() {
		return eco;
	}

	public void setEco(boolean eco) {
		this.eco = eco;
	}
}
