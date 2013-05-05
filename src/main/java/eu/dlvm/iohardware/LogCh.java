/**
 * 
 */
package eu.dlvm.iohardware;

/**
 * A Logical Channel address, for analog or digital inputs or outputs.
 * <p>
 * Does not add any real value, except that the difference with real channels is
 * typed to compiler.
 * 
 * @author dirk
 */
public class LogCh {
	/**
	 * Possible numbers are in [0..MAX]
	 */
	public static final int MAX = 255;
	
	private int nbr;

	public LogCh(int nbr) {
		if (nbr<0 || nbr>MAX) throw new IllegalArgumentException("Number out of possible range [0..LogCh.MAX].");
		this.nbr = nbr;
	}

	public int nr() {
		return nbr;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof LogCh))
			return false;
		LogCh olc = (LogCh) o;
		return (nbr == olc.nbr);
	}

	@Override
	public int hashCode() {
		return nbr;
	}

	@Override
	public String toString() {
		return "LogCh nr="+nbr;
	}
}
