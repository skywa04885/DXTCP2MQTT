package nl.duflex.proxy;

import java.io.IOException;

public enum ProxyProtocol {
    Mqtt("MQTT"),
    ModbusSlave("MODBUS_SLAVE"),
    ModbusMaster("MODBUS_MASTER"),
    HttpRestApi("HTTP_REST_API");

    public final String label;

    ProxyProtocol(final String label) {
        this.label = label;
    }

    public static ProxyProtocol deserialize(String serialized) throws RuntimeException {
        serialized = serialized.trim().toUpperCase();

        for (final ProxyProtocol proxyProtocol : values())
            if (proxyProtocol.label.equals(serialized)) return proxyProtocol;

        throw new RuntimeException("Unrecognized protocol label: " + serialized);
    }

    public static ProxyProtocol readFromProxyInputStreamReader(final ProxyInputStreamReader proxyInputStreamReader)
            throws IOException {
        final var line = proxyInputStreamReader.readStringUntilNewLine();
        if (line == null) return null;

        return deserialize(line);
    }
}
