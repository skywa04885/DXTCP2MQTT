package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;
import java.util.List;

class ReadDiscreteInputsResponseMessage extends ResponseMessage<ReadDiscreteInputsResponse> {
    static class Builder extends ResponseMessage.Builder<ReadDiscreteInputsResponseMessage> {
        private List<Boolean> values = null;

        Builder setValues(List<Boolean> values) {
            this.values = values;
            return this;
        }

        @Override
        Builder readFromInputStreamReader(ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            // Reads the line and returns null if the stream has ended.
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            // Decodes the coil values.
            this.values = ModbusProxyBitArrayDecoder.decodeFromString(line);

            // Returns the current instance.
            return this;
        }

        @Override
        ReadDiscreteInputsResponseMessage build() {
            assert this.values != null;

            return new ReadDiscreteInputsResponseMessage(this.values);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final List<Boolean> values;

    ReadDiscreteInputsResponseMessage(final List<Boolean> values) {
        this.values = values;
    }

    com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse toModbusResponse() {
        final byte[] encodedCoilBytes = ModbusProxyBitArrayEncoder.encodeToBinary(this.values);
        return new com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse(
                io.netty.buffer.Unpooled.wrappedBuffer(encodedCoilBytes));
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(ModbusProxyBitArrayEncoder.encode(this.values)).flush();
    }
}
