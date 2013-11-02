package eu.dlvm.domotics.base;

/**
 * A block is an abstraction of 'electronics' components making up home
 * electronics. Blocks are either electricity schemes (e.g. wiring scheme) or
 * sensors (e.g. switches) or actuators (e.g. lamps).
 * <p>
 * Note that these Blocks are on a high abstraction level: it is OK to have
 * Lamps, Pushbuttons, SwitchBoardScreens. It is not OK to speak about Relays.
 * But sometimes it is difficult to draw the line...
 * 
 * @author dirk vaneynde
 * 
 */
public abstract class Block {

	protected String name;
	protected String description;

	public Block(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String toString() {
		return "Block name='" + name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}
