package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;

/**
 * TODO geen aparte thread, gewoon hardcoderen wat je wil, b.v. wind of zon op en neer; niet zeker dat daar XmlHwConfigurator voor nodig is, maar bon.
 * 
 * @author dirk
 *
 */
public class HwDriverChannelSimulator implements IHwDriverChannel {

	/*
	private List<Board> boards;
	private ChannelMap map;

	public HwDriverChannelSimulator(XmlHwConfigurator xhc) {
		boards = new ArrayList<Board>();
		map = new ChannelMap();
		xhc.configure(boards, map);
	}
	*/

	@Override
	public void connect() {
	}

	@Override
	public List<String> sendAndRecv(String stringToSend) {
		 ArrayList<String> responses = new ArrayList<>();
		 //reset("INP_O 0x380 255\n\n","")
		 return responses;
	}

	@Override
	public void disconnect() {
	}

}
