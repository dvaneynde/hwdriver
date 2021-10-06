package eu.dlvm.iohardware.diamondsys.factories;

import java.util.List;

import eu.dlvm.iohardware.diamondsys.Board;
import eu.dlvm.iohardware.diamondsys.ChannelMap;

/**
 * Factory that will create the boards and a
 * mapping between logical and physical channels.
 * 
 * @author dirk
 * 
 */
public interface IBoardFactory {
    /**
     * 
     * @param boards
     *            Output parameter, boards will be set. Must not be <code>null</code>, but empty list.
     * @param map
     *            Output parameter, mappings will be set. Must not be <code>null</code>, but empty list.
     */
    void configure(List<Board> boards, ChannelMap map);
}
