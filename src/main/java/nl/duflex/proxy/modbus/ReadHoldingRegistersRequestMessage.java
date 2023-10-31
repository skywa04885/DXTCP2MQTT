package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class ReadHoldingRegistersRequestMessage extends RequestMessage<ReadHoldingRegistersRequest> {
    static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest, ReadHoldingRegistersRequestMessage> {
        private Integer address = null;
        private Integer quantity = null;

        @Override
        Builder copyFromModbusRequest(final ReadHoldingRegistersRequest request) {
            this.address = request.getAddress();
            this.quantity = request.getQuantity();
            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 2) throw new RuntimeException("Invalid number of segments in read holding register request line");

            final String addressLineSegment = lineSegments[0];
            final String quantityLineSegment = lineSegments[1];

            int address;
            int quantity;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in read holding register request line");
            }

            try {
                quantity = Integer.parseInt(quantityLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid quantity in read holding register request line");
            }

            this.address = address;
            this.quantity = quantity;

            return null;
        }

        public ReadHoldingRegistersRequestMessage build() {
            assert this.address != null;
            assert this.quantity != null;

            return new ReadHoldingRegistersRequestMessage(this.address, this.quantity);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int quantity;

    ReadHoldingRegistersRequestMessage(final int address, final int value) {
        super(ModbusProxyRequestType.ReadHoldingRegisters);

        this.address = address;
        this.quantity = value;
    }

    public int getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.quantity).flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + ", quantity: " + this.quantity + " }";
    }
}
