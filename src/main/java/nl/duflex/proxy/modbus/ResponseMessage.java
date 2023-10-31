package nl.duflex.proxy.modbus;

import nl.duflex.proxy.IProxyOutputStreamWritable;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

abstract class ResponseMessage<U extends com.digitalpetri.modbus.responses.ModbusResponse> implements IProxyOutputStreamWritable {
    static abstract class Builder<T extends ResponseMessage<?>> {
        abstract Builder<T> readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader) throws IOException, RuntimeException;

        abstract T build();
    }

    abstract U toModbusResponse();

    public abstract void writeToProxyOutputStream(final ProxyOutputStreamWriter writer) throws IOException;
}
