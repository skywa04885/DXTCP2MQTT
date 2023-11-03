package nl.duflex.proxy.http;

import nl.duflex.proxy.ProtocolClientHandlerBuilder;
import nl.duflex.proxy.ProxyProtocolClientHandler;
import org.jetbrains.annotations.Nullable;

public class HttpRestApiClientHandlerBuilder extends ProtocolClientHandlerBuilder {
    private @Nullable DXHttpConfig config = null;

    public HttpRestApiClientHandlerBuilder setConfig(final DXHttpConfig config) {
        this.config = config;

        return this;
    }

    @Override
    protected ProxyProtocolClientHandler buildImpl() {
        assert config != null;

        return new HttpRestApiClientHandler(client, config);
    }
}
