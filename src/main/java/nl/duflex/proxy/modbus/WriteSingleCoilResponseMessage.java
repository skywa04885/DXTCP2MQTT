package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.WriteSingleCoilResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteSingleCoilResponseMessage extends ResponseMessage<WriteSingleCoilResponse> {
    static class Builder extends ResponseMessage.Builder<WriteSingleCoilResponseMessage> {
        private Integer address = null;
        private Boolean value = null;


        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            int address;
            boolean value;

            // Reads the response line.
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            // Splits the line into the segments.
            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Invalid response length");

            // Parses the address.
            try {
                address = Integer.parseInt(lineSegments[0]);
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid address given");
            }

            // Parses the value.
            try {
                value = Integer.parseInt(lineSegments[1]) > 0;
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid value given");
            }

            // Sets the address and the value.
            this.address = address;
            this.value = value;

            // Returns the current instance.
            return this;
        }

        @Override
        WriteSingleCoilResponseMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new WriteSingleCoilResponseMessage(this.address, this.value);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final boolean value;

    WriteSingleCoilResponseMessage(final int address, final boolean value) {
        this.address = address;
        this.value = value;
    }

    com.digitalpetri.modbus.responses.WriteSingleCoilResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.WriteSingleCoilResponse(
                this.address, this.value ? 0xFF00 : 0x0000);
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + (this.value ? "1" : "0")).flush();
    }
}
