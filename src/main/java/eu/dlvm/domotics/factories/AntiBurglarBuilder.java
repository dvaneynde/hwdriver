package eu.dlvm.domotics.factories;

import java.util.Map;

import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.controllers.GadgetController;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.controllers.gadgets.RandomOnOff;

/**
 * Builds three GadgetSets:
 * <ol>
 * <li>switch participating lamps off</li>
 * <li>does a 10 second count down (on-off-on...)</li>
 * <li>then a lot of random effects and sine with dimmers</li>
 * </ol>
 * 
 * @author dirk
 *
 */
public class AntiBurglarBuilder {

	public static GadgetController build(Map<String, Block> blocks, String name, int onTime, int offTime, IDomoticContext ctx) {
		GadgetController.Builder builder = new GadgetController.Builder(name, true, ctx);
		GadgetController ab = builder.activateOnStart().repeat().setOnOffTime(onTime, offTime).build();
		ab.addGadgetSet(buildAntiBurglarGadgetSet(blocks));

		return ab;
	}

	private static GadgetSet buildAntiBurglarGadgetSet(Map<String, Block> blocksSoFar) {
		// Random aan/uit
		// TODO meer uit dan aan - nieuwe random maken
		GadgetSet gs = new GadgetSet(Integer.MAX_VALUE);
		Lamp lamp;
		lamp = (Lamp) blocksSoFar.get("LichtCircante");
		gs.getGadgets().add(new RandomOnOff(lamp, 120000, 300000));
		lamp = (Lamp) blocksSoFar.get("LichtKeuken");
		gs.getGadgets().add(new RandomOnOff(lamp, 100000, 360000));
		lamp = (Lamp) blocksSoFar.get("LichtBureau");
		gs.getGadgets().add(new RandomOnOff(lamp, 240000, 60000));
		lamp = (Lamp) blocksSoFar.get("LichtGangBoven");
		gs.getGadgets().add(new RandomOnOff(lamp, 30000, 120000));
		return gs;
	}

}
