package eu.dlvm.domotics;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel;

public class HwDriverChannelMock implements IHwDriverChannel {

	public String sentToDriver0;
	public String responseFromDriverToUse0;
	public String sentToDriver1;
	public String responseFromDriverToUse1;
	public int counter;

	public HwDriverChannelMock() {
		reset();
	}

	public void reset() {
		sentToDriver0 = responseFromDriverToUse0 = sentToDriver1 = responseFromDriverToUse1 = null;
		counter = 0;
	}

	public void reset(String responseToUse0, String responseToUse1) {
		sentToDriver0  = sentToDriver1  = null;
		this.responseFromDriverToUse0 = responseToUse0;
		this.responseFromDriverToUse1 = responseToUse1;
		counter = 0;
	}

	@Override
	public void connect() {
		;
	}

	@Override
	public List<String> sendAndRecv(String stringToSend, Reason reason) {
		List<String> result;
		if (counter % 2 == 0) {
			this.sentToDriver0 = stringToSend;
			result = convert(this.responseFromDriverToUse0);
		} else {
			sentToDriver1 = stringToSend;
			result = convert(this.responseFromDriverToUse1);
		}
		counter++;
		return result;
	}

	@Override
	public void disconnect() {
		;
	}

	private List<String> convert(String lines) {
		List<String> msgs = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(lines, "\n");
		while (st.hasMoreTokens()) {
			msgs.add(st.nextToken());
		}
		return msgs;
	}
}
