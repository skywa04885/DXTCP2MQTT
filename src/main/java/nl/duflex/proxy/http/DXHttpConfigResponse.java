package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;

public class DXHttpConfigResponse {
    public static final String ELEMENT_NAME = "Response";
    public static final String CODE_ATTRIBUTE_NAME = "Code";

    public final int Code;
    public @Nullable final DXHttpConfigFields Fields;
    public @Nullable final DXHttpConfigHeaders Headers;

    public DXHttpConfigResponse(final int code, @Nullable final DXHttpConfigFields fields, @Nullable final DXHttpConfigHeaders headers) {
        Code = code;
        Fields = fields;
        Headers = headers;
    }

    public static DXHttpConfigResponse FromElement(final Element element) {
        final String codeString = element.getAttribute(CODE_ATTRIBUTE_NAME).trim();
        if (codeString.isEmpty())
            throw new RuntimeException("Code is missing from response");

        int code;

        try {
            code = Integer.parseInt(codeString);
        } catch (final NumberFormatException numberFormatException) {
            throw new RuntimeException("Invalid code given");
        }

        final var headersElements = DXDomUtils.GetChildElementsWithTagName(element, DXHttpConfigHeaders.ELEMENT_TAG_NAME);
        DXHttpConfigHeaders headers = null;
        if (headersElements.size() == 1) {
            final var headersElement = headersElements.get(0);
            headers = DXHttpConfigHeaders.FromElement(headersElement);
        }
        if (headersElements.size() > 1) throw new RuntimeException("Too many headers elements");

        final List<Element> fieldsElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigFields.ELEMENT_TAG_NAME);

        if (fieldsElements.size() == 0)
            return new DXHttpConfigResponse(code, null, headers);
        else if (fieldsElements.size() > 1)
            throw new RuntimeException("Too many fields elements specified for response");

        final Element fieldsElement = fieldsElements.get(0);
        final var fields = DXHttpConfigFields.FromElement(fieldsElement);

        return new DXHttpConfigResponse(code, fields, headers);
    }
}
