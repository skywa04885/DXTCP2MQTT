package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProtocolClientHandlerBuilder;
import nl.duflex.proxy.ProxyProtocolClientHandler;

public class ModbusMasterProtocolClientHandlerBuilder extends ProtocolClientHandlerBuilder {
    @Override
    protected ProxyProtocolClientHandler buildImpl() {
        return new ModbusMasterProtocolClientHandler(this.client);
    }
}
