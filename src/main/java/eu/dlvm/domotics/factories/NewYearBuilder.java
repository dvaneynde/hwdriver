package eu.dlvm.domotics.factories;

import java.util.Map;

import eu.dlvm.domotics.actuators.DimmedLamp;
import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.actuators.NewYear;
import eu.dlvm.domotics.actuators.newyear.Blink;
import eu.dlvm.domotics.actuators.newyear.RandomOnOff;
import eu.dlvm.domotics.actuators.newyear.Sinus;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IHardwareAccess;

public class NewYearBuilder {

	public NewYear build(Map<String, Block> blocks, long startTimeMs, long endTimeMs, IHardwareAccess ctx) {
		NewYear ny = new NewYear("newyear", startTimeMs, endTimeMs, ctx);

		Lamp lamp;
		DimmedLamp dl;
		NewYear.GadgetSet gs;
		/*
		 * <sine lamp="LichtZithoek" cycle-ms="5000" cycle-start-deg="0" />
		 * <sine lamp="LichtCircanteRondom" cycle-ms="5000"
		 * cycle-start-deg="120" /> <sine lamp="LichtVeranda" cycle-ms="5000"
		 * cycle-start-deg="240" /> <onoff lamp="LichtBureau" /> <random
		 * lamp="LichtCircante" min-on-ms="500" rand-mult-ms="1000" />
		 */
		gs = ny.new GadgetSet();
		gs.startMs = 0;
		gs.endMs = 10 * 1000;
		lamp = (Lamp) blocks.get("LichtCircante");
		gs.gadgets.add(new Blink(lamp, 1));
		lamp = (Lamp) blocks.get("LichtKeuken");
		gs.gadgets.add(new Blink(lamp, 1));
		lamp = (Lamp) blocks.get("LichtBureau");
		gs.gadgets.add(new Blink(lamp, 1));
		lamp = (Lamp) blocks.get("LichtInkom");
		gs.gadgets.add(new Blink(lamp, 1));
		lamp = (Lamp) blocks.get("LichtGangBoven");
		gs.gadgets.add(new Blink(lamp, 1));
		ny.addEntry(gs);

		gs = ny.new GadgetSet();
		gs.startMs = 10 * 1000;
		gs.endMs = 15 * 1000;
		lamp = (Lamp) blocks.get("LichtCircante");
		gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
		lamp = (Lamp) blocks.get("LichtKeuken");
		gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
		lamp = (Lamp) blocks.get("LichtBureau");
		gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
		lamp = (Lamp) blocks.get("LichtInkom");
		gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
		lamp = (Lamp) blocks.get("LichtGangBoven");
		gs.gadgets.add(new RandomOnOff(lamp, 500, 1000));
		dl = (DimmedLamp) blocks.get("LichtZithoek");
		gs.gadgets.add(new Sinus(dl, 5000, 0));
		dl = (DimmedLamp) blocks.get("LichtCircanteRondom");
		gs.gadgets.add(new Sinus(dl, 5000, 120));
		dl = (DimmedLamp) blocks.get("LichtVeranda");
		gs.gadgets.add(new Sinus(dl, 5000, 240));
		ny.addEntry(gs);

		return ny;
	}

}
