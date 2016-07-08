package eu.dlvm.domotics.service;

public class ScreenRobotInfo {

	private boolean robotOn;
	private int sunLevel;
	private float windLevel;

	public ScreenRobotInfo(boolean robotOn, int sunLevel, float windLevel) {
		super();
		this.robotOn = robotOn;
		this.sunLevel = sunLevel;
		this.windLevel = windLevel;
	}

	public boolean isRobotOn() {
		return robotOn;
	}

	public void setRobotOn(boolean robotOn) {
		this.robotOn = robotOn;
	}

	public int getSunLevel() {
		return sunLevel;
	}

	public void setSunLevel(int sunLevel) {
		this.sunLevel = sunLevel;
	}

	public float getWindLevel() {
		return windLevel;
	}

	public void setWindLevel(float windLevel) {
		this.windLevel = windLevel;
	}
	
	
}
