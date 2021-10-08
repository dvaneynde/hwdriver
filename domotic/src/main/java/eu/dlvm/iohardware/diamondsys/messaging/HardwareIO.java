package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import eu.dlvm.iohardware.IHardwareReader;
import eu.dlvm.iohardware.IHardwareWriter;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.ChannelFault;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;
import eu.dlvm.iohardware.diamondsys.FysCh;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;
import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel.Reason;

/**
 * Diamond-systems specific implementation of {@link IHardwareIO}.
 * 
 * @author dirk vaneynde
 */
public class HardwareIO implements IHardwareIO {

	static Logger log = LoggerFactory.getLogger(HardwareIO.class);

	private List<Board> boards;
	private ChannelMap channelMap; // logical channel --> physical channel
	private IHwDriverChannel driverChannel;
	private int errorCount = 0;

	/**
	 * Create a Diamond Systems hardware configuration.
	 * 
	 * @param bf
	 * @param hwDriverChannel
	 */
	public HardwareIO(IBoardFactory bf, IHwDriverChannel hwDriverChannel) {
		boards = new ArrayList<Board>();
		channelMap = new ChannelMap();
		bf.configure(boards, channelMap);

		this.driverChannel = hwDriverChannel;
	}

	@Override
	public void initialize() throws ChannelFault {

		driverChannel.connect();

		StringBuffer sb = new StringBuffer();
		sb.append(GeneralMsg.constructHardwareInit());
		for (Board board : boards) {
			IBoardMessaging bm = (IBoardMessaging) board;
			sb.append(bm.msgInitBoard());
		}
		sb.append('\n');

		List<String> recvdLines;
		recvdLines = driverChannel.sendAndRecv(sb.toString(), Reason.INIT);

		handleRecvdErrorsOnly(recvdLines);
	}

	@Override
	public void refreshInputs() {
		/*
		 * maak REQ commando's en zend ontvang input vals, stuur terug
		 */
		StringBuffer sb = new StringBuffer();
		for (Board board : boards) {
			IBoardMessaging bm = (IBoardMessaging) board;
			sb.append(bm.msgInputRequest());
		}
		sb.append('\n');

		List<String> recvdLines;
		try {
			recvdLines = driverChannel.sendAndRecv(sb.toString(), Reason.INPUT);
		} catch (ChannelFault e) {
			log.error("Error communicating with driver, ignored. Some input changes may be lost. Detail:"+e.getMessage());
			return;
		}

		for (String line : recvdLines) {
			try {
				StringTokenizer st = new StringTokenizer(line);
				String cmd = st.nextToken();
				if (cmd.equals(GeneralMsg.MSGPREFIX_ERROR)) {
					log.error("Error received from Hardware: " + GeneralMsg.parseERROR(st));
				} else {
					Integer address = Integer.parseInt(st.nextToken().substring(2), 16);
					Board b = findBoardBy(address);
					((IBoardMessaging) b).parseInputResult(cmd, address, st);
					errorCount = 0;
				}
			} catch (Exception e) {
				if (errorCount ++ > 5)
					System.exit(1);
				log.warn("Error in line received from Hardware Driver, IGNORED. Line=" + line, e);
			}
		}
	}

	@Override
	public void refreshOutputs() {
		/*
		 * stuur SET commando's geen antwoord, tenzij ERRORs
		 */
		StringBuffer sb = new StringBuffer();
		for (Board board : boards) {
			sb.append(((IBoardMessaging) board).msgOutputRequest());
		}
		sb.append('\n');

		List<String> recvdLines;
		try {
			recvdLines = driverChannel.sendAndRecv(sb.toString(), Reason.OUTPUT);
			errorCount = 0;
		} catch (ChannelFault e) {
			log.error("Error communicating with driver, ignored. Some output changes may be lost.");
			if (errorCount ++ > 5)
				System.exit(1);	// TODO specific exception so outputs can be saved and then exit...
			return;
		}

		handleRecvdErrorsOnly(recvdLines);
	}

	@Override
	public void stop() {
		/*
		 * Stuur STOP commando Ontvangst is leeg, tenzij ERRORs
		 */
		String lines2Send = GeneralMsg.constructQUIT() + '\n';
		try {
			List<String> recvdLines = driverChannel.sendAndRecv(lines2Send, Reason.STOP);
			handleRecvdErrorsOnly(recvdLines);
		} catch (ChannelFault e) {
			log.warn("STOP command to driver gives error. Will try to properly close TCP connection. Message: " + e.getMessage());
		}

		driverChannel.disconnect();
	}

	private void handleRecvdErrorsOnly(List<String> recvdLines) {
		for (String line : recvdLines) {
			StringTokenizer st = new StringTokenizer(line);
			String cmd = st.nextToken();
			if (cmd.equals(GeneralMsg.MSGPREFIX_ERROR)) {
				log.error("Error received from Hardware: " + GeneralMsg.parseERROR(st));
			} else {
				log.warn("Unexpected message received from Hardware Driver. IGNORED. Detail:" + line);
			}
		}
	}

	@Override
	public boolean readDigitalInput(String lc) {
		FysCh fc = channelMap.fysCh(lc);
		log.debug("readDigitalInput(), for LogCh=" + lc + ", got FysCh=" + fc);
		if (fc == null)
			return false; // TODO correct oplossen...
		Board b = boards.get(fc.getBoardNr());
		return b.readDigitalInput((byte) fc.getBoardChannelNr());
	}

	@Override
	public void writeDigitalOutput(String lc, boolean val) {
		FysCh fc = channelMap.fysCh(lc);
		if (fc == null)
			return; // TODO correct oplossen...
		Board b = boards.get(fc.getBoardNr());
		b.writeDigitalOutput((byte) fc.getBoardChannelNr(), val);
	}

	@Override
	public int readAnalogInput(String lc) {
		FysCh fc = channelMap.fysCh(lc);
		if (fc == null)
			return 0; // TODO correct oplossen...
		Board b = boards.get(fc.getBoardNr());
		return b.readAnalogInput((byte) fc.getBoardChannelNr());
	}

	@Override
	public void writeAnalogOutput(String lc, int value) {
		FysCh fc = channelMap.fysCh(lc);
		if (fc == null)
			return; // TODO correct oplossen...
		Board b = boards.get(fc.getBoardNr());
		b.writeAnalogOutput((byte) fc.getBoardChannelNr(), value);
	}

	/**
	 * Finds board with address that is <= given address, and that is closest to
	 * given address.
	 * 
	 * @param address
	 *            to look for
	 * @return board found, or <code>null</code>
	 */
	public Board findBoardBy(int address) {
		int lowdistance = Integer.MAX_VALUE;
		Board lowboard = null;
		for (Board b : boards) {
			if (b.getAddress() == address)
				return b;
			if (b.getAddress() < address) {
				if ((address - b.getAddress()) < lowdistance) {
					lowdistance = address - b.getAddress();
					lowboard = b;
				}
			}
		}
		return lowboard;
	}

	public List<Board> getBoards() {
		return boards;
	}

	public ChannelMap getChannelMap() {
		return channelMap;
	}

	public IHwDriverChannel getDriverChannel() {
		return driverChannel;
	}
}
