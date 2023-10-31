package nl.duflex.proxy.modbus;

class ReadHoldingRegistersResponseMessage extends BufferResponseMessage<com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse> {
    static class Builder extends BufferResponseMessage.Builder<com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse, ReadHoldingRegistersResponseMessage> {
        @Override
        public ReadHoldingRegistersResponseMessage build() {
            return new ReadHoldingRegistersResponseMessage(this.buffer);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    ReadHoldingRegistersResponseMessage(final byte[] buffer) {
        super(buffer);
    }

    @Override
    com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse(
                io.netty.buffer.Unpooled.copiedBuffer(this.buffer));
    }
}
