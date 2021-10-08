package eu.dlvm.iohardware;

/**
 * Methods to set up a specific hardware.
 * <p> </p>
 */
public interface IHardwareBuilder {
    /**
     * Create a hardware specific IHardwareIO. Note that this is just a start, to really be hardware independent the paraneters should be passed as a key-value list, and domotic's main should accept these as an encoded script or so. And the DiamondSys stuff should be in a jar loaded at startup.
     */
    IHardwareIO build(String cfgFile, String host, int port, int readTimeout, boolean simulated);
}
