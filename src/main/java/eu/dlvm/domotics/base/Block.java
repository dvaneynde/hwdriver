package eu.dlvm.domotics.base;

/**
 * A block is a building block for anything from switches to lamps.
 * 
 * @author dirk vaneynde
 */
public abstract class Block {

	protected String name;
	protected String description;
	protected String uiGroup;

	private Block(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Block(String name, String description, String uiGroup) {
		this(name, description);
		this.uiGroup = uiGroup;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUiGroup() {
		return uiGroup;
	}

	@Override
	public String toString() {
		return "Block [name=" + name + ", description=" + description + ", uiGroup=" + uiGroup + "]";
	}
	
}
