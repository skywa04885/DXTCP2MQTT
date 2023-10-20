package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyInputStreamReader;

import java.io.IOException;
import java.io.InputStreamReader;

public abstract class ModbusProxyResponseMessage {
    public static abstract class Builder {
        public abstract Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException;

        public abstract ModbusProxyResponseMessage build();
    }
}
