package eu.dlvm.iohardware.diamondsys.deprecated;

@Deprecated
public interface IMsg2Hw {

    /**
     * @return Message string as it appears on the wire.
     */
    public abstract String convert4Wire();


}