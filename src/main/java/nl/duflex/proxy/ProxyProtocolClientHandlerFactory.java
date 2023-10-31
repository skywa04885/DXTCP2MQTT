package nl.duflex.proxy;

import nl.duflex.proxy.modbus.ModbusMasterProtocolClientHandler;
import nl.duflex.proxy.mqtt.MqttProtocolClientHandler;
import nl.duflex.proxy.modbus.ModbusSlaveProtocolClientHandler;

public class ProxyProtocolClientHandlerFactory {
    public static ProxyProtocolClientHandler create(final ProxyProtocol proxyProtocol, final ProxyTcpClient client) {
        return switch (proxyProtocol) {
            case Mqtt -> new MqttProtocolClientHandler(client);
            case ModbusSlave -> new ModbusSlaveProtocolClientHandler(client);
            case ModbusMaster -> new ModbusMasterProtocolClientHandler(client);
        };
    }
}
