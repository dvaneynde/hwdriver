package eu.dlvm.domotics.factories;

import java.util.Map;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.controllers.GadgetController;
import eu.dlvm.domotics.controllers.gadgets.Blink;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.controllers.gadgets.OnOff;
import eu.dlvm.domotics.controllers.gadgets.RandomOnOff;
import eu.dlvm.domotics.controllers.gadgets.Sinus;

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
public class NewYearBuilder {

	public static GadgetController build(Map<String, Block> blocks, long startTimeMs, long endTimeMs, IDomoticBuilder ctx) {

		GadgetController ny = new GadgetController("newyear", startTimeMs, endTimeMs - startTimeMs, true, false, ctx);

		/*
		 * <sine lamp="LichtZithoek" cycle-ms="5000" cycle-start-deg="0" />
		 * <sine lamp="LichtCircanteRondom" cycle-ms="5000"
		 * cycle-start-deg="120" /> <sine lamp="LichtVeranda" cycle-ms="5000"
		 * cycle-start-deg="240" /> <onoff lamp="LichtBureau" /> <random
		 * lamp="LichtCircante" min-on-ms="500" rand-mult-ms="1000" />
		 */
		{
			// Alles af
			GadgetSet gs = new GadgetSet(100);
			ny.addGadgetSet(gs);
			OnOff oo = new OnOff();
			oo.add(oo.new Command(0, false));
			addLamps2OnOff(blocks, oo, false);
			gs.getGadgets().add(oo);
		}
		{
			// Aftellen
			GadgetSet gs = new GadgetSet(10 * 1000);
			ny.addGadgetSet(gs);
			Lamp lamp;
			lamp = (Lamp) blocks.get("LichtCircante");
			gs.getGadgets().add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtKeuken");
			gs.getGadgets().add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtBureau");
			gs.getGadgets().add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtInkom");
			gs.getGadgets().add(new Blink(lamp, 1));
			// lamp = (Lamp) blocks.get("LichtGangBoven");
			// gs.gadgets.add(new Blink(lamp, 1));
		}
		{
			// Show
			GadgetSet gs = new GadgetSet(30 * 1000);
			ny.addGadgetSet(gs);
			// Zet Dimmers terug aan
			//			OnOff oo = new OnOff();
			//			addLamps2OnOff(blocks, oo, true);
			//			gs.gadgets.add(oo);
			// Start show
			Lamp lamp;
			lamp = (Lamp) blocks.get("LichtCircante");
			gs.getGadgets().add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtKeuken");
			gs.getGadgets().add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtBureau");
			gs.getGadgets().add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtInkom");
			gs.getGadgets().add(new RandomOnOff(lamp, 500, 1000));
			// lamp = (Lamp) blocks.get("LichtGangBoven");
			// gs.getGadgets().add(new RandomOnOff(lamp, 500, 1000));
			DimmedLamp dl;
			dl = (DimmedLamp) blocks.get("LichtZithoek");
			gs.getGadgets().add(new Sinus(dl, 3000, 0));
			dl = (DimmedLamp) blocks.get("LichtCircanteRondom");
			gs.getGadgets().add(new Sinus(dl, 3000, 120));
			dl = (DimmedLamp) blocks.get("LichtVeranda");
			gs.getGadgets().add(new Sinus(dl, 3000, 240));
		}

		return ny;
	}

	private static void addLamps2OnOff(Map<String, Block> blocks, OnOff oo, boolean dimmersOnly) {
		if (!dimmersOnly) {
			oo.add((Lamp) blocks.get("LichtCircante"));
			oo.add((Lamp) blocks.get("LichtKeuken"));
			oo.add((Lamp) blocks.get("LichtBureau"));
			oo.add((Lamp) blocks.get("LichtInkom"));
			oo.add((Lamp) blocks.get("LichtGangBoven"));
		}
		oo.add((DimmedLamp) blocks.get("LichtZithoek"));
		oo.add((DimmedLamp) blocks.get("LichtCircanteRondom"));
		oo.add((DimmedLamp) blocks.get("LichtVeranda"));
	}

}
