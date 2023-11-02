package nl.duflex.proxy;

public abstract class ProtocolClientHandlerBuilder {
    protected ProxyTcpClient client = null;

    public ProtocolClientHandlerBuilder setClient(final ProxyTcpClient client) {
        this.client = client;

        return this;
    }

    protected abstract ProxyProtocolClientHandler buildImpl();

    public ProxyProtocolClientHandler build()
    {
        assert this.client != null;
        return buildImpl();
    }
}
