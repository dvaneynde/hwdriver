package eu.dlvm.iohardware.diamondsys;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;
import eu.dlvm.iohardware.diamondsys.factories.IBoardFactory;
import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2Hw;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2HwReqInput;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2InitBoard;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2InitHardware;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2SetOutputs;
import eu.dlvm.iohardware.diamondsys.messaging.Msg2Stop;
import eu.dlvm.iohardware.diamondsys.messaging.MsgError;
import eu.dlvm.iohardware.diamondsys.messaging.MsgFromHw;
import eu.dlvm.iohardware.diamondsys.messaging.MsgInputsDmmat;
import eu.dlvm.iohardware.diamondsys.messaging.MsgInputsOpalmm;
import eu.dlvm.iohardware.diamondsys.messaging.Parser;

/**
 * Diamond-systems specific implementation of {@link IHardwareIO}.
 * 
 * @author dirk vaneynde
 */
public class HardwareIO implements IHardwareIO {

	static Logger log = Logger.getLogger(HardwareIO.class);

	private List<Board> boards;
	private ChannelMap channelMap;
	private IHwDriverChannel driverChannel;

	/**
	 * Create a Diamond Systems hardware configuration.
	 * 
	 * @param bf
	 *            Reference to a configurator that will create the boards and a
	 *            mapping between logical and physical channels.
	 * @param hwDriverHostname
	 * @param hwDriverPort
	 */
	public HardwareIO(IBoardFactory bf, IHwDriverChannel hwDriverChannel) {
		boards = new ArrayList<Board>();
		channelMap = new ChannelMap();
		bf.configure(boards, channelMap);
		this.driverChannel = hwDriverChannel;
	}

	public ChannelMap getChannelMap() {
		return channelMap;
	}

	public void setChannelMap(ChannelMap channelMap) {
		this.channelMap = channelMap;
	}

	@Override
	public void initialize() {

		driverChannel.connect();

		StringBuffer sb = new StringBuffer();
		sb.append(new Msg2InitHardware().convert4Wire());
		for (Board board : boards) {
			Msg2Hw m2h = new Msg2InitBoard(board);
			sb.append(m2h.convert4Wire());
		}
		sb.append('\n');

		List<String> recvdLines;
		recvdLines = driverChannel.sendAndRecv(sb.toString());

		handleRecvdErrorsOnly(recvdLines);
	}

	@Override
	public void refreshInputs() {
		/*
		 maak REQ commando's en zend
		 ontvang input vals, stuur terug
		 */
		StringBuffer sb = new StringBuffer();
		for (Board board : boards) {
			Msg2Hw m = new Msg2HwReqInput().construct(board);
			sb.append(m.convert4Wire());
		}
		sb.append('\n');

		List<String> recvdLines;
		recvdLines = driverChannel.sendAndRecv(sb.toString());

		for (String line : recvdLines) {
			try {
				MsgFromHw msg = Parser.parseFromWire(line);
				if (msg instanceof MsgError) {
					log.error("Error received from Hardware: "
							+ ((MsgError) msg).getDetail());
				} else if (msg instanceof MsgInputsOpalmm) {
					updateInputVals((MsgInputsOpalmm) msg);
				} else if (msg instanceof MsgInputsDmmat) {
					updateInputVals((MsgInputsDmmat) msg);
				} else {
					log.warn("Unexpected message received from Hardware Driver. IGNORED. Detail:"
							+ line);
				}
			} catch (ParseException e) {
				log.warn(
						"Error in line received from Hardware Driver. IGNORED",
						e);
			}
		}
	}

	/*
	 * TODO see "visitor pattern" bug, move to Msg*
	 */
	private void updateInputVals(MsgInputsOpalmm inVals) {
		OpalmmBoard board = (OpalmmBoard) findBoardBy(inVals.getAddress());
		board.digiIn().updateInputFromHardware(inVals.getValue());
	}

	/*
	 * TODO see "visitor pattern" bug, move to Msg*
	 */
	private void updateInputVals(MsgInputsDmmat inVals) {
		DmmatBoard board = (DmmatBoard) findBoardBy(inVals.getAddress());
		if (board.digiIn() != null) {
			board.digiIn().updateInputFromHardware(inVals.getDigitalInValue());
			if (inVals.getDigitalInValue() < 0) {
				log.error("Bug detected: dmmat board digiIn is non-null, yet it seems '-' was received from driver.\nMsgInputsDmmat: "+inVals.toString()+"\nBoard: "+board.toString());
				throw new RuntimeException("Bug detected, check log.");
			}
		}
		for (int i = 0; i < DmmatBoard.ANALOG_IN_CHANNELS; i++) {
			if (board.anaIn(i) != null) {
				board.anaIn(i).updateInputFromHardware(
						inVals.getAnalogInValue(i));
				if (inVals.getAnalogInValue(i) < 0) {
					log.error("Bug detected: dmmat board anaIn["+i+"] is non-null, yet it seems '-' was received from driver.\nMsgInputsDmmat: "+inVals.toString()+"\nBoard: "+board.toString());
					throw new RuntimeException("Bug detected, check log.");
				}
			}
		}
	}

	@Override
	public void refreshOutputs() {
		/*
		 stuur SET commando's
		 geen antwoord, tenzij ERRORs
		 */
		StringBuffer sb = new StringBuffer();
		for (Board board : boards) {
			Msg2Hw m = new Msg2SetOutputs().construct(board);
			sb.append(m.convert4Wire());
		}
		sb.append('\n');

		List<String> recvdLines;
		recvdLines = driverChannel.sendAndRecv(sb.toString());

		handleRecvdErrorsOnly(recvdLines);
	}

	@Override
	public void stop() {
		/*
		 Stuur STOP commando
		 Ontvangst is leeg, tenzij ERRORs
		 */
		Msg2Stop msg = new Msg2Stop();
		String lines2Send = msg.convert4Wire() + '\n';

		List<String> recvdLines = driverChannel.sendAndRecv(lines2Send);

		handleRecvdErrorsOnly(recvdLines);

		driverChannel.disconnect();
	}

	private void handleRecvdErrorsOnly(List<String> recvdLines) {
		for (String line : recvdLines) {
			try {
				MsgFromHw msg = Parser.parseFromWire(line);
				if (msg instanceof MsgError) {
					log.error("Error received from Hardware: "
							+ ((MsgError) msg).getDetail());
				} else {
					log.warn("Unexpected message received from Hardware Driver. IGNORED. Detail:"
							+ msg.asWireString());
				}
			} catch (ParseException e) {
				log.warn(
						"Error in line received from Hardware Driver. IGNORED",
						e);
			}
		}
	}

	@Override
	public boolean readDigitalInput(LogCh lc) {
		FysCh fc = channelMap.fysCh(lc);
		Board b = boards.get(fc.getBoardNr());
		DigiIn d = null;
		if (b instanceof OpalmmBoard) {
			d = ((OpalmmBoard) b).digiIn();
		} else if (b instanceof DmmatBoard) {
			d = ((DmmatBoard) b).digiIn();
		} else
			throw new IllegalArgumentException();
		return d.getInput((byte) fc.getBoardChannelNr());
	}

	@Override
	public void writeDigitalOutput(LogCh lc, boolean val) {
		FysCh fc = channelMap.fysCh(lc);
		Board b = boards.get(fc.getBoardNr());
		DigiOut out = null;
		if (b instanceof OpalmmBoard) {
			out = ((OpalmmBoard) b).digiOut();
		} else if (b instanceof DmmatBoard) {
			out = ((DmmatBoard) b).digiOut();
		} else
			throw new IllegalArgumentException();
		out.setOutputForChannel(val, (byte) fc.getBoardChannelNr());
	}

	@Override
	public int readAnalogInput(LogCh lc) {
		FysCh fc = channelMap.fysCh(lc);
		DmmatBoard db = (DmmatBoard) boards.get(fc.getBoardNr());
		return db.anaIn(fc.getBoardChannelNr()).getInput();
	}

	@Override
	public void writeAnalogOutput(LogCh lc, int value) {
		FysCh fc = channelMap.fysCh(lc);
		;
		DmmatBoard b = (DmmatBoard) boards.get(fc.getBoardNr());
		b.anaOut((byte) fc.getBoardChannelNr()).setOutput(value);
	}

	private Board findBoardBy(int address) {
		for (Board b : boards) {
			if (b.getAddress() == address)
				return b;
		}
		throw new IllegalArgumentException("No dio board found with port="
				+ address);
	}
}
