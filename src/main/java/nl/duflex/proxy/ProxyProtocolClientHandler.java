package nl.duflex.proxy;

public abstract class ProxyProtocolClientHandler implements Runnable {
    protected final ProxyTcpClient client;

    public ProxyProtocolClientHandler(final ProxyTcpClient client) {
        this.client = client;
    }
}
