package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyInputStreamReader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ModbusOptions {
    public static class Builder {
        private InetAddress address = null;
        private Short port = null;

        public Builder setAddress(final InetAddress address) {
            this.address = address;
            return this;
        }

        public Builder setPort(final Short port) {
            this.port = port;
            return this;
        }

        public ModbusOptions build() {
            assert this.address != null;
            assert this.port != null;

            return new ModbusOptions(this.address, this.port);
        }

        public Builder readFromInputStreamReader(final ProxyInputStreamReader reader) throws IOException {
            final String modbusOptionsLine = reader.readStringUntilNewLine();
            if (modbusOptionsLine == null) return null;
            return this.parse(modbusOptionsLine);
        }

        public Builder parse(final String rawString) throws RuntimeException {
            // Splits the raw string into segments.
            final String[] rawStringSegments = rawString.split(" ");
            if (rawStringSegments.length != 2) throw new RuntimeException("Two segments must be in the options");

            // Parses the address.
            try {
                this.setAddress(InetAddress.getByName(rawStringSegments[0]));
            } catch (final UnknownHostException exception) {
                throw new RuntimeException("Invalid address given");
            }

            // Parses the port.
            try {
                this.setPort(Short.parseShort(rawStringSegments[1]));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid port given");
            }

            // Returns the builder.
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final InetAddress address;
    private final short port;

    public ModbusOptions(final InetAddress address, final short port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public short getPort() {
        return this.port;
    }
}
