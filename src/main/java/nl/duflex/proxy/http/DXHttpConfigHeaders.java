package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class DXHttpConfigHeaders {
    public static final String ELEMENT_TAG_NAME = "Headers";

    public final Map<String, DXHttpConfigHeader> Children;

    public DXHttpConfigHeaders(final Map<String, DXHttpConfigHeader> children) {
        Children = children;
    }

    public static DXHttpConfigHeaders FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME))
            throw new RuntimeException("Element tag name mismatch, expected: " + ELEMENT_TAG_NAME + ", got: " +
                    element.getTagName());

        final var headerElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigHeader.ELEMENT_TAG_NAME);

        final var headers = new HashMap<String, DXHttpConfigHeader>();

        for (final Element value : headerElements) {
            final var header = DXHttpConfigHeader.FromElement(value);

            if (headers.containsKey(header.Key))
                throw new RuntimeException("Duplicate header with key: " + header.Key);

            headers.put(header.Key, header);
        }

        return new DXHttpConfigHeaders(headers);
    }
}
