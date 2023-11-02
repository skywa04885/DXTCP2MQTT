package nl.duflex.proxy;

import nl.duflex.proxy.modbus.ModbusMasterProtocolClientHandler;
import nl.duflex.proxy.mqtt.MqttProtocolClientHandler;
import nl.duflex.proxy.modbus.ModbusSlaveProtocolClientHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyProtocolClientHandlerFactory {
    private final Map<ProxyProtocol, ProtocolClientHandlerBuilder> protocolClientHandlerBuilderMap;

    public ProxyProtocolClientHandlerFactory(final Map<ProxyProtocol, ProtocolClientHandlerBuilder> protocolClientHandlerBuilderMap) {
        this.protocolClientHandlerBuilderMap = protocolClientHandlerBuilderMap;
    }

    public ProxyProtocolClientHandler createForProtocol(final ProxyProtocol proxyProtocol, final ProxyTcpClient client) {
        final var builder = protocolClientHandlerBuilderMap.get(proxyProtocol);
        if (builder == null) return null;
        return builder.setClient(client).build();
    }
}
