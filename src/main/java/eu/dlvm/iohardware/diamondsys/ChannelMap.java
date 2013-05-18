package eu.dlvm.iohardware.diamondsys;

import org.apache.log4j.Logger;

import eu.dlvm.iohardware.ChannelType;
import eu.dlvm.iohardware.LogCh;

/**
 * Mapping of FysCh to LogCh, and vice versa.
 * @author dirk
 *
 */
public class ChannelMap {

	static Logger log = Logger.getLogger(ChannelMap.class);

	private FysCh[] fs = new FysCh[LogCh.MAX];
	
	public FysCh fysCh(LogCh l) {
		return fs[l.nr()];
	}
	
	public LogCh logCh(FysCh f) {
		for (int i=0; i<fs.length; i++) {
			if (fs[i].equals(f))
				return new LogCh(i);
		}
		return null;
	}
	
	public LogCh logCh(int boardNr, ChannelType type, int boardChannelNr) {
		FysCh f = new FysCh(boardNr, type, boardChannelNr);
		return logCh(f);
	}
	
	public void add(LogCh l, FysCh f) {
		if (fs[l.nr()] != null)
			log.warn("ChannelMap already has an entry for "+l+". Will be overwritten.");
		fs[l.nr()] = f;
	}
	
}
