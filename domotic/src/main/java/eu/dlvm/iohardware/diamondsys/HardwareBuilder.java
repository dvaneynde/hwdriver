package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.IHardwareBuilder;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverChannelSimulator;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;
import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel;

public class HardwareBuilder implements IHardwareBuilder {

    @Override
    public IHardwareIO build(String cfgFile, String host, int port, int readTimeout, boolean simulated) {
        XmlHwConfigurator xhc = new XmlHwConfigurator(cfgFile);
        IHwDriverChannel hdc;
        if (simulated)
            hdc = new HwDriverChannelSimulator();
        else
            hdc = new HwDriverTcpChannel(host, port, readTimeout);
        HardwareIO hw = new HardwareIO(xhc, hdc);
        return hw;
    }
}
