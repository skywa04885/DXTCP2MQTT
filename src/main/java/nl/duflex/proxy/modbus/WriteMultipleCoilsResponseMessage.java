package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.WriteMultipleCoilsResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteMultipleCoilsResponseMessage extends ResponseMessage<WriteMultipleCoilsResponse> {
    static class Builder extends ResponseMessage.Builder<WriteMultipleCoilsResponseMessage> {
        private Integer address = null;
        private Integer quantity = null;

        @Override
        Builder readFromInputStreamReader(ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            int address;
            int quantity;

            // Reads the line.
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            // Splits the line into the segments.
            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Invalid number of segments in line");

            // Parses the address.
            try {
                address = Integer.parseInt(lineSegments[0]);
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid address");
            }

            // Parses the quantity.
            try {
                quantity = Integer.parseInt(lineSegments[1]);
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid quantity");
            }

            // Sets the address and the quantity.
            this.address = address;
            this.quantity = quantity;

            // Returns the current instance.
            return this;
        }

        @Override
        WriteMultipleCoilsResponseMessage build() {
            assert this.address != null;
            assert this.quantity != null;

            return new WriteMultipleCoilsResponseMessage(this.address, this.quantity);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int quantity;

    WriteMultipleCoilsResponseMessage(final int address, final int quantity) {
        this.address = address;
        this.quantity = quantity;
    }

    com.digitalpetri.modbus.responses.WriteMultipleCoilsResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.WriteMultipleCoilsResponse(this.address, this.quantity);
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.quantity).flush();
    }
}
