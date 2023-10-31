package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

abstract class RequestMessage<U extends com.digitalpetri.modbus.requests.ModbusRequest> {
    static abstract class Builder<U extends com.digitalpetri.modbus.requests.ModbusRequest, T extends RequestMessage<U>>
    {
        abstract Builder<U, T> copyFromModbusRequest(final U request);

        abstract Builder<U, T> readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws RuntimeException, IOException;

        abstract T build();
    }

    final ModbusProxyRequestType requestType;

    RequestMessage(final ModbusProxyRequestType requestType) {
        this.requestType = requestType;
    }

    void writeToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        this.requestType.writeToOutputStreamWriter(writer);
        this.implWriteToOutputStreamWriter(writer);
    }

    abstract void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException;
}
