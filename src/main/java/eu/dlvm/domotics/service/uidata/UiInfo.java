package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * Data to update UI.
 * 
 * @author dirk
 *
 */
public class UiInfo {
	private String name;
	private String type;
	private String description = "";
	private String group = "";
	private String status = "";

	public UiInfo() {
	}

	public UiInfo(Block block, String status) {
		// this(block.getName(), block.getClass().getSimpleName(), block.getUiGroup(), block.getDescription());
		setName(block.getName());
		setType(block.getClass().getSimpleName());
		setGroup(block.getUiGroup());
		setDescription(block.getDescription());
		setStatus(status);
	}

//	public UiInfo(String name, String type, String uiGroup, String description) {
//		this();
//		setName(name);
//		setType(type);
//		setGroup(uiGroup);
//		setDescription(description);
//	}

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
		this.description = (description == null ? "" : description);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = (status == null ? "" : status);
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = (group == null ? "" : group);
	}

	@Override
	public String toString() {
		return "UiInfoOnOff [name=" + name + ", type=" + type + ", description=" + description + ", group=" + group + ", status=" + status + "]";
	}

}
