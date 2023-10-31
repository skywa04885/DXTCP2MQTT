package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.ModbusResponse;
import io.netty.buffer.Unpooled;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

public abstract class BufferResponseMessage<U extends ModbusResponse> extends ResponseMessage<U> {
    static abstract class Builder<U extends ModbusResponse, T extends BufferResponseMessage<U>> extends ResponseMessage.Builder<T> {
        protected byte[] buffer = null;

        abstract T build();

        @Override
        Builder<U, T> readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException {
            final String nBytesLine = inputStreamReader.readStringUntilNewLine();
            if (nBytesLine == null) return null;

            int nBytes;

            try {
                nBytes = Integer.parseInt(nBytesLine);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("Received invalid number of bytes");
            }

            final byte[] buffer = inputStreamReader.readNBytes(nBytes);
            if (buffer == null) return null;

            this.buffer = buffer;

            return this;
        }
    }

    protected final byte[] buffer;

    BufferResponseMessage(final byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(String.valueOf(this.buffer.length)).write(this.buffer).flush();
    }

    abstract U toModbusResponse();
}
