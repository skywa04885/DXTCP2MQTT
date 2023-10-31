package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteMultipleRegistersResponseMessage extends ResponseMessage<WriteMultipleRegistersResponse> {
    static class Builder extends ResponseMessage.Builder<WriteMultipleRegistersResponseMessage> {
        private Integer address = null;
        private Integer quantity = null;

        @Override
        Builder readFromInputStreamReader(ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Unexpected segment amount for write multiple " +
                    "registers response");

            final String addressSegment = lineSegments[0];
            final String quantitySegment = lineSegments[1];

            int address;
            int quantity;

            try {
                address = Integer.parseInt(addressSegment);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Got invalid address");
            }

            try {
                quantity = Integer.parseInt(quantitySegment);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Got invalid quantity");
            }

            this.address = address;
            this.quantity = quantity;

            return this;
        }

        @Override
        WriteMultipleRegistersResponseMessage build() {
            return new WriteMultipleRegistersResponseMessage(this.address, this.quantity);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int quantity;

    WriteMultipleRegistersResponseMessage(final int address, final int quantity) {
        this.address = address;
        this.quantity = quantity;
    }

    com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse(this.address, this.quantity);
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.quantity).flush();
    }
}
