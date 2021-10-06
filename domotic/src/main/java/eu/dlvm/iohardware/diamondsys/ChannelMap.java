package eu.dlvm.iohardware.diamondsys;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapping of Logical Channel ID (String) to Physical Channel, and vice versa.
 * 
 * @author dirk
 * 
 */
public class ChannelMap {

    static Logger log = LoggerFactory.getLogger(ChannelMap.class);

    Map<String, FysCh> map = new HashMap<String, FysCh>();

    public FysCh fysCh(String l) {
        return map.get(l);
    }

    public void add(String l, FysCh f) {
        if (map.containsKey(l))
            log.warn("ChannelMap already has an entry for " + l + ". Will be overwritten.");
        map.put(l, f);
    }

}
