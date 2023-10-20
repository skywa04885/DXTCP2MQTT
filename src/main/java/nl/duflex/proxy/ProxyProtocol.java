package nl.duflex.proxy;

public enum ProxyProtocol {
    MQTT("MQTT"),
    MODBUS_SLAVE("MODBUS_SLAVE");

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
