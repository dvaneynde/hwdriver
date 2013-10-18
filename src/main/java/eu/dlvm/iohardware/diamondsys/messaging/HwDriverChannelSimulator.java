package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.ArrayList;
import java.util.List;

public class HwDriverChannelSimulator implements IHwDriverChannel {

	@Override
	public void connect() {
	}

	@Override
	public List<String> sendAndRecv(String stringToSend) {
		return new ArrayList<String>();
	}

	@Override
	public void disconnect() {
	}

}
