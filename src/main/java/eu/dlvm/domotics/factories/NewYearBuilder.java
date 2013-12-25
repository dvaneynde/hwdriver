package eu.dlvm.domotics.factories;

import java.util.Map;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.actuators.NewYear;
import eu.dlvm.domotics.actuators.newyear.Blink;
import eu.dlvm.domotics.actuators.newyear.OnOff;
import eu.dlvm.domotics.actuators.newyear.RandomOnOff;
import eu.dlvm.domotics.actuators.newyear.Sinus;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IHardwareAccess;

public class NewYearBuilder {

	public NewYear build(Map<String, Block> blocks, long startTimeMs, long endTimeMs, IHardwareAccess ctx) {
		NewYear ny = new NewYear("newyear", startTimeMs, endTimeMs, ctx);

		// Lamp lamp;
		// DimmedLamp dl;
		/*
		 * <sine lamp="LichtZithoek" cycle-ms="5000" cycle-start-deg="0" />
		 * <sine lamp="LichtCircanteRondom" cycle-ms="5000"
		 * cycle-start-deg="120" /> <sine lamp="LichtVeranda" cycle-ms="5000"
		 * cycle-start-deg="240" /> <onoff lamp="LichtBureau" /> <random
		 * lamp="LichtCircante" min-on-ms="500" rand-mult-ms="1000" />
		 */
		int setStartTime = 0;
		int setEndTime;
		{
			// Alles af
			setStartTime = 0;
			setEndTime = setStartTime + 100;
			NewYear.GadgetSet gs = ny.new GadgetSet();
			gs.startMs = setStartTime;
			gs.endMs = setEndTime;
			ny.addEntry(gs);
			OnOff oo = new OnOff();
			oo.add(oo.new Event(setStartTime, false));
			addLamps2OnOff(blocks, oo, false);
			gs.gadgets.add(oo);
		}
		{
			// Aftellen
			setStartTime = setEndTime + 50;
			setEndTime = setStartTime + 10 * 1000;
			NewYear.GadgetSet gs = ny.new GadgetSet();
			gs.startMs = setStartTime;
			gs.endMs = setEndTime;
			ny.addEntry(gs);
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
			setStartTime = setEndTime + 50;
			setEndTime = setStartTime + 30 * 1000;
			NewYear.GadgetSet gs = ny.new GadgetSet();
			gs.startMs = setStartTime;
			gs.endMs = setEndTime;
			ny.addEntry(gs);
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
