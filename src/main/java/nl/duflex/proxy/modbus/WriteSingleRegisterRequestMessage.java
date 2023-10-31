package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteSingleRegisterRequestMessage extends RequestMessage<WriteSingleRegisterRequest> {
    static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.WriteSingleRegisterRequest, WriteSingleRegisterRequestMessage> {
        private Integer address = null;
        private Integer value = null;

        @Override
        Builder copyFromModbusRequest(WriteSingleRegisterRequest request) {
            this.address = request.getAddress();
            this.value = request.getValue();
            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader)
                throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.trim().split(" +");
            if (lineSegments.length != 2)
                throw new RuntimeException("Invalid number of line segments for write single register request");

            final String addressLineSegment = lineSegments[0].trim();
            final String valueLineSegment = lineSegments[1].trim();

            int address;
            int value;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in line for write single register request");
            }

            try {
                value = Integer.parseInt(valueLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid value in line for write single register request");
            }

            this.address = address;
            this.value = value;

            return this;
        }

        WriteSingleRegisterRequestMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new WriteSingleRegisterRequestMessage(this.address, this.value);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int value;

    WriteSingleRegisterRequestMessage(final int address, final int value) {
        super(ModbusProxyRequestType.WriteSingleRegister);

        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return this.address;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.value).flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + " }";
    }
}
