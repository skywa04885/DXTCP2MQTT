package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.MaskWriteRegisterRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class MaskWriteRegisterRequestMessage extends RequestMessage<com.digitalpetri.modbus.requests.MaskWriteRegisterRequest> {
    static class Builder
            extends RequestMessage.Builder<com.digitalpetri.modbus.requests.MaskWriteRegisterRequest, MaskWriteRegisterRequestMessage> {
        private Integer address = null;
        private Integer orMask = null;
        private Integer andMask = null;

        @Override
        Builder copyFromModbusRequest(final MaskWriteRegisterRequest request) {
            this.address = request.getAddress();
            this.orMask = request.getOrMask();
            this.andMask = request.getAndMask();

            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.trim().split(" +");
            if (lineSegments.length != 3)
                throw new RuntimeException("Invalid number of line segments for mask write register request line");

            final String addressLineSegment = lineSegments[0].trim();
            final String orMaskLineSegment = lineSegments[1].trim();
            final String andMaskLineSegment = lineSegments[2].trim();

            int address;
            int orMask;
            int andMask;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in mask write register request line");
            }

            try {
                orMask = Integer.parseInt(orMaskLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid or mask in mask write register request line");
            }

            try {
                andMask = Integer.parseInt(andMaskLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid and mask in mask write register request line");
            }

            this.address = address;
            this.orMask = orMask;
            this.andMask = andMask;

            return this;
        }

        @Override
        MaskWriteRegisterRequestMessage build() {
            assert this.address != null;
            assert this.orMask != null;
            assert this.andMask != null;

            return null;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int orMask;
    private final int andMask;

    MaskWriteRegisterRequestMessage(final int address, final int orMask, final int andMask) {
        super(ModbusProxyRequestType.MaskWriteRegister);

        this.address = address;
        this.orMask = orMask;
        this.andMask = andMask;
    }

    public int getAddress() {
        return address;
    }

    public int getOrMask() {
        return orMask;
    }

    public int getAndMask() {
        return andMask;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer
                .writeLine(this.address + " " + this.orMask + " " + this.andMask)
                .flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + ", or mask: " + this.orMask
                + ", and mask: " + this.andMask + " }";
    }
}
