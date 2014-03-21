package eu.dlvm.domotics.mappers;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.dlvm.domotics.actuators.Screen;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.service.BlockInfo;

/**
 * TODO wrong... Connects a {@link Switch} event to one or more {@link Screen}'s
 * with one fixed Screen operation, either Up or Down. So for one screen you
 * need two of these, connected to the same screen but to two different
 * switches, or more general two different (switch, switch-event) tuples
 * connected to one screen.
 * 
 * @author dirk vaneynde
 * 
 */
public class Switch2Screen extends Block implements ISwitchListener, IUserInterfaceAPI {

	static Logger log = Logger.getLogger(Switch2Screen.class);

	private Set<Screen> screens = new HashSet<>();
	private ISwitchListener.ClickType clickEvent;
	private Switch down;

	public Switch2Screen(String name, String description, String ui, Switch down, Switch up, ClickType clickEvent) {
		super(name, description, ui);
		this.down = down;
		this.clickEvent = clickEvent;
		down.registerListener(this);
		up.registerListener(this);
	}

	public void registerListener(Screen screen) {
		screens.add(screen);
	}

	@Override
	public void onEvent(Switch source, ClickType click) {
		if (click == clickEvent) {
			if (source.getName().equals(down.getName()))
				for (Screen screen : screens)
					screen.down();
			else
				for (Screen screen : screens)
					screen.up();
		} else {
			if (log.isDebugEnabled())
				log.debug("Switch2Screen " + getName() + ": ignored click event " + click + " from source=" + source);
		}
	}

	@Override
	public BlockInfo getBlockInfo() {
		BlockInfo bi = null;
		if (ui != null) {
			log.debug("getBlockInfo(), ui='" + ui + "'");
			bi = new BlockInfo(this.getName(), "DimmerSwitch", this.getDescription());
		}
		return bi;
	}

	// TODO slecht, apart block subtype van maken...
	@Override
	public void update(String action) {
		if (action.equals("down")) {
			for (Screen screen : screens)
				screen.down();
		} else if (action.equals("up")) {
			for (Screen screen : screens)
				screen.up();
		} else
			log.warn("update(), unknown action=" + action + ", on me=" + toString());
	}

	@Override
	public String toString() {
		return "Switch2Screen [screens=" + screens + ", clickEvent=" + clickEvent + ", down=" + down + "]";
	}

}
