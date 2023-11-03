package nl.duflex.proxy.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DXHttpRequest {
    private final HttpRequest _httpRequest;
    private final DXHttpConfigResponses _configResponses;

    public DXHttpRequest(final HttpRequest httpRequest, final DXHttpConfigResponses configResponses) {
        _httpRequest = httpRequest;
        _configResponses = configResponses;
    }

    public DXHttpResponse Perform(final HttpClient httpClient) throws IOException, InterruptedException {
        final HttpResponse<byte[]> response = httpClient.send(_httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        return DXHttpResponseFactory.FromResponse(response, _configResponses);
    }
}
