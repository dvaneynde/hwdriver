package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock Hardware Driver when simulating. This is not for automated tests, but
 * exploring while really running the app.
 * 
 * @author dirk
 *
 */
public class HwDriverChannelSimulator implements IHwDriverChannel {

	private boolean sunWindSimulation = true;

	private int levelLight = 3000;
	private int directionLight = 1;
	private int inputWind = 0;
	private int ctrWind = 0;
	private int modWind = 10;

	@Override
	public void connect() {
	}

	@Override
	public List<String> sendAndRecv(String stringToSend, Reason reason) {
		ArrayList<String> responses = new ArrayList<>();
		if (reason == Reason.INPUT) {
			// reset("INP_O 0x380 255\n\n","")

			if (sunWindSimulation) {
				responses.add("INP_D 0x330 0 " + levelLight + " -");
				if (levelLight == 4000) {
					directionLight = -1;
				} else if (levelLight == 500)
					directionLight = 1;
				levelLight += 1 * directionLight;

				responses.add("INP_D 0x300 " + inputWind + " - -");
				// 50 ms is 20 keer per seconde.
				// Als modWind==5, dan elke 250ms transitie, dus 500ms
				// golflengte, dus 2Hz
				// Als modWind==1, dan elke 50ms transitie, dus 100ms
				// golflengte, dus 10Hz
				if (ctrWind % modWind == 0)
					inputWind = (inputWind == 0 ? 1 : 0);
				// verander frequentie elke 5 seconden, dus na 5 x 20 = 100 keer
				if (ctrWind % 200 == 0) {
					modWind *= 2;
					if (modWind > 32)
						modWind = 1;
				}
				ctrWind++;
			}
		}
		return responses;
	}

	@Override
	public void disconnect() {
	}

}
