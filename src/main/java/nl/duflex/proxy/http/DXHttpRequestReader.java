package nl.duflex.proxy.http;

import nl.duflex.proxy.ProxyInputStreamReader;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DXHttpRequestReader {
    public static final Pattern KEY_VALUE_PAIR_PATTERN =
            Pattern.compile("^(?<Key>[a-zA-Z0-9_\\-]+): (?<Value>.+)$");

    private final ProxyInputStreamReader proxyInputStreamReader;
    private final DXHttpConfig config;

    public DXHttpRequestReader(final ProxyInputStreamReader proxyInputStreamReader, final DXHttpConfig config) {
        this.proxyInputStreamReader = proxyInputStreamReader;
        this.config = config;
    }

    private HashMap<String, String> ReadKeyValuePairs(final String[] lines) {
        final var result = new HashMap<String, String>();

        for (final var line : lines) {
            // Matches the line against the key/ value pair regular expression, if it does not match
            //  throw an exception.
            final Matcher matcher = KEY_VALUE_PAIR_PATTERN.matcher(line);
            if (!matcher.matches())
                throw new RuntimeException("Invalid key/ value pair");

            // Gets the key and the value from the matcher.
            final String key = matcher.group("Key");
            final String value = matcher.group("Value");

            // Inserts the key/ value pair into the result map.
            result.put(key, value);
        }

        return result;
    }

    public DXHttpRequest Read() throws IOException {
        ///
        /// Gets the initialization line.
        ///

        // Read the initialization line.
        String tempString = proxyInputStreamReader.readStringUntilNewLine();
        if (tempString == null) return null;

        // Split the initialization line into its segments.
        String[] tempStringSegments = tempString.trim().split(" ");

        // Get the segments from the temporary string,
        final String methodLabel = tempStringSegments[0].trim();
        final String apiName = tempStringSegments[1].trim();
        final String instanceName = tempStringSegments[2].trim();
        final String endpointName = tempStringSegments[3].trim();

        // Gets the request method based on the label.
        final var requestMethod = DXHttpRequestMethod.FromLabel(methodLabel);

        ///
        /// Get the configurations based on the initialization line.
        ///

        // Gets the config api based on the name.
        final DXHttpConfigApi configApi = config.GetApiByName(apiName);
        if (configApi == null)
            throw new RuntimeException("Could not find API with name " + apiName);

        // Gets the config instance based on the name.
        final DxHttpConfigInstance configInstance = configApi.GetInstanceByName(instanceName);
        if (configInstance == null)
            throw new RuntimeException("Could not find instance with name " + instanceName + " in api " + apiName);

        // Gets the config endpoint based on the name.
        final DXHttpConfigEndpoint configEndpoint = configApi.GetEndpointByName(endpointName);
        if (configEndpoint == null)
            throw new RuntimeException("Could not find endpoint with name " + endpointName + " in api " + apiName);

        // Gets the config request based on the method.
        final DXHttpConfigRequest configRequest = configEndpoint.GetRequestByMethod(requestMethod);
        if (configRequest == null)
            throw new RuntimeException("Could not find request with method " + requestMethod.Label + " in endpoint "
                    + endpointName + " in api " + apiName);

        // Gets the config request uri.
        final DXHttpConfigUri configRequestUri = configRequest.Uri;

        ///
        /// Gets the remaining templating data based on the configuration.
        ///

        // gets the path substitutions if they are required by the configuration.
        HashMap<String, String> pathSubstitutions = null;
        if (configRequestUri.Path.ShouldSubstitute()) {
            if ((tempString = proxyInputStreamReader.readStringUntilDoubleNewLine()) == null) return null;
            pathSubstitutions = ReadKeyValuePairs(tempString.split("\\r?\\n"));
        }

        // Gets the query parameters if they are required by the configuration.
        HashMap<String, String> queryParameters;
        if (!configRequestUri.QueryParameters.isEmpty()) {
            if ((tempString = proxyInputStreamReader.readStringUntilDoubleNewLine()) == null) return null;
            queryParameters = ReadKeyValuePairs(tempString.split("\\r?\\n"));
        } else {
            queryParameters = null;
        }

        // Gets the headers if they are required by the configuration.
        HashMap<String, String> headers = null;
        if (configRequest.Headers != null && !configRequest.Headers.Children.isEmpty()) {
            if ((tempString = proxyInputStreamReader.readStringUntilDoubleNewLine()) == null) return null;
            headers = ReadKeyValuePairs(tempString.split("\\r?\\n"));
        }

        // Gets the body fields if they are required by the configuration.
        HashMap<String, String> fields = null;
        if (configRequest.Fields != null && !configRequest.Fields.Fields.isEmpty()) {
            if ((tempString = proxyInputStreamReader.readStringUntilDoubleNewLine()) == null) return null;
            fields = ReadKeyValuePairs(tempString.split("\\r?\\n"));
        }

        ///
        /// Constructs the HTTP request.
        ///

        // Creates the initial part of the URI, which does not include the query parameters.
        final var pathRenderer = new DXHttpPathTemplateRenderer(configRequestUri.Path);
        StringBuilder uriStringBuilder = new StringBuilder(configInstance.Protocol + "://" + configInstance.Host
                + ':' + configInstance.Port + pathRenderer.Render(pathSubstitutions));

        // Adds the query parameters to the request URI.
        if (!configRequestUri.QueryParameters.isEmpty()) {
            // The query parameters will never be null due to previous logic.
            assert queryParameters != null;

            // Builds the query parameters.
            uriStringBuilder.append('?').append(configRequestUri.QueryParameters.values().stream().map(param -> {
                var value = param.Value != null ? param.Value : queryParameters.get(param.Key);
                if (value == null) throw new RuntimeException("Missing query parameter for key: " + param.Key);
                return URLEncoder.encode(param.Key, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8);
            }).collect(Collectors.joining("&")));
        }

        // Creates the request builder based on the built URI.
        var httpRequestBuilder = HttpRequest.newBuilder(URI.create(uriStringBuilder.toString()));

        // Sets the headers if required.
        if (configRequest.Headers != null && !configRequest.Headers.Children.isEmpty()) {
            // Headers will never be null due to previous logic.
            assert headers != null;

            // Sets all the headers.
            for (final var header : configRequest.Headers.Children.values()) {
                // Gets the value of the header.
                var value = headers.get(header.Name != null ? header.Name : header.Key);
                if (value == null) value = header.Value;

                // If the value is still null throw a runtime exception.
                if (value == null)
                    throw new RuntimeException("Missing value for header with key: " + header.Key);

                // Adds the header to the request builder.
                httpRequestBuilder = httpRequestBuilder.header(header.Key, value);
            }
        }

        // Constructs the body publisher if needed.
        HttpRequest.BodyPublisher bodyPublisher = null;
        if (configRequest.Fields != null && !configRequest.Fields.Fields.isEmpty())
        {
            // Makes sure that the request method supports a body,
            if (configRequest.Method != DXHttpRequestMethod.Post && configRequest.Method != DXHttpRequestMethod.Put)
                throw new RuntimeException("Fields specified in request that does not support a body");

            // Encodes the fields and creates the body publisher.
            final byte[] encodedFields = DXHttpFieldsEncoder.Encode(configRequest.Fields, fields);
            bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(encodedFields);

            // Adds the Content-Type header.
            httpRequestBuilder.header("Content-Type", configRequest.Fields.Format.MimeType);
        }

        // Sets the request method and the possible body publisher.
        switch (configRequest.Method) {
            case Get -> httpRequestBuilder = httpRequestBuilder.GET();
            case Post -> httpRequestBuilder = httpRequestBuilder.POST(bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
            case Put -> httpRequestBuilder = httpRequestBuilder.PUT(bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
            case Delete -> httpRequestBuilder = httpRequestBuilder.DELETE();
        }

        // Builds the http request.
        final HttpRequest httpRequest = httpRequestBuilder.build();

        // Returns the DX http request.
        return new DXHttpRequest(httpRequest, configRequest.Responses);
    }
}
