package nl.duflex.proxy.http;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class DXHttpRequestFactory {
    public static DXHttpRequest FromConfig(final DXHttpConfig config, final DXHttpRequestData requestData) {
        // Gets the API.
        final var api = config.GetApiByName(requestData.ApiName);
        if (api == null) throw new RuntimeException("Could not find API with name: " + requestData.ApiName);

        // Gets the endpoint.
        final var endpoint = api.GetEndpointByName(requestData.EndpointName);
        if (endpoint == null)
            throw new RuntimeException("Could not find endpoint with name " + requestData.EndpointName
                    + " in API with name " + requestData.ApiName);

        // Gets the instance.
        final var instance = api.GetInstanceByName(requestData.InstanceName);
        if (instance == null)
            throw new RuntimeException("Could not find instance with name " + requestData.InstanceName
                    + " in API with name " + requestData.ApiName);

        // Gets the request using the given method.
        final var request = endpoint.GetRequestByMethod(requestData.Method);
        if (request == null)
            throw new RuntimeException("No request using method " + requestData.Method + " in endpoint "
                    + requestData.EndpointName + " of API with name " + requestData.ApiName);

        final var uri = request.Uri;
        final var queryParameters = uri.QueryParameters;

        // Creates the initial part of the URI, which does not include the query parameters.
        final var pathRenderer = new DXHttpPathTemplateRenderer(request.Uri.Path);
        StringBuilder uriStringBuilder = new StringBuilder(instance.Protocol + "://" + instance.Host.getHostAddress()
                + ':' + instance.Port + pathRenderer.Render(requestData.PathSubstitutes));

        // Adds the query parameters to the request URI.
        if (queryParameters.size() > 0) {
            uriStringBuilder.append('?').append(queryParameters.values().stream().map(param -> {
                var value = param.Value != null ? param.Value : requestData.QueryParameters.get(param.Key);
                if (value == null) throw new RuntimeException("Missing query parameter for key: " + param.Key);
                return URLEncoder.encode(param.Key, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8);
            }).collect(Collectors.joining("&")));
        }

        var httpRequestBuilder = HttpRequest.newBuilder(URI.create(uriStringBuilder.toString()));

        // Sets the headers.
        for (final var header : request.Headers.values()) {
            final var value = header.Value != null ? header.Value : requestData.Headers.get(header.Key);
            if (value == null) throw new RuntimeException("Missing value for header with key: " + header.Key);
            httpRequestBuilder = httpRequestBuilder.header(header.Key, header.Value);
        }

        // Sets the request method and the possible body publisher.
        switch (requestData.Method) {
            case Get -> httpRequestBuilder = httpRequestBuilder.GET();
            case Post -> httpRequestBuilder = httpRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            case Put -> httpRequestBuilder = httpRequestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
            case Delete -> httpRequestBuilder = httpRequestBuilder.DELETE();
        }

        // Builds the request.
        final var httpRequest = httpRequestBuilder.build();


    }
}
