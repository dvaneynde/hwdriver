package eu.dlvm.iohardware;

public enum ChannelType {
	DigiIn(0), DigiOut(1), AnlgIn(2), AnlgOut(3);

	private int nr;

	ChannelType(int nr) {
		this.nr = nr;
	}
	
	public int asNumber() {
		return nr;
	}
}
