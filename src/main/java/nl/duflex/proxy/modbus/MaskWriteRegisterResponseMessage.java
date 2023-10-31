package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.MaskWriteRegisterRequest;
import com.digitalpetri.modbus.responses.MaskWriteRegisterResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class MaskWriteRegisterResponseMessage extends ResponseMessage<MaskWriteRegisterResponse> {
    static class Builder extends ResponseMessage.Builder<MaskWriteRegisterResponseMessage> {
        private Integer address = null;
        private Integer orMask = null;
        private Integer andMask = null;

        @Override
        Builder readFromInputStreamReader(
                final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 3) throw new RuntimeException("Invalid response line");

            final String addressLineSegment = lineSegments[0];
            final String orMaskLineSegment = lineSegments[1];
            final String andMaskLineSegment = lineSegments[2];

            int address;
            int orMask;
            int andMask;

            try {
                address = Integer.parseInt(addressLineSegment);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Invalid address");
            }

            try {
                orMask = Integer.parseInt(orMaskLineSegment);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Invalid or mask");
            }

            try {
                andMask = Integer.parseInt(andMaskLineSegment);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Invalid and mask");
            }

            this.address = address;
            this.orMask = orMask;
            this.andMask = andMask;

            return this;
        }

        @Override
        MaskWriteRegisterResponseMessage build() {
            assert this.address != null;
            assert this.orMask != null;
            assert this.andMask != null;

            return new MaskWriteRegisterResponseMessage(this.address, this.orMask, this.andMask);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int orMask;
    private final int andMask;

    MaskWriteRegisterResponseMessage(final int address, final int orMask, final int andMask) {
        this.address = address;
        this.orMask = orMask;
        this.andMask = andMask;
    }

    @Override
    com.digitalpetri.modbus.responses.MaskWriteRegisterResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.MaskWriteRegisterResponse(this.address, this.andMask, this.orMask);
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.orMask + " " + this.andMask).flush();
    }
}
