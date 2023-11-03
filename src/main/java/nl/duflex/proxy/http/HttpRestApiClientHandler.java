package nl.duflex.proxy.http;

import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyTcpClient;

import java.io.IOException;
import java.net.http.HttpClient;

public class HttpRestApiClientHandler extends ProxyProtocolClientHandler {
    private final DXHttpConfig config;

    public HttpRestApiClientHandler(final ProxyTcpClient client, final DXHttpConfig config) {
        super(client);

        this.config = config;
    }

    @Override
    public void run() {
        final var proxyInputStreamReader = client.getInputStreamReader();
        final var proxyOutputStreamWriter = client.getOutputStreamWriter();

        final var requestReader = new DXHttpRequestReader(proxyInputStreamReader, config);
        final var responseWriter = new DXHttpResponseWriter(proxyOutputStreamWriter);

        final var httpClient = HttpClient.newHttpClient();

        try {
            while (true) {
                // Reads the request.
                final DXHttpRequest request = requestReader.Read();
                if (request == null) break;

                // Performs the HTTP request.
                final DXHttpResponse response = request.perform(httpClient);

                // Writes the response.
                responseWriter.write(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
