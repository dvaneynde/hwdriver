package eu.dlvm.domotics.base;

/**
 * A block is represents an Sensor, Controller or Actuator, or Mapper together forming a
 * Domotic instance.
 * 
 * @author dirk vaneynde
 * 
 */
public abstract class Block {

	protected String name;
	protected String description;
	protected String ui;

	public Block(String name, String description, String ui) {
		this.name = name;
		this.description = description;
		this.ui = ui;
	}

	@Override
	public String toString() {
		return "Block name='" + name + ", ui=" + ui;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUi() {
		return ui;
	}
}
