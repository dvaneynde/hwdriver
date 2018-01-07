package eu.dlvm.domotics.controllers;

import java.util.Map;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.controllers.gadgets.Blink;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.controllers.gadgets.OnOff;
import eu.dlvm.domotics.controllers.gadgets.RandomOnOff;
import eu.dlvm.domotics.controllers.gadgets.Sinus;

/**
 * Builds three GadgetSets:<ol>
 * <li>switch participating lamps off</li>
 * <li>does a 10 second count down (on-off-on...)</li>
 * <li>then a lot of random effects and sine with dimmers</li>
 * </ol>
 * @author dirk
 *
 */
public class NewYearBuilder {

	public GadgetController build(Map<String, Block> blocks, long startTimeMs, long endTimeMs, IDomoticContext ctx) {
		
		GadgetController ny = new GadgetController("newyear", startTimeMs, endTimeMs, ctx);

		/*
		 * <sine lamp="LichtZithoek" cycle-ms="5000" cycle-start-deg="0" />
		 * <sine lamp="LichtCircanteRondom" cycle-ms="5000"
		 * cycle-start-deg="120" /> <sine lamp="LichtVeranda" cycle-ms="5000"
		 * cycle-start-deg="240" /> <onoff lamp="LichtBureau" /> <random
		 * lamp="LichtCircante" min-on-ms="500" rand-mult-ms="1000" />
		 */
		int gadgetSetStartTime = 0;
		int gadgetSetEndTime;
		{
			// Alles af
			gadgetSetStartTime = 0;
			gadgetSetEndTime = gadgetSetStartTime + 100;
			GadgetSet gs = new GadgetSet();
			gs.startMs = gadgetSetStartTime;
			gs.endMs = gadgetSetEndTime;
			ny.addGadgetSet(gs);
			OnOff oo = new OnOff();
			oo.add(oo.new TodoEvent(gadgetSetStartTime, false));
			addLamps2OnOff(blocks, oo, false);
			gs.gadgets.add(oo);
		}
		{
			// Aftellen
			gadgetSetStartTime = gadgetSetEndTime + 50;
			gadgetSetEndTime = gadgetSetStartTime + 10 * 1000;
			GadgetSet gs = new GadgetSet();
			gs.startMs = gadgetSetStartTime;
			gs.endMs = gadgetSetEndTime;
			ny.addGadgetSet(gs);
			Lamp lamp;
			lamp = (Lamp) blocks.get("LichtCircante");
			gs.gadgets.add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtKeuken");
			gs.gadgets.add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtBureau");
			gs.gadgets.add(new Blink(lamp, 1));
			lamp = (Lamp) blocks.get("LichtInkom");
			gs.gadgets.add(new Blink(lamp, 1));
			// lamp = (Lamp) blocks.get("LichtGangBoven");
			// gs.gadgets.add(new Blink(lamp, 1));
		}
		{
			// Show
			gadgetSetStartTime = gadgetSetEndTime + 50;
			gadgetSetEndTime = gadgetSetStartTime + 30 * 1000;
			GadgetSet gs = new GadgetSet();
			gs.startMs = gadgetSetStartTime;
			gs.endMs = gadgetSetEndTime;
			ny.addGadgetSet(gs);
			// Zet Dimmers terug aan
			OnOff oo = new OnOff();
			addLamps2OnOff(blocks, oo, true);
			gs.gadgets.add(oo);
			// Start show
			Lamp lamp;
			lamp = (Lamp) blocks.get("LichtCircante");
			gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtKeuken");
			gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtBureau");
			gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
			lamp = (Lamp) blocks.get("LichtInkom");
			gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
			// lamp = (Lamp) blocks.get("LichtGangBoven");
			// gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
			DimmedLamp dl;
			dl = (DimmedLamp) blocks.get("LichtZithoek");
			gs.gadgets.add(new Sinus(dl, 3000, 0));
			dl = (DimmedLamp) blocks.get("LichtCircanteRondom");
			gs.gadgets.add(new Sinus(dl, 3000, 120));
			dl = (DimmedLamp) blocks.get("LichtVeranda");
			gs.gadgets.add(new Sinus(dl, 3000, 240));
		}

		return ny;
	}

	private void addLamps2OnOff(Map<String, Block> blocks, OnOff oo, boolean dimmersOnly) {
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
