package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

public class UiInfoLevel extends UiInfo{

	private int level;
	
	public UiInfoLevel() {
	}

	public UiInfoLevel(Block block, String status, int level) {
		super(block, status);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}