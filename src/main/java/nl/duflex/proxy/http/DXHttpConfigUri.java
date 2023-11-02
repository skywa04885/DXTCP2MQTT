package nl.duflex.proxy.http;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class DXHttpConfigUri {
    public static final String ELEMENT_TAG_NAME = "Uri";
    public static final String PATH_ATTRIBUTE_NAME = "Path";
    public static final String QUERY_PARAMETERS_ELEMENT_TAG_NAME = "QueryParameters";

    public final DXHttpPathTemplate Path;
    public final Map<String, DXHttpConfigUriQueryParameter> QueryParameters;

    public DXHttpConfigUri(final DXHttpPathTemplate path, final Map<String, DXHttpConfigUriQueryParameter> queryParameters) {
        Path = path;
        QueryParameters = queryParameters;
    }

    public static DXHttpConfigUri FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME)) throw new RuntimeException("Tag name mismatch");

        final var pathString = element.getAttribute(PATH_ATTRIBUTE_NAME).trim();
        if (pathString.isEmpty()) throw new RuntimeException("Path attribute is missing");

        final var pathTemplateParser = new DXHttpPathTemplateParser();
        final var pathTemplate = pathTemplateParser.Parse(pathString);

        final var queryParametersElements = element.getElementsByTagName(QUERY_PARAMETERS_ELEMENT_TAG_NAME);
        if (queryParametersElements.getLength() == 0) throw new RuntimeException("Query parameters element missing");
        else if (queryParametersElements.getLength() > 1) throw new RuntimeException("Too many query parameters elements");

        final var queryParametersElement = (Element) queryParametersElements.item(0);
        final var queryParameterElements = queryParametersElement.getElementsByTagName(DXHttpConfigUriQueryParameter.TAG_NAME);

        final var queryParameters = new HashMap<String, DXHttpConfigUriQueryParameter>();

        for (var i = 0; i < queryParameterElements.getLength(); ++i)
        {
            final var queryParameterElement = (Element) queryParameterElements.item(i);
            final var queryParameter = DXHttpConfigUriQueryParameter.FromElement(queryParameterElement);
            queryParameters.put(queryParameter.Key, queryParameter);
        }

        return new DXHttpConfigUri(pathTemplate, queryParameters);
    }
}
