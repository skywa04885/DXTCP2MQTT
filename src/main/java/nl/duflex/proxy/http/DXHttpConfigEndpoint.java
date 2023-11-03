package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class DXHttpConfigEndpoint {
    public static final String TAG_NAME = "Endpoint";
    public static final String NAME_ATTRIBUTE_NAME = "Name";

    public final String Name;
    public final Map<DXHttpRequestMethod, DXHttpConfigRequest> Requests;

    public DXHttpConfigEndpoint(final String name, final Map<DXHttpRequestMethod, DXHttpConfigRequest> requests) {
        Name = name;
        Requests = requests;
    }

    public DXHttpConfigRequest GetRequestByMethod(final DXHttpRequestMethod method) {
        return Requests.get(method);
    }

    public static DXHttpConfigEndpoint FromElement(final Element element) {
        if (!element.getTagName().equals(TAG_NAME)) throw new RuntimeException("Tag name mismatch");

        final String name = element.getAttribute(NAME_ATTRIBUTE_NAME).trim();
        if (name.isEmpty()) throw new RuntimeException("Name attribute missing");

        final var requestElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigRequest.ELEMENT_TAG_NAME);

        final var requests = new HashMap<DXHttpRequestMethod, DXHttpConfigRequest>();

        for (final Element value : requestElements) {
            final var request = DXHttpConfigRequest.FromElement(value);

            if (requests.containsKey(request.Method))
                throw new RuntimeException("Two requests with same method");

            requests.put(request.Method, request);
        }

        return new DXHttpConfigEndpoint(name, requests);
    }
}
