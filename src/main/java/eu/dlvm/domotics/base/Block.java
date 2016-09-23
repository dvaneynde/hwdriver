package eu.dlvm.domotics.base;

/**
 * A block is a building block for anything from switches to lamps.
 * 
 * @author dirk vaneynde
 * 
 * TODO ui must go out here
 */
public abstract class Block {

	protected String name;
	protected String description;
	protected String uiPosition;

	public Block(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Block(String name, String description, String ui) {
		this(name, description);
		this.uiPosition = ui;
	}

	@Override
	public String toString() {
		return "Block name='" + name + ", ui-position=" + uiPosition;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUiPosition() {
		return uiPosition;
	}
}
