package eu.dlvm.domotics.service;

import eu.dlvm.domotics.base.Block;

public class UiInfo {
	private String name;
	private String type;
	private String description="";
	private String groupName="";
	private boolean on;
	private int level;
	private String status="";


	public UiInfo() {
	}

	public UiInfo(Block block) { 
		this(block.getName(), block.getClass().getSimpleName(), block.getDescription());
	}
	
	public UiInfo(String name, String type, String description) {
		this();
		this.name = name;
		this.type = type;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "UiInfo [name=" + name + ", type=" + type + ", description=" + description + ", on=" + on
				+ ", level=" + level + "]";
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
