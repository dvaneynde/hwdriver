package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent to Hardware Driver, to set one or more analog output values for
 * a given address (=board).
 * <p>
 * <code>SET_AO address:int (channel:byte value:int)+\n</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address of physical board, in hex format with '0x' prefix.
 * <dt>channel
 * <dd>output channel on that board, typically in range [0..7] or [0..15]
 * <dt>value
 * <dd>analog value to set. Note that Java has 4 bytes here, sufficient for all
 * needs.
 * </dl>
 * <p>
 * Note that the board type does not have to be sent, this should already have
 * been done via {@link Msg2InitBoard} and must be retained by the Hardware
 * Driver.
 * 
 * @author dirk vaneynde
 * @deprecated
 */
public class Msg2OutputAnalog extends Msg2Hw {

	private int address;
	private List<ChannelValue> cvs;

	/**
	 * Creates on message line, with no parameters. Use
	 * {@link #add(ChannelValue)} to add parameters.
	 * 
	 * @param address
	 *            Address of board.
	 */
	public Msg2OutputAnalog(int address) {
		this.address = address;
		cvs = new ArrayList<ChannelValue>();
	}

	/**
	 * Creates one message line.
	 * 
	 * @param address
	 *            Address of board.
	 * @param cvs
	 *            List of channel/value combinations to set the output.
	 */
	public Msg2OutputAnalog(int address, ChannelValue[] cvs) {
		this.address = address;
		this.cvs = new ArrayList<ChannelValue>(cvs.length);
		for (ChannelValue cv : cvs) {
			this.cvs.add(cv);
		}
	}

	/**
	 * @param cv
	 *            Channel/value pair to add.
	 */
	public void add(ChannelValue cv) {
		cvs.add(cv);
	}

	public List<ChannelValue> getChannelValues() {
		return cvs;
	}

	@Override
	public String convert4Wire() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("SET_AO 0x%x", address));
		for (ChannelValue cv : cvs) {
			sb.append(' ').append(cv.channel).append(' ').append(cv.value);
		}
		sb.append('\n');
		return sb.toString();
	}

}
