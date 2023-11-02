package nl.duflex.proxy.mqtt;

import nl.duflex.proxy.ProtocolClientHandlerBuilder;
import nl.duflex.proxy.ProxyProtocolClientHandler;

public class MqttProtocolClientHandlerBuilder extends ProtocolClientHandlerBuilder {
    @Override
    public ProxyProtocolClientHandler buildImpl() {
        return new MqttProtocolClientHandler(this.client);
    }
}
