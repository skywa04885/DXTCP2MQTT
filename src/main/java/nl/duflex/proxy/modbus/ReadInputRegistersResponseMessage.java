package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import io.netty.buffer.Unpooled;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class ReadInputRegistersResponseMessage extends BufferResponseMessage<com.digitalpetri.modbus.responses.ReadInputRegistersResponse> {
    static class Builder extends BufferResponseMessage.Builder<com.digitalpetri.modbus.responses.ReadInputRegistersResponse, ReadInputRegistersResponseMessage> {
        @Override
        ReadInputRegistersResponseMessage build() {
            return new ReadInputRegistersResponseMessage(this.buffer);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    ReadInputRegistersResponseMessage(final byte[] buffer) {
        super(buffer);
    }

    @Override
    com.digitalpetri.modbus.responses.ReadInputRegistersResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.ReadInputRegistersResponse(
                io.netty.buffer.Unpooled.copiedBuffer(this.buffer));
    }
}
