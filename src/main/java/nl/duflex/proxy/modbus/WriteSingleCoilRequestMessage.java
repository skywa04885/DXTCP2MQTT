package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteSingleCoilRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class WriteSingleCoilRequestMessage extends RequestMessage<WriteSingleCoilRequest> {
    static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.WriteSingleCoilRequest, WriteSingleCoilRequestMessage> {
        private Integer address = null;
        private Boolean value = null;

        @Override
        Builder copyFromModbusRequest(final WriteSingleCoilRequest request) {
            this.address = request.getAddress();
            this.value = request.getValue() == 0xFF00;
            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader)
                throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.trim().split(" +");
            if (lineSegments.length != 2)
                throw new RuntimeException("Invalid number of segments for write single coil request");

            final String addressLineSegment = lineSegments[0].trim();
            final String valueLineSegment = lineSegments[1].trim();

            int address;
            boolean value;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in line for write single coil request");
            }

            if (valueLineSegment.equals("1")) value = true;
            else if (valueLineSegment.equals("0")) value = false;
            else throw new RuntimeException("Invalid value in line for write single coil request");

            this.address = address;
            this.value = value;

            return this;
        }

        WriteSingleCoilRequestMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new WriteSingleCoilRequestMessage(this.address, this.value);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final boolean value;

    WriteSingleCoilRequestMessage(final int address, final boolean value) {
        super(ModbusProxyRequestType.WriteSingleCoil);

        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return this.address;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    void implWriteToOutputStreamWriter(ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + (this.value ? "1" : "0")).flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + " }";
    }
}
