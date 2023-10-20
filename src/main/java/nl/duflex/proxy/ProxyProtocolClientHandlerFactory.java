package nl.duflex.proxy;

import nl.duflex.proxy.mqtt.MqttProtocolClientHandler;
import nl.duflex.proxy.modbus.ModbusSlaveProtocolClientHandler;

public class ProxyProtocolClientHandlerFactory {
    public static ProxyProtocolClientHandler create(final ProxyProtocol proxyProtocol, final ProxyTcpClient client) {
        switch (proxyProtocol) {
            case MQTT -> {
                return new MqttProtocolClientHandler(client);
            }
            case MODBUS_SLAVE -> {
                return new ModbusSlaveProtocolClientHandler(client);
            }
            default -> throw new Error("Invalid protocol given");
        }
    }
}
