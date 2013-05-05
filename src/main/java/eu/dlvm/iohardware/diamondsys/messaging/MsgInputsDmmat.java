package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Digital and Analog input measurements received from Hardware Driver, from one
 * DMMAT board.
 * <p>
 * <code>INP_D address:int valD:int valA1:int valA2:int</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address in hex format with '0x' prefix.
 * <dt>valD
 * <dd>digital input value; if digital input not used then contains '-'
 * <dt>valA1, valA2
 * <dd>analog sample value, in decimal format, of first and second analog input
 * channel respectively; if analog input not used then contains '-'
 * </dl>
 * <p>
 * For example:<br>
 * <code>INP_D 0x300 6 - 240</code><br>
 * means that board at address 0x300 digital input channels 1 and 2 are on
 * (hence 6), analog channel 0 was not requested and analog channel1 measures
 * 240.
 * 
 * @author dirk vaneynde
 * 
 */
public class MsgInputsDmmat extends MsgFromHw {

	public static final String NAME = "INP_D";
	private int address;
	private int digitalInValue;
	private int[] analogInValues = new int[2];

	public MsgInputsDmmat() {
		super();
		digitalInValue = -1;
		for (int i = 0; i < analogInValues.length; i++)
			analogInValues[i] = -1;
	}

	/**
	 * Only exists for testing purposes at current.
	 */
	public MsgInputsDmmat(int address, int digiVal, int anaVal1, int anaVal2) {
		this.address = address;
		this.digitalInValue = digiVal;
		this.analogInValues[0] = anaVal1;
		this.analogInValues[1] = anaVal2;
	}

	/**
	 * @return the digitalInValue, or <code>-1</code> if this channel is not
	 *         used.
	 */
	public int getDigitalInValue() {
		return digitalInValue;
	}

	/**
	 * 
	 * @param ch Channel 0 or 1.
	 * @return the analogInValue1, or <code>-1</code> if this channel is not
	 *         used.
	 */
	public int getAnalogInValue(int ch) {
		return analogInValues[ch];
	}

	/**
	 * @return address of I/O board, also identifies the board.
	 */
	public int getAddress() {
		return address;
	}

	@Override
	public String asWireString() {
		StringBuffer sb = new StringBuffer();
		sb.append(NAME).append(" 0x").append(Integer.toString(address, 16));
		sb.append(' ').append(digitalInValue == -1 ? "-" : digitalInValue);
		for (int i = 0; i < analogInValues.length; i++) {
			sb.append(' ').append(
					analogInValues[i] == -1 ? "-" : analogInValues[i]);
		}
		sb.append('\n');
		return sb.toString();
	}

	@Override
	protected void parse(StringTokenizer st) throws ParseException {
		String s = st.nextToken();
		this.address = Integer.parseInt(s.substring(2), 16);
		s = st.nextToken();
		if (!s.equals("-"))
			this.digitalInValue = Integer.parseInt(s);
		for (int i = 0; i < analogInValues.length; i++) {
			s = st.nextToken();
			if (!s.equals("-"))
				this.analogInValues[i] = Integer.parseInt(s);
		}
	}

	@Override
	public String toString() {
		return "MsgInputsDmmat: wire format='"+asWireString()+"'";
	}
}
