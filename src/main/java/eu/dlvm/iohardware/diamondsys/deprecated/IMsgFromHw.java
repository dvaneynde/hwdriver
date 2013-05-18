package eu.dlvm.iohardware.diamondsys.deprecated;

import java.text.ParseException;
import java.util.StringTokenizer;

@Deprecated
public interface IMsgFromHw {

    /**
     * @return Message as it appears on the wire.
     */
    public abstract String asWireString();

    /**
     * Takes a tokenizer on the received message, which already processed the
     * initial MSG_ID, and parses the rest if any, and sets message specific
     * properties.
     * 
     * @param st
     *            tokenizer on wire string.
     * @throws ParseException
     *             if wire string has error; error positions is not always set.
     */
    public abstract void parse(StringTokenizer st) throws ParseException;

}