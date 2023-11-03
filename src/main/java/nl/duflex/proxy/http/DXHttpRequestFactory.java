package nl.duflex.proxy.http;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DXHttpRequestFactory {
    /**
     * Creates an HTTP request from the given config and request data. Super messy method, does a lot, but
     *  does it well.
     * @param config The configuration.
     * @param requestData The request data.
     * @return The created request.
     */
    public static DXHttpRequest FromConfig(final DXHttpConfig config, final DXHttpRequestData requestData) {
        // Gets the API.
        final DXHttpConfigApi api = config.GetApiByName(requestData.ApiName);
        if (api == null) throw new RuntimeException("Could not find API with name: " + requestData.ApiName);

        // Gets the endpoint.
        final DXHttpConfigEndpoint endpoint = api.GetEndpointByName(requestData.EndpointName);
        if (endpoint == null)
            throw new RuntimeException("Could not find endpoint with name " + requestData.EndpointName
                    + " in API with name " + requestData.ApiName);

        // Gets the instance.
        final DxHttpConfigInstance instance = api.GetInstanceByName(requestData.InstanceName);
        if (instance == null)
            throw new RuntimeException("Could not find instance with name " + requestData.InstanceName
                    + " in API with name " + requestData.ApiName);

        // Gets the request using the given method.
        final DXHttpConfigRequest request = endpoint.GetRequestByMethod(requestData.Method);
        if (request == null)
            throw new RuntimeException("No request using method " + requestData.Method + " in endpoint "
                    + requestData.EndpointName + " of API with name " + requestData.ApiName);

        final DXHttpConfigUri uri = request.Uri;
        final Map<String, DXHttpConfigUriQueryParameter> queryParameters = uri.QueryParameters;

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
        for (final var header : request.Headers.Children.values()) {
            // Gets the value of the header from the request data, the request data can contain shorter names
            //  for the specific headers, that's why the name is used if it has been specified. If there is no
            //  such header value for the key/ name use the default value.
            var value = requestData.Headers.get(header.Name != null ? header.Name : header.Key);
            if (value == null) value = header.Value;

            // If the value is still null throw a runtime exception.
            if (value == null)
                throw new RuntimeException("Missing value for header with key: " + header.Key);

            // Adds the header to the request builder.
            httpRequestBuilder = httpRequestBuilder.header(header.Key, value);
        }

        // Constructs the body publisher if needed.
        HttpRequest.BodyPublisher bodyPublisher = null;
        if (request.Fields != null)
        {
            // Makes sure that the request method supports a body,
            if (request.Method != DXHttpRequestMethod.Post && request.Method != DXHttpRequestMethod.Put)
                throw new RuntimeException("Fields specified in request that does not support a body");

            // Encodes the fields and creates the body publisher.
            final byte[] encodedFields = DXHttpFieldsEncoder.Encode(request.Fields, requestData.Body);
            bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(encodedFields);

            // Adds the Content-Type header.
            httpRequestBuilder.header("Content-Type", request.Fields.Format.MimeType);
        }

        // Sets the request method and the possible body publisher.
        switch (requestData.Method) {
            case Get -> httpRequestBuilder = httpRequestBuilder.GET();
            case Post -> httpRequestBuilder = httpRequestBuilder.POST(bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
            case Put -> httpRequestBuilder = httpRequestBuilder.PUT(bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
            case Delete -> httpRequestBuilder = httpRequestBuilder.DELETE();
        }

        final var responses = request.Responses;

        final HttpRequest httpRequest = httpRequestBuilder.build();

        return new DXHttpRequest(httpRequest, responses);
    }
}
