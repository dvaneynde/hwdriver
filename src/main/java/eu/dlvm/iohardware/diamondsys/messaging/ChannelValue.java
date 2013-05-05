package eu.dlvm.iohardware.diamondsys.messaging;

public class ChannelValue {
	public byte channel;
	public int value;

	public ChannelValue(byte c, int v) {
		channel = c;
		value = v;
	}
}
