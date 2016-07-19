package eu.dlvm.domotics.service;

import java.util.List;
import java.util.Map;

public class Data {

	String title;
	List<String> groupNames;
	Map<String, List<BlockInfo>> groupname2infos;
	Map<String, Boolean> groupOn;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	public Map<String, List<BlockInfo>> getGroupname2infos() {
		return groupname2infos;
	}

	public void setGroupname2infos(Map<String, List<BlockInfo>> groupname2infos) {
		this.groupname2infos = groupname2infos;
	}

	public Map<String, Boolean> getGroupOn() {
		return groupOn;
	}

	public void setGroupOn(Map<String, Boolean> groupOn) {
		this.groupOn = groupOn;
	}

	@Override
	public String toString() {
		return "Data [title=" + title + ", groupNames=" + groupNames + ", groupname2infos=" + groupname2infos + ", groupOn=" + groupOn + "]";
	}
	
	
}
