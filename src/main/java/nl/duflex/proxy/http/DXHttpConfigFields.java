package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class DXHttpConfigFields {
    public static final String ELEMENT_TAG_NAME = "Fields";
    public static final String FORMAT_ATTRIBUTE_NAME = "Format";

    public final Map<String, DXHttpConfigField> Fields;
    public final DXHttpFieldsFormat Format;

    public DXHttpConfigFields(final Map<String, DXHttpConfigField> fields, final DXHttpFieldsFormat format) {
        Fields = fields;
        Format = format;
    }

    public DXHttpConfigField GetFieldByName(final String name) {
        return Fields.get(name);
    }

    public static DXHttpConfigFields FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME))
            throw new RuntimeException("Tag name mismatch, expected: " + ELEMENT_TAG_NAME + ", got: "
                + element.getTagName());

        final var fieldsElements = DXDomUtils.GetChildElementsWithTagName(element,
                DXHttpConfigField.ELEMENT_TAG_NAME);

        final var fields = new HashMap<String, DXHttpConfigField>();

        for (final Element fieldElement : fieldsElements) {
            final var field = DXHttpConfigField.FromElement(fieldElement);
            if (fields.containsKey(field.Name)) throw new RuntimeException("Duplicate name in fields: " + field.Name);
            fields.put(field.Name, field);
        }

        final var formatName = element.getAttribute(FORMAT_ATTRIBUTE_NAME).trim();
        final var format = DXHttpFieldsFormat.GetByName(formatName);

        return new DXHttpConfigFields(fields, format);
    }
}
