package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

public class UiInfoLevel extends UiInfo {

	private int level, min, low, high, max;

	public UiInfoLevel() {
	}

	public UiInfoLevel(Block block, String status, int level, int min, int low, int high, int max) {
		super(block, status);
		this.level = level;
		this.min = min;
		this.low = low;
		this.high = high;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getLow() {
		return low;
	}

	public int getHigh() {
		return high;
	}

	public int getLevel() {
		return level;
	}

	public int getMax() {
		return max;
	}

}