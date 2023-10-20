package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

public abstract class ModbusProxyRequestMessage {
    protected final ModbusProxyRequestType requestType;

    protected ModbusProxyRequestMessage(final ModbusProxyRequestType requestType) {
        this.requestType = requestType;
    }

    public void writeToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        this.requestType.writeToOutputStreamWriter(writer);
        this.implWriteToOutputStreamWriter(writer);
    }

    protected abstract void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException;
}
