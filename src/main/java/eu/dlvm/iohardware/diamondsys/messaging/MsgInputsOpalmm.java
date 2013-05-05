package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Digital input measurements received from Hardware Driver, from one OPALMM board.
 * <p>
 * <code>INP_O address:int val:int\n</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address in hex format with '0x' prefix. 
 * <dt>val
 * <dd>really a byte, each bit is the state on or off of one channel.
 * </dl>
 * 
 * @author dirk vaneynde
 * 
 */
public class MsgInputsOpalmm extends MsgFromHw {

	public static final String NAME = "INP_O";
	private int address;
	private int value;

	public MsgInputsOpalmm() {
		super();
	}

	/**
	 * Only exists for testing purposes at current.
	 */
	public MsgInputsOpalmm(int address, int value) {
		this.address = address;
		this.value = value;
	}

	/**
	 * @return address of I/O board, also identifies the board.
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * Each digital channel is represented by a bit in a byte. (Could also be a
	 * word etc., Java ints are 4 bytes so we cover what is typically used). A
	 * byte being 8 bits represents 8 channels. If value is '1' then only
	 * channel 0 is on. If value is 5, channels 0 and 2 are on.
	 * 
	 * @return encoded channel states, on or off.
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String asWireString() {
		StringBuffer sb = new StringBuffer();
		sb.append(NAME).append(" 0x").append(Integer.toString(address, 16)).append(' ').append(value).append('\n');
		return sb.toString();
	}

	@Override
	protected void parse(StringTokenizer st) throws ParseException {
		String s = st.nextToken();
		this.address = Integer.parseInt(s.substring(2), 16);
		s = st.nextToken();
		value = Integer.parseInt(s);
	}
}
