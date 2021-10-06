package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

public class UiInfoOnOffLevel extends UiInfoOnOff{

	private int level;
	
	public UiInfoOnOffLevel() {
	}

	public UiInfoOnOffLevel(Block block, String status, boolean on, int level) {
		super(block, status, on);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}