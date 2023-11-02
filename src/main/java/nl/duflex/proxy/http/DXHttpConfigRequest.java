package nl.duflex.proxy.http;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class DXHttpConfigRequest {
    public static final String ELEMENT_TAG_NAME = "Request";
    public static final String METHOD_ATTRIBUTE_NAME = "Method";
    public static final String HEADERS_ELEMENT_TAG_NAME = "Headers";

    public final DXHttpConfigUri Uri;
    public final DXHttpRequestMethod Method;
    public final Map<String, DXHttpConfigHeader> Headers;

    public DXHttpConfigRequest(final DXHttpConfigUri uri, final DXHttpRequestMethod method,
                               final Map<String, DXHttpConfigHeader> headers) {
        Uri = uri;
        Method = method;
        Headers = headers;
    }

    public static DXHttpConfigRequest FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME)) throw new RuntimeException("Tag name mismatch");

        final var uriElements = element.getElementsByTagName(DXHttpConfigUri.ELEMENT_TAG_NAME);
        if (uriElements.getLength() == 0) throw new RuntimeException("Uri element is missing");
        else if (uriElements.getLength() > 1) throw new RuntimeException("Too many uri elements");
        final var uriElement = (Element) uriElements.item(0);

        final var uri = DXHttpConfigUri.FromElement(uriElement);

        final var methodString = element.getAttribute(METHOD_ATTRIBUTE_NAME);
        final var method = DXHttpRequestMethod.FromLabel(methodString);

        final var headersElements = element.getElementsByTagName(HEADERS_ELEMENT_TAG_NAME);
        if (headersElements.getLength() == 0) throw new RuntimeException("Headers element is missing");
        if (headersElements.getLength() > 1) throw new RuntimeException("Too many headers elements");
        final var headersElement = (Element) headersElements.item(0);
        final var headerElements = headersElement.getElementsByTagName(DXHttpConfigHeader.ELEMENT_TAG_NAME);

        final var headers = new HashMap<String, DXHttpConfigHeader>();

        for (var i = 0; i < headerElements.getLength(); ++i) {
            final var headerElement = (Element) headerElements.item(i);
            final var header = DXHttpConfigHeader.FromElement(headerElement);

            if (headers.containsKey(header.Key)) throw new RuntimeException("Duplicate header with key: " + header.Key);

            headers.put(header.Key, header);
        }

        return new DXHttpConfigRequest(uri, method, headers);
    }
}
