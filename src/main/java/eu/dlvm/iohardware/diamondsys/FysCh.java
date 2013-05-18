package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.ChannelType;

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
	public String toString() {
		return "FysCh boardNr=" + boardNr + ", channel=" + chType
				+ ", channel on board=" + boardChannelNr;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + boardChannelNr;
        result = prime * result + boardNr;
        result = prime * result + ((chType == null) ? 0 : chType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FysCh other = (FysCh) obj;
        if (boardChannelNr != other.boardChannelNr)
            return false;
        if (boardNr != other.boardNr)
            return false;
        if (chType != other.chType)
            return false;
        return true;
    }
}
