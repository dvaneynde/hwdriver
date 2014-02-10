package eu.dlvm.domotica.service;

import java.util.HashMap;
import java.util.Map;

public class BlockInfo {
	private String name;
	private String type;
	private String description;
	private Map<String, String> parms;

	public BlockInfo() {
		parms = new HashMap<>();
	}

	public BlockInfo(String name, String type, String description) {
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

	public Map<String, String> getParms() {
		return parms;
	}

	public void setParms(Map<String, String> parms) {
		this.parms = parms;
	}

	public void addParm(String key, String value) {
		parms.put(key, value);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "BlockInfo [name=" + name + ", type=" + type + ", description=" + description + ", parms=" + parms + "]";
	}
}
