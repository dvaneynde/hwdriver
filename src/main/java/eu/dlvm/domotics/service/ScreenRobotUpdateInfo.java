package eu.dlvm.domotics.service;

public class ScreenRobotUpdateInfo {

	private boolean robotOn;

	public boolean isRobotOn() {
		return robotOn;
	}

	public void setRobotOn(boolean robotOn) {
		this.robotOn = robotOn;
	}

	@Override
	public String toString() {
		return "ScreenRobotUpdateInfo [robotOn=" + robotOn + "]";
	}
	
}
