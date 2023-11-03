package nl.duflex.proxy.http;

import nl.duflex.proxy.DXJsonUtils;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DXHttpResponseFactory {
    public static DXHttpResponse FromResponse(final HttpResponse<byte[]> httpResponse, final DXHttpConfigResponses configResponses) {
        final DXHttpConfigResponse configResponse = configResponses.GetByCode(httpResponse.statusCode());
        if (configResponse == null)
            throw new RuntimeException("No config response found for status code: " + httpResponse.statusCode());

        // Gets the response body.
        Map<String, String> body = null;
        if (configResponse.Fields != null && configResponse.Fields.Format == DXHttpFieldsFormat.JSON) {
            body = new HashMap<>();

            // Parses the json object.
            final var jsonObject = new JSONObject(new String(httpResponse.body(), StandardCharsets.UTF_8));

            // Loops over all the fields and gets the required fields from the body.
            for (final DXHttpConfigField configField : configResponse.Fields.Fields.values()) {
                // Gets the value of the field, which is the one that is supplied in the response, and if that does not
                // exist the default value given in the config.
                final Object valueInsideJsonObject = DXJsonUtils.GetRecursive(jsonObject, configField.Path);
                final String value = valueInsideJsonObject != null
                        ? valueInsideJsonObject.toString()
                        : configField.Value;

                // If the value still was not found throw a runtime exception.
                if (value == null)
                    throw new RuntimeException("Could not find value for field with path " + configField.Path);

                // Insert the value and the name in the response body.
                body.put(configField.Name, value);
            }
        }

        // Gets all the headers of interest.
        Map<String, String> headers = null;
        if (configResponse.Headers != null && !configResponse.Headers.Children.isEmpty()) {
            headers = new HashMap<>();

            for (final DXHttpConfigHeader header : configResponse.Headers.Children.values()) {
                // Gets the header value.
                final var optionalValue = httpResponse.headers().firstValue(header.Key);
                final var value = optionalValue.orElseGet(() -> header.Value);

                // If the value is still null throw a runtime error.
                if (value == null)
                    throw new RuntimeException("Missing value for header with key: " + header.Key);

                // Puts the header value together with its key (or name) in the headers.
                headers.put(header.Name != null ? header.Name : header.Key, value);
            }
        }

        return new DXHttpResponse(configResponse.Code, headers, body);
    }
}
