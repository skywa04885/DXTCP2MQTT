package nl.duflex.proxy.http;

import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyTcpClient;

public class HttpRestApiClientHandler extends ProxyProtocolClientHandler {
    public HttpRestApiClientHandler(ProxyTcpClient client) {
        super(client);
    }

    @Override
    public void run() {

    }
}
