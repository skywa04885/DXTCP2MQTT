package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DXHttpConfigRequest {
    public static final String ELEMENT_TAG_NAME = "Request";
    public static final String METHOD_ATTRIBUTE_NAME = "Method";

    public final DXHttpConfigUri Uri;
    public final DXHttpRequestMethod Method;
    public final DXHttpConfigHeaders Headers;
    public final DXHttpConfigFields Fields;
    public final DXHttpConfigResponses Responses;

    public DXHttpConfigRequest(final DXHttpConfigUri uri, final DXHttpRequestMethod method,
                               final DXHttpConfigHeaders headers,
                               final DXHttpConfigFields fields,
                               final DXHttpConfigResponses responses) {
        Uri = uri;
        Method = method;
        Headers = headers;
        Fields = fields;
        Responses = responses;
    }

    public static DXHttpConfigRequest FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME)) throw new RuntimeException("Tag name mismatch");

        final var uriElements = DXDomUtils.GetChildElementsWithTagName(element, DXHttpConfigUri.ELEMENT_TAG_NAME);
        if (uriElements.size() == 0) throw new RuntimeException("Uri element is missing");
        else if (uriElements.size() > 1) throw new RuntimeException("Too many uri elements");
        final var uriElement = (Element) uriElements.get(0);

        final var uri = DXHttpConfigUri.FromElement(uriElement);

        final var methodString = element.getAttribute(METHOD_ATTRIBUTE_NAME);
        final var method = DXHttpRequestMethod.FromLabel(methodString);

        final var headersElements = DXDomUtils.GetChildElementsWithTagName(element, DXHttpConfigHeaders.ELEMENT_TAG_NAME);
        if (headersElements.size() == 0) throw new RuntimeException("Headers element is missing");
        if (headersElements.size() > 1) throw new RuntimeException("Too many headers elements");
        final var headersElement = headersElements.get(0);

        final var headers = DXHttpConfigHeaders.FromElement(headersElement);

        final List<Element> fieldsElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigFields.ELEMENT_TAG_NAME);

        DXHttpConfigFields fields = null;

        if (fieldsElements.size() == 1) {
            final var fieldsElement = (Element) fieldsElements.get(0);
            fields = DXHttpConfigFields.FromElement(fieldsElement);
        } else if (fieldsElements.size() > 1) throw new RuntimeException("Too many fields elements");

        final List<Element> responsesElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigResponses.ELEMENT_TAG_NAME);

        if (responsesElements.size() == 0)
            throw new RuntimeException("Responses element is missing");
        else if (responsesElements.size() > 1)
            throw new RuntimeException("Too many responses elements");

        final Element responsesElement = responsesElements.get(0);

        final var responses = DXHttpConfigResponses.FromElement(responsesElement);

        return new DXHttpConfigRequest(uri, method, headers, fields, responses);
    }
}
