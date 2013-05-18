package eu.dlvm.iohardware.diamondsys.messaging;

import java.text.ParseException;
import java.util.StringTokenizer;

class Utils {

    /**
     * @param line
     * @param b
     * @return address
     * @throws ParseException
     */
    static int parseLine(String line, IBoardMessaging b) throws ParseException {
        StringTokenizer st = new StringTokenizer(line);
        String sCmd = st.nextToken();
        String sAddress = st.nextToken().substring(2);
        int address = Integer.parseInt(sAddress, 16);
        b.parseInputResult(sCmd, address, st);
        return address;
    }

}
