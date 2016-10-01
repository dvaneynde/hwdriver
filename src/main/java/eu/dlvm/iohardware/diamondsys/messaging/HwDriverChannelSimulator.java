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

	private int levelLight = 3000;
	private int directionLight = 1;
	private int inputWind = 0;
	private int ctrWind = 0;
	private int modWind = 5;

	@Override
	public void connect() {
	}

	@Override
	public List<String> sendAndRecv(String stringToSend, Reason reason) {
		ArrayList<String> responses = new ArrayList<>();
		if (reason == Reason.INPUT) {
			//reset("INP_O 0x380 255\n\n","")
			responses.add("INP_D 0x330 0 " + levelLight + " -");
			if (levelLight == 4000) {
				directionLight = -1;
			} else if (levelLight == 3000)
				directionLight = 1;
			levelLight += 1 * directionLight;

			// 50 ms is 20 keer per seconde.
			// Als modWind==5, dan elke 250ms transitie, dus 500ms golflengte, dus 2Hz
			// Als modWind==1, dan elke 50ms transitie, dus 100ms golflengte, dus 10Hz
			responses.add("INP_D 0x300 " + inputWind + " - -");
			if (ctrWind % modWind == 0)
				inputWind = (inputWind == 0 ? 1 : 0);
			if (ctrWind % 500 == 0)
				modWind = (modWind == 5 ? 1 : 5);
			ctrWind++;
		}
		return responses;
	}

	@Override
	public void disconnect() {
	}

}
