package nl.duflex.proxy;

import nl.duflex.proxy.http.HttpRestApiClientHandlerBuilder;
import nl.duflex.proxy.modbus.ModbusMasterProtocolClientHandlerBuilder;
import nl.duflex.proxy.modbus.ModbusSlaveProtocolClientHandlerBuilder;
import nl.duflex.proxy.mqtt.MqttProtocolClientHandlerBuilder;

public class ProtocolClientHandlerBuilderFactory {
    public static ProtocolClientHandlerBuilder forProtocol(final ProxyProtocol protocol) {
        return switch (protocol) {
            case Mqtt -> new MqttProtocolClientHandlerBuilder();
            case ModbusSlave -> new ModbusSlaveProtocolClientHandlerBuilder();
            case ModbusMaster -> new ModbusMasterProtocolClientHandlerBuilder();
            case HttpRestApi -> new HttpRestApiClientHandlerBuilder();
        };
    }
}
