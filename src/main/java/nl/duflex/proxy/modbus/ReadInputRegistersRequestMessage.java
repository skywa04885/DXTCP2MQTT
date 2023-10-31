package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class ReadInputRegistersRequestMessage
        extends RequestMessage<ReadInputRegistersRequest> {
    static class Builder
            extends RequestMessage.Builder<com.digitalpetri.modbus.requests.ReadInputRegistersRequest,
            ReadInputRegistersRequestMessage> {
        private Integer address = null;
        private Integer quantity = null;

        @Override
        Builder copyFromModbusRequest(final com.digitalpetri.modbus.requests.ReadInputRegistersRequest request) {
            this.address = request.getAddress();
            this.quantity = request.getQuantity();
            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Invalid number of segments in read input registers request line");

            final String addressLineSegment = lineSegments[0];
            final String quantityLineSegment = lineSegments[1];

            int address;
            int quantity;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in read input registers request line");
            }

            try {
                quantity = Integer.parseInt(quantityLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid quantity in read input registers request line");
            }

            this.address = address;
            this.quantity = quantity;

            return null;
        }

        ReadInputRegistersRequestMessage build() {
            assert this.address != null;
            assert this.quantity != null;

            return new ReadInputRegistersRequestMessage(this.address, this.quantity);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int quantity;

    ReadInputRegistersRequestMessage(final int address, final int quantity) {
        super(ModbusProxyRequestType.ReadInputRegisters);

        this.address = address;
        this.quantity = quantity;
    }

    public int getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.quantity);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + ", quantity: " + this.quantity + " }";
    }
}
