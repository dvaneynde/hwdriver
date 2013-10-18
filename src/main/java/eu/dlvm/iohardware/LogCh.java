/**
 * 
 */
package eu.dlvm.iohardware;

/**
 * A Logical Channel address, for analog or digital inputs or outputs.
 * <p>
 * The {{@link #id()} property must be <i>at least</i> unique per {@link ChannelType}. Specific hardware implementations may have stricter requirements though.
 * 
 * @author dirk
 */
public class LogCh {
    private String id;

    /**
     * Preferred constructor. 
     * @param id Unique id.
     */
    public LogCh(String id) {
        this.id = id;
    }

    /**
     * Alternative constructor. See {{@link #LogCh(String)} for the preferred one.
     * @param nbr Unique number.
     */
    public LogCh(int nbr) {
        id = Integer.toString(nbr);
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return "LogCh id=" + id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
