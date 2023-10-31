package nl.duflex.proxy;

import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

public interface IProxyOutputStreamWritable {
    void writeToProxyOutputStream(final ProxyOutputStreamWriter proxyOutputStreamWriter) throws IOException;
}
