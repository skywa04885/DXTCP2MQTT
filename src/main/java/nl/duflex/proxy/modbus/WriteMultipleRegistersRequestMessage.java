package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteMultipleRegistersRequestMessage extends RequestMessage<WriteMultipleRegistersRequest> {
    public static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest, WriteMultipleRegistersRequestMessage> {
        private Integer address = null;
        private Integer quantity = null;
        private byte[] values = null;

        @Override
        Builder copyFromModbusRequest(final WriteMultipleRegistersRequest request) {
            this.address = request.getAddress();
            this.quantity = request.getQuantity();
            this.values = io.netty.buffer.ByteBufUtil.getBytes(request.getValues());

            return this;
        }

        @Override
        Builder readFromInputStreamReader(ProxyInputStreamReader inputStreamReader) throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 3) throw new RuntimeException("Invalid number of segments in read write multiple " +
                    "registers request");

            final String addressLineSegment = lineSegments[0];
            final String quantityLineSegment = lineSegments[1];
            final String noBytesLineSegment = lineSegments[2];

            int address;
            int quantity;
            int noBytes;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in write multiple registers request line");
            }

            try {
                quantity = Integer.parseInt(quantityLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid quantity in write multiple registers request line");
            }

            try {
                noBytes = Integer.parseInt(noBytesLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid no bytes in write multiple registers request line");
            }

            final byte[] values = inputStreamReader.readNBytes(noBytes);
            if (values == null) return null;

            this.address = address;
            this.quantity = quantity;
            this.values = values;

            return this;
        }

        WriteMultipleRegistersRequestMessage build() {
            assert this.address != null;
            assert this.quantity != null;
            assert this.values != null;

            return new WriteMultipleRegistersRequestMessage(this.address, this.quantity, this.values);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int quantity;
    private final byte[] values;

    WriteMultipleRegistersRequestMessage(final int address, final int quantity, final byte[] values) {
        super(ModbusProxyRequestType.WriteMultipleRegisters);

        this.address = address;
        this.quantity = quantity;
        this.values = values;
    }

    public int getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

    public byte[] getValues() {
        return values;
    }

    com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest toRequest() {
        return new com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest(this.address, this.quantity, this.values);
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer
                .writeLine(this.address + " " + this.quantity)
                .write(this.values)
                .flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + ", quantity: " + this.quantity + " }";
    }
}
