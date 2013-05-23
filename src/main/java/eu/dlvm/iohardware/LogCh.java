/**
 * 
 */
package eu.dlvm.iohardware;

/**
 * A Logical Channel address, for analog or digital inputs or outputs.
 * <p>
 * The {{@link #nr()} property must be <i>at least</i> unique per {@link ChannelType}. Specific hardware implementations may have stricter requirements though.
 * 
 * @author dirk
 */
public class LogCh {
    /**
     * Possible numbers are in [0..MAX]
     */
    public static final int MAX = 256;

    private int nbr;

    public LogCh(int nbr) {
        if (nbr < 0 || nbr > MAX)
            throw new IllegalArgumentException("Number out of possible range [0.." + LogCh.MAX + "].");
        this.nbr = nbr;
    }

    public int nr() {
        return nbr;
    }

    @Override
    public String toString() {
        return "LogCh nr=" + nbr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nbr;
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
        LogCh other = (LogCh) obj;
        if (nbr != other.nbr)
            return false;
        return true;
    }
}
