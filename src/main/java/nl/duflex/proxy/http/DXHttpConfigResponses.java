package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.w3c.dom.Element;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DXHttpConfigResponses {
    public static final String ELEMENT_TAG_NAME = "Responses";

    public final Map<Integer, DXHttpConfigResponse> Responses;

    public DXHttpConfigResponses(final Map<Integer, DXHttpConfigResponse> responses) {
        Responses = responses;
    }

    public DXHttpConfigResponse GetByCode(final int code) {
        return Responses.get(code);
    }

    public static DXHttpConfigResponses FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME))
            throw new RuntimeException("Tag name mismatch, expected: " + ELEMENT_TAG_NAME + ", got: "
                    + element.getTagName());

        final List<Element> responseElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigResponse.ELEMENT_NAME);

        final Map<Integer, DXHttpConfigResponse> responses = new HashMap<>();

        for (final Element responseElement : responseElements) {
            final var response = DXHttpConfigResponse.FromElement(responseElement);
            if (responses.containsKey(response.Code))
                throw new RuntimeException("Duplicate response code " + response.Code);
            responses.put(response.Code, response);
        }

        return new DXHttpConfigResponses(responses);
    }
}
