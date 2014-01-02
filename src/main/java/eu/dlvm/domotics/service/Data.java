package eu.dlvm.domotics.service;

import java.util.List;

import eu.dlvm.domotica.service.BlockInfo;

public class Data {

	String title;
	List<BlockInfo> actuators;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<BlockInfo> getActuators() {
		return actuators;
	}
	public void setActuators(List<BlockInfo> actuators) {
		this.actuators = actuators;
	}
}
