package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;
import java.io.InputStreamReader;

public class ModbusProxyWriteSingleRegisterResponseMessage extends ModbusProxyResponseMessage {
    public static class Builder extends ModbusProxyResponseMessage.Builder {
        private Integer address = null;
        private Integer value = null;

        /**
         * Sets the address.
         * @param address the value to set.
         * @return the current builder instance.
         */
        public Builder setAddress(final Integer address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the value.
         * @param value the value to set.
         * @return the current builder instance.
         */
        public Builder setValue(final Integer value) {
            this.value = value;
            return this;
        }

        /**
         * Builds a single register response message.
         * @return the built message.
         */
        public ModbusProxyWriteSingleRegisterResponseMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new ModbusProxyWriteSingleRegisterResponseMessage(this.address, this.value);
        }

        /**
         * Reads the address and the value from the given input stream.
         * @param inputStreamReader the input stream to read from.
         * @return the current builder instance.
         * @throws IOException gets thrown when the reading of the line fails.
         * @throws RuntimeException gets thrown when the address or value is invalid.
         */
        @Override
        public ModbusProxyResponseMessage.Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            int address;
            int value;

            // Reads the line and null if the end of the stream is reached.
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            // Splits the line into multiple segments
            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Invalid number of line segments");

            // Parses the address.
            try {
                address = Integer.parseInt(lineSegments[0]);
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid address given");
            }

            // Parses the value.
            try {
                value = Integer.parseInt(lineSegments[1]);
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid value given");
            }

            // Stores the address and the value (since no exceptions occurred).
            this.address = address;
            this.value = value;

            // Returns the current builder instance.
            return this;
        }
    }

    /**
     * Creates a new builder instance.
     * @return the newly created builder instance.
     */
    public static ModbusProxyWriteSingleRegisterResponseMessage.Builder newBuilder() {
        return new ModbusProxyWriteSingleRegisterResponseMessage.Builder();
    }

    private final int address;
    private final int value;

    public ModbusProxyWriteSingleRegisterResponseMessage(final int address, final int value) {
        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return this.address;
    }

    public int getValue() {
        return this.value;
    }

    public WriteSingleRegisterResponse toResponse() {
        return new WriteSingleRegisterResponse(this.address, this.value);
    }

    public void writeToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.value).flush();
    }
}
