package nl.duflex.proxy.http;

import nl.duflex.proxy.ProtocolClientHandlerBuilder;
import nl.duflex.proxy.ProxyProtocolClientHandler;

public class HttpRestApiClientHandlerBuilder extends ProtocolClientHandlerBuilder {
    @Override
    protected ProxyProtocolClientHandler buildImpl() {
        return new HttpRestApiClientHandler(this.client);
    }
}
