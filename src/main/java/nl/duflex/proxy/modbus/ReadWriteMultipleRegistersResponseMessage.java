package nl.duflex.proxy.modbus;

class ReadWriteMultipleRegistersResponseMessage extends BufferResponseMessage<com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse> {
    static class Builder extends BufferResponseMessage.Builder<com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse, ReadWriteMultipleRegistersResponseMessage> {
        @Override
        public ReadWriteMultipleRegistersResponseMessage build() {
            return new ReadWriteMultipleRegistersResponseMessage(this.buffer);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    ReadWriteMultipleRegistersResponseMessage(final byte[] buffer) {
        super(buffer);
    }

    @Override
    com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse toModbusResponse() {
        return new com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse(
                io.netty.buffer.Unpooled.copiedBuffer(this.buffer));
    }
}
