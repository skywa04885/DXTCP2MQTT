package nl.duflex.proxy;

public enum ProxyProtocol {
    Mqtt("MQTT"),
    ModbusSlave("MODBUS_SLAVE"),
    ModbusMaster("MODBUS_MASTER");

    public final String label;

    ProxyProtocol(final String label) {
        this.label = label;
    }

    public static ProxyProtocol fromLabel(final String label) throws RuntimeException {
        for (final ProxyProtocol proxyProtocol : values()) {
            if (!proxyProtocol.label.equals(label)) continue;
            return proxyProtocol;
        }

        throw new RuntimeException("Unrecognized protocol label: " + label);
    }
}
