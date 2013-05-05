package eu.dlvm.iohardware.diamondsys;

public class FysCh {

	private int boardNr;
	private ChannelType chType;
	private int boardChannelNr;

	public FysCh(int boardNr, ChannelType type, int boardChannelNr) {
		this.boardNr = boardNr;
		this.chType = type;
		this.boardChannelNr = boardChannelNr;
	}

	/**
	 * @return Physical board's number.
	 */
	public int getBoardNr() {
		return boardNr;
	}

	/**
	 * @return Channel number within a physical board.
	 */
	public int getBoardChannelNr() {
		return boardChannelNr;
	}

	/**
	 * @return the chType
	 */
	public ChannelType getChannelType() {
		return chType;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof FysCh))
			return false;
		FysCh o2 = (FysCh) o;
		return ((chType == o2.chType) && (boardNr == o2.boardNr) && (boardChannelNr == o2.boardChannelNr));
	}

	@Override
	public int hashCode() {
		return boardNr * 8 + boardChannelNr + chType.asNumber() * 100;
	}

	@Override
	public String toString() {
		return "FysCh boardNr=" + boardNr + ", channel=" + chType
				+ ", channel on board=" + boardChannelNr;
	}
}
