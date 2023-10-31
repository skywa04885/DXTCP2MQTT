package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteSingleRegisterResponseMessage extends ResponseMessage<WriteSingleRegisterResponse> {
    static class Builder extends ResponseMessage.Builder<WriteSingleRegisterResponseMessage> {
        private Integer address = null;
        private Integer value = null;

        WriteSingleRegisterResponseMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new WriteSingleRegisterResponseMessage(this.address, this.value);
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
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
    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int value;

    WriteSingleRegisterResponseMessage(final int address, final int value) {
        this.address = address;
        this.value = value;
    }

    com.digitalpetri.modbus.responses.WriteSingleRegisterResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.WriteSingleRegisterResponse(this.address, this.value);
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.value).flush();
    }
}
