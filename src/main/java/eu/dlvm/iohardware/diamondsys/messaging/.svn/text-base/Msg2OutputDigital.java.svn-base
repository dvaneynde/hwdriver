package eu.dlvm.iohardware.diamondsys.messaging;


/**
 * Message sent to Hardware Driver, to set digital output values for a given address (=board).
 * <p>
 * <code>SET_DO address:int type:char value:int\n</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address of physical board, in hex format with '0x' prefix.
 * <dt>type
 * <dd>type of DiamonSys board, e.g. 'O' for Opalmm
 * <dt>value
 * <dd>digital value to set; typically a byte, it sets 8 digital output
 * channels.
 * </dl>
 * <p>
 * Currently we do not support multiple channels per address, where each channel
 * (or 'port' in Diamond systems speak) would have 8 digital outputs. There is
 * no need - yet.
 * 
 * @author dirk vaneynde
 * @deprecated
 */
public class Msg2OutputDigital extends Msg2Hw {
	private int address;
	private char type;
	private int value;

	public Msg2OutputDigital(int address, char type, int value) {
		this.address = address;
		this.type = type;
		this.value = value;
	}

	@Override
	public String convert4Wire() {
		return String.format("SET_DO 0x%x %c %d\n", address, type, value);
	}

}
